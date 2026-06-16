const router = require('express').Router();
const pool = require('../db/pool');
const { adminAuth } = require('../middleware/auth');

router.use(adminAuth);

// ── GET /cms ───────────────────────────────────────────
router.get('/', async (req, res) => {
  try {
    const { rows } = await pool.query('SELECT * FROM app_content ORDER BY section, content_key');
    const grouped = {};
    rows.forEach(item => {
      if (!grouped[item.section]) grouped[item.section] = [];
      grouped[item.section].push(item);
    });
    res.json(grouped);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PUT /cms/:section ──────────────────────────────────
router.put('/:section', async (req, res) => {
  try {
    const { items } = req.body;
    if (!items || !Array.isArray(items)) {
      return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'items array required' } });
    }

    for (const item of items) {
      await pool.query('UPDATE app_content SET value = $1, updated_by = $2, updated_at = NOW() WHERE content_key = $3 AND section = $4', [item.value, req.admin.id, item.content_key, req.params.section]);
    }

    const { rows } = await pool.query('SELECT * FROM app_content WHERE section = $1 ORDER BY content_key', [req.params.section]);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
