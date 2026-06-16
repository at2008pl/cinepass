const router = require('express').Router();
const pool = require('../db/pool');
const { adminAuth } = require('../middleware/auth');

router.use(adminAuth);

// ── GET /members ───────────────────────────────────────
router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const offset = (page - 1) * limit;
    const search = req.query.search || '';
    const status = req.query.status || '';

    let where = 'WHERE 1=1';
    const params = [];
    let idx = 1;

    if (search) {
      where += ` AND (name ILIKE $${idx} OR email ILIKE $${idx} OR phone ILIKE $${idx})`;
      params.push(`%${search}%`);
      idx++;
    }
    if (status) {
      where += ` AND status = $${idx}`;
      params.push(status);
      idx++;
    }

    const countQ = await pool.query(`SELECT COUNT(*) as total FROM users ${where}`, params);
    const total = parseInt(countQ.rows[0].total);

    params.push(limit, offset);
    const { rows } = await pool.query(
      `SELECT id, name, phone, email, coin_balance as coins,
        (SELECT COUNT(*) FROM referral_chains WHERE referrer_id = users.id AND level = 1) as referrals,
        referral_code, status, created_at as joined
       FROM users ${where}
       ORDER BY created_at DESC
       LIMIT $${idx} OFFSET $${idx + 1}`,
      params
    );

    res.json({ data: rows, total, page, limit });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /members/:id ───────────────────────────────────
router.get('/:id', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM users WHERE id = $1', [req.params.id]);
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'User not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /members/:id/referral-tree ─────────────────────
router.get('/:id/referral-tree', async (req, res) => {
  try {
    const userId = req.params.id;
    const { rows: direct } = await pool.query(
      `SELECT rc.referee_id as id, m.name, rc.level, rc.reward_coins as coins_earned
       FROM referral_chains rc JOIN users m ON m.id = rc.referee_id
       WHERE rc.referrer_id = $1 ORDER BY rc.level, rc.created_at`,
      [userId]
    );

    const totalChain = await pool.query('SELECT COUNT(*) as total FROM referral_chains WHERE referrer_id = $1', [userId]);
    const maxDepth = await pool.query('SELECT COALESCE(MAX(level), 0) as depth FROM referral_chains WHERE referrer_id = $1', [userId]);

    res.json({
      user_id: parseInt(userId),
      direct_referrals: direct,
      chain_depth: parseInt(maxDepth.rows[0].depth),
      total_chain_members: parseInt(totalChain.rows[0].total)
    });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /members/:id/coin-history ──────────────────────
router.get('/:id/coin-history', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const offset = (page - 1) * limit;

    const { rows } = await pool.query(
      'SELECT * FROM coin_transactions WHERE user_id = $1 ORDER BY created_at DESC LIMIT $2 OFFSET $3',
      [req.params.id, limit, offset]
    );
    res.json({ data: rows });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
