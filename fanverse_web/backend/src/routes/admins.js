const router = require('express').Router();
const bcrypt = require('bcryptjs');
const pool = require('../db/pool');
const { adminAuth } = require('../middleware/auth');

router.use(adminAuth);

// ── GET /admins ────────────────────────────────────────
router.get('/', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT id, name, email, role, active, last_login, created_at FROM admin_users ORDER BY id');
    res.json({ data: rows });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── POST /admins/invite ────────────────────────────────
router.post('/invite', async (req, res) => {
  try {
    if (req.admin.role !== 'super_admin') {
      return res.status(403).json({ error: { code: 'FORBIDDEN', message: 'Only super admins can invite' } });
    }

    const { name, email, role } = req.body;
    if (!name || !email) {
      return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'Name and email are required' } });
    }

    const tempPassword = Math.random().toString(36).slice(-8);
    const hash = await bcrypt.hash(tempPassword, 10);

    const { rows } = await pool.query(
      'INSERT INTO admin_users (name, email, password, role, invited_by) VALUES ($1,$2,$3,$4,$5) RETURNING id, name, email, role, active',
      [name, email, hash, role || 'content', req.admin.id]
    );

    // In production, send an email with tempPassword
    console.log(`📧 Invite sent to ${email} with temp password: ${tempPassword}`);

    res.status(201).json({ ...rows[0], temp_password: tempPassword });
  } catch (err) {
    if (err.code === '23505') return res.status(409).json({ error: { code: 'CONFLICT', message: 'Email already exists' } });
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PUT /admins/:id/role ───────────────────────────────
router.put('/:id/role', async (req, res) => {
  try {
    if (req.admin.role !== 'super_admin') {
      return res.status(403).json({ error: { code: 'FORBIDDEN', message: 'Only super admins can change roles' } });
    }
    const { role } = req.body;
    const { rows } = await pool.query('UPDATE admin_users SET role=$1 WHERE id=$2 RETURNING id, name, email, role, active', [role, req.params.id]);
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Admin not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PATCH /admins/:id/toggle ───────────────────────────
router.patch('/:id/toggle', async (req, res) => {
  try {
    if (req.admin.role !== 'super_admin') {
      return res.status(403).json({ error: { code: 'FORBIDDEN', message: 'Only super admins can toggle access' } });
    }
    const { active } = req.body;
    const { rows } = await pool.query('UPDATE admin_users SET active=$1 WHERE id=$2 RETURNING id, name, email, role, active', [active, req.params.id]);
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Admin not found' } });
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
