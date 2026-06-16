import pool from '../db.js';
import { getUserIdFromAuthorization } from '../utils/authSession.js';
import { applyWalletTransaction, ensureWallet, getWalletSummary } from '../services/walletService.js';

const COIN_TO_RUPEE = 0.1;
const MAX_COIN_DISCOUNT_PERCENT = 0.3;

const createTicketId = () => `tkt_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`;

const mapTicketRow = (row) => ({
  id: row.id,
  seats: row.seat_labels || [],
  totalPrice: Number(row.total_price || 0),
  coinsUsed: Number(row.coins_used || 0),
  discount: Number(row.discount || 0),
  finalPrice: Number(row.final_price || 0),
  qrCode: row.qr_code,
  status: row.status,
  event: row.event_title,
  showtime: row.show_start_time,
  format: row.show_format,
  venue: row.event_venue,
});

const mapTicketWithShowtime = (row) => ({
  id: row.id,
  seats: row.seat_labels || [],
  finalPrice: Number(row.final_price || 0),
  qrCode: row.qr_code,
  status: row.status,
  showtime: {
    startTime: row.show_start_time,
    format: row.show_format,
    event: {
      title: row.event_title,
      venue: row.event_venue,
      city: row.event_city,
    },
  },
});

export const bookTicket = async (req, res) => {
  const userId = getUserIdFromAuthorization(req.headers.authorization);
  if (!userId) {
    return res.status(401).json({ success: false, message: 'Unauthorized', data: null });
  }

  const { showtimeId, seats, coinsToUse = 0 } = req.body || {};
  const seatList = Array.isArray(seats) ? seats.filter(Boolean) : [];
  if (!showtimeId || seatList.length === 0) {
    return res.status(400).json({ success: false, message: 'Showtime and seats are required', data: null });
  }

  try {
    await ensureWallet(userId);

    const showtimeResult = await pool.query(
      `
        SELECT
          s.id,
          s.event_id,
          s.start_time,
          s.format,
          s.price,
          e.title AS event_title,
          e.venue AS event_venue,
          e.city AS event_city,
          e.discount_for_registered
        FROM showtimes s
        JOIN events e ON e.id = s.event_id
        WHERE s.id = $1
        LIMIT 1
      `,
      [showtimeId],
    );
    const showtime = showtimeResult.rows[0];
    if (!showtime) {
      return res.status(404).json({ success: false, message: 'Showtime not found', data: null });
    }

    const isRegisteredResult = await pool.query(
      `
        SELECT has_priority
        FROM event_registrations
        WHERE event_id = $1 AND user_id = $2
        LIMIT 1
      `,
      [showtime.event_id, userId],
    );
    const hasPriority = Boolean(isRegisteredResult.rows[0]?.has_priority);

    const wallet = await getWalletSummary(userId);
    const currentCoins = Number(wallet?.coins || 0);
    const requestedCoins = Math.max(0, Number(coinsToUse || 0));

    const seatCount = seatList.length;
    const originalPrice = Number(showtime.price) * seatCount;
    const maxCoinDiscountRupee = originalPrice * MAX_COIN_DISCOUNT_PERCENT;
    const maxCoinsAllowed = Math.floor(maxCoinDiscountRupee / COIN_TO_RUPEE);
    const coinsUsed = Math.min(requestedCoins, currentCoins, maxCoinsAllowed);
    const coinDiscount = coinsUsed * COIN_TO_RUPEE;

    const baseAfterCoin = Math.max(0, originalPrice - coinDiscount);
    const priorityDiscount = hasPriority
      ? Number((baseAfterCoin * (Number(showtime.discount_for_registered || 0) / 100)).toFixed(2))
      : 0;
    const totalDiscount = Number((coinDiscount + priorityDiscount).toFixed(2));
    const finalPrice = Number(Math.max(0, originalPrice - totalDiscount).toFixed(2));

    const ticketId = createTicketId();
    const qrCode = `cinepass:${ticketId}:${userId}`;

    await pool.query(
      `
        INSERT INTO tickets (
          id, user_id, event_id, showtime_id, seat_labels,
          total_price, coins_used, discount, final_price, qr_code, status
        )
        VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,'BOOKED')
      `,
      [
        ticketId,
        userId,
        showtime.event_id,
        showtime.id,
        seatList,
        originalPrice,
        coinsUsed,
        totalDiscount,
        finalPrice,
        qrCode,
      ],
    );

    if (coinsUsed > 0) {
      await applyWalletTransaction(
        userId,
        -coinsUsed,
        'REDEEMED_TICKET',
        `Ticket booking discount for ${showtime.event_title}`,
      );
    }

    return res.json({
      success: true,
      message: 'Ticket booked successfully',
      data: {
        ticket: {
          id: ticketId,
          seats: seatList,
          totalPrice: originalPrice,
          coinsUsed,
          discount: totalDiscount,
          finalPrice,
          qrCode,
          status: 'BOOKED',
          event: showtime.event_title,
          showtime: showtime.start_time,
          format: showtime.format,
          venue: showtime.event_venue,
        },
        pricing: {
          originalPrice,
          coinDiscount,
          priorityDiscount,
          totalDiscount,
          finalPrice,
          coinsSaved: coinsUsed,
        },
      },
    });
  } catch (error) {
    console.error('[TICKETS] bookTicket failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to book ticket', data: null });
  }
};

export const getMyTickets = async (req, res) => {
  const userId = getUserIdFromAuthorization(req.headers.authorization);
  if (!userId) {
    return res.status(401).json({ success: false, message: 'Unauthorized', data: null });
  }

  try {
    const result = await pool.query(
      `
        SELECT
          t.*,
          s.start_time AS show_start_time,
          s.format AS show_format,
          e.title AS event_title,
          e.venue AS event_venue,
          e.city AS event_city
        FROM tickets t
        JOIN showtimes s ON s.id = t.showtime_id
        JOIN events e ON e.id = t.event_id
        WHERE t.user_id = $1
        ORDER BY s.start_time DESC, t.created_at DESC
      `,
      [userId],
    );

    const now = new Date();
    const upcoming = [];
    const past = [];
    result.rows.forEach((row) => {
      const mapped = mapTicketWithShowtime(row);
      if (new Date(row.show_start_time) > now && row.status !== 'CANCELLED') {
        upcoming.push(mapped);
      } else {
        past.push(mapped);
      }
    });

    return res.json({
      success: true,
      message: 'Tickets loaded',
      data: {
        upcoming,
        past,
        total: result.rows.length,
      },
    });
  } catch (error) {
    console.error('[TICKETS] getMyTickets failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to load tickets', data: null });
  }
};

export const getTicketById = async (req, res) => {
  const userId = getUserIdFromAuthorization(req.headers.authorization);
  if (!userId) {
    return res.status(401).json({ success: false, message: 'Unauthorized', data: null });
  }

  const { ticketId } = req.params;
  try {
    const result = await pool.query(
      `
        SELECT
          t.*,
          s.start_time AS show_start_time,
          s.format AS show_format,
          e.title AS event_title,
          e.venue AS event_venue
        FROM tickets t
        JOIN showtimes s ON s.id = t.showtime_id
        JOIN events e ON e.id = t.event_id
        WHERE t.id = $1 AND t.user_id = $2
        LIMIT 1
      `,
      [ticketId, userId],
    );

    const row = result.rows[0];
    if (!row) {
      return res.status(404).json({ success: false, message: 'Ticket not found', data: null });
    }

    return res.json({
      success: true,
      message: 'Ticket loaded',
      data: mapTicketRow(row),
    });
  } catch (error) {
    console.error('[TICKETS] getTicketById failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to load ticket', data: null });
  }
};

export const cancelTicket = async (req, res) => {
  const userId = getUserIdFromAuthorization(req.headers.authorization);
  if (!userId) {
    return res.status(401).json({ success: false, message: 'Unauthorized', data: null });
  }

  const { ticketId } = req.params;
  try {
    const existingResult = await pool.query(
      `
        SELECT id, status, coins_used
        FROM tickets
        WHERE id = $1 AND user_id = $2
        LIMIT 1
      `,
      [ticketId, userId],
    );
    const ticket = existingResult.rows[0];
    if (!ticket) {
      return res.status(404).json({ success: false, message: 'Ticket not found', data: null });
    }
    if (ticket.status === 'CANCELLED') {
      return res.json({ success: true, message: 'Ticket already cancelled', data: null });
    }

    await pool.query(
      `
        UPDATE tickets
        SET status = 'CANCELLED'
        WHERE id = $1
      `,
      [ticketId],
    );

    const refundCoins = Number(ticket.coins_used || 0);
    if (refundCoins > 0) {
      await applyWalletTransaction(
        userId,
        refundCoins,
        'REFUND_TICKET',
        `Refunded coins from cancelled ticket ${ticketId}`,
      );
    }

    return res.json({ success: true, message: 'Ticket cancelled', data: null });
  } catch (error) {
    console.error('[TICKETS] cancelTicket failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to cancel ticket', data: null });
  }
};

