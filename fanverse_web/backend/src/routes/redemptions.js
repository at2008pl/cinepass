const router = require('express').Router();
const pool = require('../db/pool');
const { adminAuth } = require('../middleware/auth');

router.use(adminAuth);

// ── GET /redemptions ───────────────────────────────────
router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const offset = (page - 1) * limit;
    const status = req.query.status;

    let where = '';
    const params = [];
    if (status) { where = 'WHERE r.status = $1'; params.push(status); }

    params.push(limit, offset);
    const limIdx = params.length - 1;

    const { rows } = await pool.query(
      `SELECT r.*, m.name as user_name, o.title as offer_title
       FROM offer_redemptions r
       JOIN users m ON m.id = r.user_id
       JOIN offers o ON o.id = r.offer_id
       ${where}
       ORDER BY r.claimed_at DESC
       LIMIT $${limIdx} OFFSET $${limIdx + 1}`,
      params
    );
    res.json({ data: rows });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PATCH /redemptions/:id/fulfill ─────────────────────
router.patch('/:id/fulfill', async (req, res) => {
  try {
    const { rows } = await pool.query(
      "UPDATE offer_redemptions SET status='fulfilled', fulfilled_at=NOW() WHERE id=$1 RETURNING *",
      [req.params.id]
    );
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Redemption not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
