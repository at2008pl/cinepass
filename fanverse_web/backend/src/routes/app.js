/**
 * Mobile App (Kotlin) endpoints.
 * These are consumed by the Kotlin Android app.
 */
const router = require('express').Router();
const pool = require('../db/pool');
const { appAuth } = require('../middleware/auth');

// ── GET /app/offers?page=referral&user_id=1 ────────────
router.get('/offers', appAuth, async (req, res) => {
  try {
    const page = req.query.page || 'referral';
    const userId = req.user.id;

    const { rows } = await pool.query(
      `SELECT o.*,
        EXISTS(SELECT 1 FROM offer_redemptions r WHERE r.offer_id = o.id AND r.user_id = $1) as claimed
       FROM offers o
       WHERE o.active = true AND o.page = $2
         AND (o.max_claims IS NULL OR o.claims_count < o.max_claims)
         AND (o.valid_until IS NULL OR o.valid_until >= CURRENT_DATE)
       ORDER BY display_order ASC, created_at DESC`,
      [userId, page]
    );
    res.json({ data: rows });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── POST /app/offers/:offerId/claim ────────────────────
router.post('/offers/:offerId/claim', appAuth, async (req, res) => {
  try {
    const userId = req.user.id;
    const offerId = req.params.offerId;

    // Check if already claimed
    const existing = await pool.query('SELECT id FROM offer_redemptions WHERE user_id=$1 AND offer_id=$2', [userId, offerId]);
    if (existing.rows.length) {
      return res.status(409).json({ error: { code: 'ALREADY_CLAIMED', message: 'You have already claimed this offer' } });
    }

    // Get offer
    const offerQ = await pool.query('SELECT * FROM offers WHERE id=$1 AND active=true', [offerId]);
    if (!offerQ.rows.length) {
      return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Offer not found or inactive' } });
    }
    const offer = offerQ.rows[0];

    // Check max claims
    if (offer.max_claims && offer.claims_count >= offer.max_claims) {
      return res.status(409).json({ error: { code: 'MAX_CLAIMS', message: 'Offer has reached maximum claims' } });
    }

    // Check referral requirement
    if (offer.target_referrals) {
      const refCount = await pool.query('SELECT COUNT(*) as c FROM referral_chains WHERE referrer_id=$1 AND level=1', [userId]);
      if (parseInt(refCount.rows[0].c) < offer.target_referrals) {
        return res.status(400).json({ error: { code: 'INSUFFICIENT_REFERRALS', message: `Need ${offer.target_referrals} referrals` } });
      }
    }

    // Check coin cost
    if (offer.coin_cost > 0) {
      const user = await pool.query('SELECT coin_balance FROM users WHERE id=$1', [userId]);
      if (user.rows[0].coin_balance < offer.coin_cost) {
        return res.status(400).json({ error: { code: 'INSUFFICIENT_COINS', message: 'Not enough coins' } });
      }
      // Deduct coins
      await pool.query('UPDATE users SET coin_balance = coin_balance - $1 WHERE id = $2', [offer.coin_cost, userId]);
      await pool.query(
        'INSERT INTO coin_transactions (user_id, type, coins, reference_id, note) VALUES ($1, $2, $3, $4, $5)',
        [userId, 'redeemed_offer', -offer.coin_cost, offerId, `Redeemed: ${offer.title}`]
      );
    }

    // Create redemption
    const { rows } = await pool.query(
      'INSERT INTO offer_redemptions (user_id, offer_id, coins_spent, reward_sent, status) VALUES ($1,$2,$3,$4,$5) RETURNING *',
      [userId, offerId, offer.coin_cost, offer.reward_value, 'claimed']
    );

    // Increment claims count
    await pool.query('UPDATE offers SET claims_count = claims_count + 1 WHERE id = $1', [offerId]);

    res.status(201).json({ redemption_id: rows[0].id, reward_value: rows[0].reward_sent, status: 'claimed' });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /app/cms/:section ──────────────────────────────
router.get('/cms/:section', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM app_content WHERE section = $1 ORDER BY content_key', [req.params.section]);
    res.json({ data: rows });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /app/config ────────────────────────────────────
router.get('/config', async (req, res) => {
  try {
    const { rows } = await pool.query("SELECT content_key, value FROM app_content WHERE section = 'system'");
    const config = {};
    rows.forEach(r => { config[r.content_key] = r.value; });

    // Add wallet config
    const walletRows = await pool.query("SELECT content_key, value FROM app_content WHERE section = 'wallet'");
    walletRows.rows.forEach(r => { config[r.content_key] = r.value; });

    res.json(config);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /app/feed ──────────────────────────────────────
router.get('/feed', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const offset = (page - 1) * limit;

    const { rows } = await pool.query(
      "SELECT id, layout, title, subtitle, content_type, body, link_url, media_url, thumbnail_url, created_at FROM feed_posts WHERE status = 'live' ORDER BY created_at DESC LIMIT $1 OFFSET $2",
      [limit, offset]
    );
    // Auto-populate thumbnail_url from YouTube link_url when not set
    rows.forEach(r => {
      if (!r.thumbnail_url && r.link_url) {
        const m = r.link_url.match(/(?:youtu\.be\/|v=|shorts\/|embed\/)([A-Za-z0-9_-]{11})/);
        if (m) r.thumbnail_url = `https://img.youtube.com/vi/${m[1]}/hqdefault.jpg`;
      }
    });
    res.json({ data: rows });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /app/member/profile ────────────────────────────
router.get('/member/profile', appAuth, async (req, res) => {
  try {
    const { rows } = await pool.query(
      `SELECT id, name, phone, email, coin_balance as coins, referral_code, status, selfie_url, created_at,
        (SELECT COUNT(*) FROM referral_chains WHERE referrer_id = users.id AND level = 1) as referrals
       FROM users WHERE id = $1`,
      [req.user.id]
    );
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'User not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PUT /app/member/profile ────────────────────────────
router.put('/member/profile', appAuth, async (req, res) => {
  try {
    const { name, email } = req.body;
    const { rows } = await pool.query(
      `UPDATE users SET name = COALESCE($1, name), email = COALESCE($2, email) WHERE id = $3
       RETURNING id, name, phone, email, coin_balance as coins, referral_code, status, created_at,
         (SELECT COUNT(*) FROM referral_chains WHERE referrer_id = users.id AND level = 1) as referrals`,
      [name, email, req.user.id]
    );
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'User not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /app/member/referral-tree ─────────────────────
router.get('/member/referral-tree', appAuth, async (req, res) => {
  try {
    const userId = req.user.id;
    const { rows } = await pool.query(
      `SELECT rc.referee_id as id, u.name, u.referred_by_user_id as parent_id, rc.level,
              COALESCE(rc.reward_coins, 0) as coins_awarded, rc.created_at as join_date
       FROM referral_chains rc
       JOIN users u ON u.id = rc.referee_id
       WHERE rc.referrer_id = $1
       ORDER BY rc.level, rc.created_at`,
      [userId]
    );
    const totalChain = await pool.query(
      'SELECT COUNT(*) as total FROM referral_chains WHERE referrer_id = $1', [userId]
    );
    const maxDepth = await pool.query(
      'SELECT COALESCE(MAX(level), 0) as depth FROM referral_chains WHERE referrer_id = $1', [userId]
    );
    res.json({
      referrals: rows,
      chain_depth: parseInt(maxDepth.rows[0].depth),
      total_chain_members: parseInt(totalChain.rows[0].total)
    });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /app/member/redemptions ────────────────────────
router.get('/member/redemptions', appAuth, async (req, res) => {
  try {
    const { rows } = await pool.query(
      `SELECT r.id, r.offer_id, o.title as offer_title, r.reward_sent as reward_value,
              r.status, r.created_at, r.fulfilled_at
       FROM offer_redemptions r
       JOIN offers o ON o.id = r.offer_id
       WHERE r.user_id = $1
       ORDER BY r.created_at DESC`,
      [req.user.id]
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── POST /app/referral/apply ───────────────────────────
router.post('/referral/apply', appAuth, async (req, res) => {
  try {
    const { referral_code } = req.body;
    if (!referral_code) return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'Referral code required' } });

    const userId = req.user.id;

    // Check if already referred
    const existing = await pool.query('SELECT referred_by FROM members WHERE id = $1', [userId]);
    if (existing.rows[0]?.referred_by) {
      return res.status(409).json({ error: { code: 'ALREADY_REFERRED', message: 'You already have a referrer' } });
    }

    // Find referrer
    const referrer = await pool.query('SELECT id FROM members WHERE referral_code = $1', [referral_code]);
    if (!referrer.rows.length) {
      return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Invalid referral code' } });
    }
    if (referrer.rows[0].id === userId) {
      return res.status(400).json({ error: { code: 'SELF_REFERRAL', message: 'Cannot refer yourself' } });
    }

    const referrerId = referrer.rows[0].id;

    // Update member's referred_by
    await pool.query('UPDATE members SET referred_by = $1 WHERE id = $2', [referrerId, userId]);

    // Create referral chain entries and award coins
    const rules = await pool.query("SELECT * FROM points_rules WHERE rule_type = 'referral_level' AND active = true ORDER BY level_number");

    // L1: Direct referrer
    let currentReferrer = referrerId;
    for (const rule of rules.rows) {
      if (!currentReferrer) break;

      await pool.query(
        'INSERT INTO referral_chain (referrer_id, referee_id, level, coins_awarded) VALUES ($1,$2,$3,$4)',
        [currentReferrer, userId, rule.level_number, rule.coins]
      );
      await pool.query('UPDATE members SET coins = coins + $1 WHERE id = $2', [rule.coins, currentReferrer]);
      await pool.query(
        'INSERT INTO coin_transactions (user_id, type, amount, rule_key, description) VALUES ($1,$2,$3,$4,$5)',
        [currentReferrer, 'credit', rule.coins, rule.rule_key, `${rule.label} - referred user`]
      );

      // Go up the chain
      const parent = await pool.query('SELECT referred_by FROM members WHERE id = $1', [currentReferrer]);
      currentReferrer = parent.rows[0]?.referred_by || null;
    }

    res.json({ message: 'Referral applied successfully' });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});


// ── GET /app/member/wallet ────────────────────────────
router.get('/member/wallet', appAuth, async (req, res) => {
  try {
    const { rows } = await pool.query(
      'SELECT coin_balance as coins FROM users WHERE id = $1',
      [req.user.id]
    );
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'User not found' } });
    const walletRows = await pool.query("SELECT content_key, value FROM app_content WHERE section = 'wallet'");
    const config = {};
    walletRows.rows.forEach(r => { config[r.content_key] = r.value; });
    res.json({ coins: rows[0].coins, config });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /app/member/transactions ─────────────────────
router.get('/member/transactions', appAuth, async (req, res) => {
  try {
    const limit = Math.min(parseInt(req.query.limit) || 20, 100);
    const offset = parseInt(req.query.offset) || 0;
    const { rows } = await pool.query(
      `SELECT id, type, coins, note, created_at
       FROM coin_transactions
       WHERE user_id = $1
       ORDER BY created_at DESC
       LIMIT $2 OFFSET $3`,
      [req.user.id, limit, offset]
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
