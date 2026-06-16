const router = require('express').Router();
const pool = require('../db/pool');
const { adminAuth } = require('../middleware/auth');

router.use(adminAuth);

// ── GET /points-rules ──────────────────────────────────
router.get('/', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM point_rules ORDER BY rule_type, level_number NULLS LAST');
    const referral_levels = rows.filter(r => r.rule_type === 'referral_level');
    const bonus_rules = rows.filter(r => r.rule_type !== 'referral_level');
    res.json({ referral_levels, bonus_rules });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── POST /points-rules ────────────────────────────────
router.post('/', async (req, res) => {
  try {
    const { rule_key, label, coins, rule_type, level_number, active, description } = req.body;
    if (!rule_key || !label || coins === undefined || !rule_type) {
      return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'rule_key, label, coins, and rule_type are required' } });
    }
    const { rows } = await pool.query(
      'INSERT INTO point_rules (rule_key, label, coins, rule_type, level_number, active, description, updated_by) VALUES ($1,$2,$3,$4,$5,$6,$7,$8) RETURNING *',
      [rule_key, label, coins, rule_type, level_number, active ?? true, description, req.admin.id]
    );
    res.status(201).json(rows[0]);
  } catch (err) {
    if (err.code === '23505') return res.status(409).json({ error: { code: 'CONFLICT', message: 'Rule key already exists' } });
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PUT /points-rules/:id ─────────────────────────────
router.put('/:id', async (req, res) => {
  try {
    const { rule_key, label, coins, rule_type, level_number, active, description } = req.body;
    const { rows } = await pool.query(
      'UPDATE point_rules SET rule_key=$1, label=$2, coins=$3, rule_type=$4, level_number=$5, active=$6, description=$7, updated_by=$8, updated_at=NOW() WHERE id=$9 RETURNING *',
      [rule_key, label, coins, rule_type, level_number, active, description, req.admin.id, req.params.id]
    );
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Rule not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── DELETE /points-rules/:id ──────────────────────────
router.delete('/:id', async (req, res) => {
  try {
    const { rowCount } = await pool.query('DELETE FROM point_rules WHERE id=$1', [req.params.id]);
    if (!rowCount) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Rule not found' } });
    res.json({ message: 'Deleted' });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PATCH /points-rules/:id/toggle ────────────────────
router.patch('/:id/toggle', async (req, res) => {
  try {
    const { active } = req.body;
    const { rows } = await pool.query('UPDATE point_rules SET active=$1, updated_by=$2, updated_at=NOW() WHERE id=$3 RETURNING *', [active, req.admin.id, req.params.id]);
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Rule not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
