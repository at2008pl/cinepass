import pool from '../db.js';
import { getUserIdFromAuthorization } from '../utils/authSession.js';

const mapEventRow = async (eventRow) => {
  const [showtimesResult, countResult] = await Promise.all([
    pool.query(
      `
        SELECT id, start_time, format, price, available_seats, priority_seats
        FROM showtimes
        WHERE event_id = $1
        ORDER BY start_time ASC
      `,
      [eventRow.id],
    ),
    pool.query(
      `
        SELECT COUNT(*)::INT AS registrations
        FROM event_registrations
        WHERE event_id = $1
      `,
      [eventRow.id],
    ),
  ]);

  return {
    id: eventRow.id,
    title: eventRow.title,
    venue: eventRow.venue,
    city: eventRow.city,
    description: eventRow.description,
    coinsPerReferral: Number(eventRow.coins_per_referral || 0),
    attendanceBonus: Number(eventRow.attendance_bonus || 0),
    organizer: { name: eventRow.organizer_name || 'RS3 Films' },
    showtimes: showtimesResult.rows.map((show) => ({
      id: show.id,
      startTime: show.start_time,
      format: show.format,
      price: Number(show.price),
      availableSeats: Number(show.available_seats),
      prioritySeats: Number(show.priority_seats),
    })),
    _count: {
      registrations: Number(countResult.rows[0]?.registrations || 0),
    },
  };
};

export const listEvents = async (req, res) => {
  const city = String(req.query.city || '').trim();
  const page = Math.max(1, Number(req.query.page || 1));
  const limit = Math.min(25, Math.max(5, Number(req.query.limit || 20)));
  const offset = (page - 1) * limit;

  try {
    const whereClauses = [];
    const params = [];
    if (city) {
      params.push(city);
      whereClauses.push(`LOWER(city) = LOWER($${params.length})`);
    }
    const whereSql = whereClauses.length ? `WHERE ${whereClauses.join(' AND ')}` : '';

    const [eventsResult, countResult] = await Promise.all([
      pool.query(
        `
          SELECT *
          FROM events
          ${whereSql}
          ORDER BY date ASC, created_at DESC
          LIMIT $${params.length + 1} OFFSET $${params.length + 2}
        `,
        [...params, limit, offset],
      ),
      pool.query(
        `
          SELECT COUNT(*)::INT AS count
          FROM events
          ${whereSql}
        `,
        params,
      ),
    ]);

    const data = await Promise.all(eventsResult.rows.map(mapEventRow));
    const total = Number(countResult.rows[0]?.count || 0);

    return res.json({
      success: true,
      data,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.max(1, Math.ceil(total / limit)),
      },
    });
  } catch (error) {
    console.error('[EVENTS] listEvents failed:', error);
    return res.status(500).json({
      success: false,
      data: [],
      pagination: { total: 0, page: 1, limit, totalPages: 0 },
    });
  }
};

export const getEvent = async (req, res) => {
  const { id } = req.params;
  try {
    const eventResult = await pool.query(
      `
        SELECT *
        FROM events
        WHERE id = $1
        LIMIT 1
      `,
      [id],
    );

    const event = eventResult.rows[0];
    if (!event) {
      return res.status(404).json({ success: false, message: 'Event not found', data: null });
    }

    const mapped = await mapEventRow(event);
    return res.json({
      success: true,
      message: 'Event loaded',
      data: mapped,
    });
  } catch (error) {
    console.error('[EVENTS] getEvent failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to load event', data: null });
  }
};

export const registerForEvent = async (req, res) => {
  const userId = getUserIdFromAuthorization(req.headers.authorization);
  if (!userId) {
    return res.status(401).json({ success: false, message: 'Unauthorized', data: null });
  }

  const { eventId } = req.params;
  try {
    const eventResult = await pool.query('SELECT id FROM events WHERE id = $1 LIMIT 1', [eventId]);
    if (eventResult.rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Event not found', data: null });
    }

    const registrationResult = await pool.query(
      `
        INSERT INTO event_registrations (event_id, user_id, has_priority)
        VALUES ($1, $2, TRUE)
        ON CONFLICT (event_id, user_id)
        DO UPDATE SET has_priority = TRUE
        RETURNING id, has_priority
      `,
      [eventId, userId],
    );

    const registration = registrationResult.rows[0];
    return res.json({
      success: true,
      message: 'Registered successfully',
      data: {
        registration: {
          id: String(registration.id),
          hasPriority: Boolean(registration.has_priority),
        },
        hasPriority: Boolean(registration.has_priority),
        message: 'Priority booking enabled',
      },
    });
  } catch (error) {
    console.error('[EVENTS] registerForEvent failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to register for event', data: null });
  }
};

