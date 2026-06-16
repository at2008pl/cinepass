const router = require('express').Router();
const pool = require('../db/pool');
const { adminAuth } = require('../middleware/auth');

router.use(adminAuth);

// ── GET /dashboard/stats ───────────────────────────────
router.get('/stats', async (req, res) => {
  try {
    const members = await pool.query('SELECT COUNT(*) as total FROM users');
    const referrals = await pool.query('SELECT COUNT(*) as total FROM referral_chains');
    const coins = await pool.query('SELECT COALESCE(SUM(coin_balance), 0) as total FROM users');
    const claims = await pool.query("SELECT COUNT(*) as total FROM offer_redemptions");

    const membersWeek = await pool.query("SELECT COUNT(*) as total FROM users WHERE created_at >= NOW() - INTERVAL '7 days'");
    const referralsToday = await pool.query("SELECT COUNT(*) as total FROM referral_chains WHERE created_at >= CURRENT_DATE");
    const coinsToday = await pool.query("SELECT COALESCE(SUM(coins), 0) as total FROM coin_transactions WHERE coins > 0 AND created_at >= CURRENT_DATE");
    const claimsToday = await pool.query("SELECT COUNT(*) as total FROM offer_redemptions WHERE claimed_at >= CURRENT_DATE");

    res.json({
      total_members: parseInt(members.rows[0].total),
      active_referrals: parseInt(referrals.rows[0].total),
      coins_issued: parseInt(coins.rows[0].total),
      offers_claimed: parseInt(claims.rows[0].total),
      deltas: {
        members_this_week: parseInt(membersWeek.rows[0].total),
        referrals_today: parseInt(referralsToday.rows[0].total),
        coins_today: parseInt(coinsToday.rows[0].total),
        offers_today: parseInt(claimsToday.rows[0].total),
      }
    });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /dashboard/referral-activity?days=7 ────────────
router.get('/referral-activity', async (req, res) => {
  try {
    const days = parseInt(req.query.days) || 7;
    const { rows } = await pool.query(`
      SELECT DATE(created_at) as day, COUNT(*) as count
      FROM referral_chains
      WHERE created_at >= NOW() - INTERVAL '${days} days'
      GROUP BY DATE(created_at)
      ORDER BY day
    `);

    const labels = rows.map(r => new Date(r.day).toLocaleDateString('en', { weekday: 'short' }));
    const data = rows.map(r => parseInt(r.count));

    res.json({ data, labels });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
