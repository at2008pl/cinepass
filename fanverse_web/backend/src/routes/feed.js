const router = require('express').Router();
const pool = require('../db/pool');
const { adminAuth } = require('../middleware/auth');
const { normalizeFeedRow, normalizeFeedRows } = require('../utils/mediaUrl');

router.use(adminAuth);

// ── GET /feed ──────────────────────────────────────────
router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const offset = (page - 1) * limit;
    const status = req.query.status;

    let where = '';
    const params = [];
    if (status) {
      where = 'WHERE fp.status = $1';
      params.push(status);
    }

    params.push(limit, offset);
    const limIdx = params.length - 1; // last two params are limit, offset

    const { rows } = await pool.query(
      `SELECT fp.*, a.name as author
       FROM feed_posts fp LEFT JOIN admin_users a ON a.id = fp.author_id
       ${where}
       ORDER BY fp.created_at DESC
       LIMIT $${limIdx} OFFSET $${limIdx + 1}`,
      params
    );
    // Auto-populate thumbnail_url from YouTube link_url if missing
    rows.forEach(r => {
      if (!r.thumbnail_url && r.link_url) {
        const m = r.link_url.match(/(?:youtu\.be\/|v=|shorts\/|embed\/)([A-Za-z0-9_-]{11})/);
        if (m) r.thumbnail_url = `https://img.youtube.com/vi/${m[1]}/hqdefault.jpg`;
      }
    });
    res.json({ data: normalizeFeedRows(rows) });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── POST /feed ─────────────────────────────────────────
router.post('/', async (req, res) => {
  try {
    const { layout, title, subtitle, content_type, body, link_url, media_url, thumbnail_url, status } = req.body;
    if (!title) return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'Title is required', field: 'title' } });

    const { rows } = await pool.query(
      `INSERT INTO feed_posts (layout, title, subtitle, content_type, body, link_url, media_url, thumbnail_url, status, author_id)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10) RETURNING *`,
      [layout || 'card', title, subtitle, content_type || 'image', body, link_url, media_url, thumbnail_url, status || 'draft', req.admin.id]
    );
    res.status(201).json(normalizeFeedRow(rows[0]));
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PUT /feed/:id ──────────────────────────────────────
router.put('/:id', async (req, res) => {
  try {
    const { layout, title, subtitle, content_type, body, link_url, media_url, thumbnail_url, status } = req.body;
    const { rows } = await pool.query(
      `UPDATE feed_posts SET layout=$1, title=$2, subtitle=$3, content_type=$4, body=$5, link_url=$6, media_url=$7, thumbnail_url=$8, status=$9
       WHERE id=$10 RETURNING *`,
      [layout, title, subtitle, content_type, body, link_url, media_url, thumbnail_url, status, req.params.id]
    );
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Post not found' } });
    res.json(normalizeFeedRow(rows[0]));
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── DELETE /feed/:id ───────────────────────────────────
router.delete('/:id', async (req, res) => {
  try {
    const { rowCount } = await pool.query('DELETE FROM feed_posts WHERE id=$1', [req.params.id]);
    if (!rowCount) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Post not found' } });
    res.json({ message: 'Deleted' });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── PATCH /feed/:id/status ─────────────────────────────
router.patch('/:id/status', async (req, res) => {
  try {
    const { status } = req.body;
    const { rows } = await pool.query('UPDATE feed_posts SET status=$1 WHERE id=$2 RETURNING *', [status, req.params.id]);
    if (!rows.length) return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Post not found' } });
    res.json(normalizeFeedRow(rows[0]));
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
