const router = require('express').Router();
const pool = require('../db/pool');
const { adminAuth } = require('../middleware/auth');

router.use(adminAuth);

// ── GET /offers ────────────────────────────────────────
router.get('/', async (req, res) => {
  try {
    const pageFilter = req.query.page_filter;
    const active = req.query.active;
    let where = 'WHERE 1=1';
    const params = [];
    let idx = 1;

    if (pageFilter) { where += ` AND page = $${idx}`; params.push(pageFilter); idx++; }
    if (active !== undefined) { where += ` AND active = $${idx}`; params.push(active === 'true'); idx++; }

    const { rows } = await pool.query(`SELECT * FROM offers ${where} ORDER BY created_at DESC`, params);
    res.json({ data: rows });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── POST /offers ───────────────────────────────────────
router.post('/', async (req, res) => {
  try {
    const { title, description, reward_type, reward_value, coin_cost, target_referrals, page, active, max_claims, valid_until, image_url, display_order } = req.body;
    if (!title || !reward_type || !reward_value) {
      return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'Title, reward_type, and reward_value are required' } });
    }

    const { rows } = await pool.query(
      `INSERT INTO offers (title, description, reward_type, reward_value, coin_cost, target_referrals, page, active, max_claims, valid_until, image_url, display_order, created_by)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13) RETURNING *`,
      [title, description, reward_type, reward_value, coin_cost || 0, target_referrals, page || 'referral', active ?? true, max_claims, valid_until, image_url, display_order || 0, req.admin.id]
    );
    res.status(201).json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PUT /offers/:id ────────────────────────────────────
router.put('/:id', async (req, res) => {
  try {
    const { title, description, reward_type, reward_value, coin_cost, target_referrals, page, active, max_claims, valid_until, image_url, display_order } = req.body;
    const { rows } = await pool.query(
      `UPDATE offers SET title=$1, description=$2, reward_type=$3, reward_value=$4, coin_cost=$5,
       target_referrals=$6, page=$7, active=$8, max_claims=$9, valid_until=$10, image_url=$11, display_order=$12
       WHERE id=$13 RETURNING *`,
      [title, description, reward_type, reward_value, coin_cost, target_referrals, page, active, max_claims, valid_until, image_url, display_order || 0, req.params.id]
    );
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Offer not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── DELETE /offers/:id ─────────────────────────────────
router.delete('/:id', async (req, res) => {
  try {
    const { rowCount } = await pool.query('DELETE FROM offers WHERE id=$1', [req.params.id]);
    if (!rowCount) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Offer not found' } });
    res.json({ message: 'Deleted' });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PATCH /offers/:id/toggle ───────────────────────────
router.patch('/:id/toggle', async (req, res) => {
  try {
    const { active } = req.body;
    const { rows } = await pool.query('UPDATE offers SET active=$1 WHERE id=$2 RETURNING *', [active, req.params.id]);
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Offer not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
