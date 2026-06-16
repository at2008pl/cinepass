const router = require('express').Router();
const pool = require('../db/pool');
const { adminAuth } = require('../middleware/auth');

router.use(adminAuth);

// ── GET /analytics/overview ────────────────────────────
router.get('/overview', async (req, res) => {
  try {
    const totalMembers = await pool.query('SELECT COUNT(*) as c FROM users');
    const totalCoins = await pool.query('SELECT COALESCE(SUM(coin_balance), 0) as c FROM users');
    const totalClaims = await pool.query('SELECT COUNT(*) as c FROM offer_redemptions');
    const avgChain = await pool.query('SELECT COALESCE(AVG(level), 0) as c FROM referral_chains');
    const members30 = parseInt(totalMembers.rows[0].c);

    const churnQ = await pool.query("SELECT COUNT(*) as c FROM users WHERE created_at < NOW() - INTERVAL '30 days'");
    const churn = members30 > 0 ? ((parseInt(churnQ.rows[0].c) / members30) * 100).toFixed(1) : 0;

    const convQ = await pool.query('SELECT COUNT(*) as c FROM users WHERE status != $1', ['pending']);
    const conversion = members30 > 0 ? Math.round((parseInt(convQ.rows[0].c) / members30) * 100) : 0;

    const avgCoins = members30 > 0 ? Math.round(parseInt(totalCoins.rows[0].c) / members30) : 0;

    res.json({
      conversion_rate: conversion,
      avg_coins_per_member: avgCoins,
      total_offers_claimed: parseInt(totalClaims.rows[0].c),
      avg_chain_depth: parseFloat(parseFloat(avgChain.rows[0].c).toFixed(1)),
      churn_30d: parseFloat(churn),
    });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── GET /analytics/growth?months=7 ─────────────────────
router.get('/growth', async (req, res) => {
  try {
    const months = parseInt(req.query.months) || 7;

    const { rows } = await pool.query(`
      SELECT TO_CHAR(d, 'Mon') as label, d as month_start,
        (SELECT COUNT(*) FROM users WHERE created_at <= d + INTERVAL '1 month') as members,
        (SELECT COUNT(*) FROM referral_chains WHERE created_at <= d + INTERVAL '1 month') as referrals,
        (SELECT COALESCE(SUM(coins),0) FROM coin_transactions WHERE coins > 0 AND created_at <= d + INTERVAL '1 month') as coins,
        (SELECT COUNT(*) FROM offer_redemptions WHERE claimed_at <= d + INTERVAL '1 month') as offers
      FROM generate_series(
        DATE_TRUNC('month', NOW()) - INTERVAL '${months - 1} months',
        DATE_TRUNC('month', NOW()),
        '1 month'
      ) as d
      ORDER BY d
    `);

    res.json({
      labels: rows.map(r => r.label),
      members: rows.map(r => parseInt(r.members)),
      referrals: rows.map(r => parseInt(r.referrals)),
      coins: rows.map(r => parseInt(r.coins)),
      offers: rows.map(r => parseInt(r.offers)),
    });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
