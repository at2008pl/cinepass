import pool from '../db.js';
import { getUserIdFromAuthorization } from '../utils/authSession.js';
import { applyWalletTransaction, ensureWallet, getWalletSummary } from '../services/walletService.js';
import { getReferralStats as fetchReferralStats, generateShareLink, getReferralTree as fetchReferralTree } from '../services/referralService.js';

const COIN_TO_RUPEE = 0.1;

const mapProfile = (row, coins) => ({
  id: String(row.id),
  name: (row.name || '').replace(/^"|"$/g, ''),
  email: (row.email || '').replace(/^"|"$/g, ''),
  phone: (row.phone || '').replace(/^"|"$/g, ''),
  referralCode: (row.referral_code || '').replace(/^"|"$/g, ''),
  isVerified: true,
  isAmbassador: Number(coins || 0) >= 3000,
  coins: Number(coins || 0),
  selfieUrl: (row.selfie_url || '').replace(/^"|"$/g, '') || null,
});

const getAuthedUserId = (req, res) => {
  const userId = getUserIdFromAuthorization(req.headers.authorization) || Number(req.query.userId || 0);
  if (!userId) {
    res.status(401).json({ success: false, message: 'Unauthorized', data: null });
    return null;
  }
  return userId;
};

export const getProfile = async (req, res) => {
  const userId = getAuthedUserId(req, res);
  if (!userId) return;

  try {
    await ensureWallet(userId);
    const result = await pool.query(
      `
        SELECT u.*, COALESCE(w.coins, 0) AS wallet_coins
        FROM users u
        LEFT JOIN wallets w ON w.user_id = u.id
        WHERE u.id = $1
        LIMIT 1
      `,
      [userId],
    );

    const user = result.rows[0];
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found', data: null });
    }

    // Get referral statistics
    const referralStats = await fetchReferralStats(userId);
    const shareLink = generateShareLink(user.referral_code);

    return res.json({
      success: true,
      message: 'Profile loaded',
      data: {
        ...mapProfile(user, user.wallet_coins),
        referralStats: {
          directReferrals: referralStats.directReferrals,
          totalEarnings: referralStats.totalEarnings,
          byLevel: referralStats.byLevel,
          shareLink: shareLink,
        },
      },
    });
  } catch (error) {
    console.error('[USERS] getProfile failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to load profile', data: null });
  }
};

export const getWallet = async (req, res) => {
  const userId = getAuthedUserId(req, res);
  if (!userId) return;

  try {
    const wallet = await getWalletSummary(userId);
    const referrals = await pool.query(
      `
        SELECT COUNT(*)::INT AS count
        FROM referrals
        WHERE referrer_user_id = $1
      `,
      [userId],
    );

    const coins = Number(wallet?.coins || 0);
    return res.json({
      success: true,
      message: 'Wallet loaded',
      data: {
        coins,
        rupeeValue: `₹${(coins * COIN_TO_RUPEE).toFixed(0)}`,
        totalEarned: Number(wallet?.total_earned || 0),
        totalSpent: Number(wallet?.total_spent || 0),
        totalReferrals: Number(referrals.rows[0]?.count || 0),
      },
    });
  } catch (error) {
    console.error('[USERS] getWallet failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to load wallet', data: null });
  }
};

export const getWalletTransactions = async (req, res) => {
  const userId = getAuthedUserId(req, res);
  if (!userId) return;

  const page = Math.max(1, Number(req.query.page || 1));
  const limit = Math.min(50, Math.max(5, Number(req.query.limit || 20)));
  const offset = (page - 1) * limit;

  try {
    const txResult = await pool.query(
      `
        SELECT id, type, coins, description, created_at
        FROM wallet_transactions
        WHERE user_id = $1
        ORDER BY created_at DESC
        LIMIT $2 OFFSET $3
      `,
      [userId, limit, offset],
    );

    const totalResult = await pool.query(
      `
        SELECT COUNT(*)::INT AS count
        FROM wallet_transactions
        WHERE user_id = $1
      `,
      [userId],
    );

    const total = Number(totalResult.rows[0]?.count || 0);
    return res.json({
      success: true,
      data: txResult.rows.map((tx) => ({
        id: String(tx.id),
        type: tx.type,
        coins: Number(tx.coins),
        description: tx.description,
        createdAt: tx.created_at,
      })),
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.max(1, Math.ceil(total / limit)),
      },
    });
  } catch (error) {
    console.error('[USERS] getWalletTransactions failed:', error);
    return res.status(500).json({
      success: false,
      data: [],
      pagination: { total: 0, page: 1, limit, totalPages: 0 },
    });
  }
};

export const redeemCoins = async (req, res) => {
  const userId = getAuthedUserId(req, res);
  if (!userId) return;

  const requestedCoins = Number(req.body?.coins || 0);
  const upiId = String(req.body?.upiId || '').trim();

  if (!requestedCoins || requestedCoins <= 0) {
    return res.status(400).json({ success: false, message: 'Coins must be greater than zero', data: null });
  }
  if (!upiId) {
    return res.status(400).json({ success: false, message: 'UPI ID is required', data: null });
  }

  try {
    const wallet = await getWalletSummary(userId);
    const currentCoins = Number(wallet?.coins || 0);
    if (requestedCoins > currentCoins) {
      return res.status(400).json({ success: false, message: 'Insufficient coins', data: null });
    }

    await applyWalletTransaction(
      userId,
      -requestedCoins,
      'REDEEMED_CASH',
      `Redeemed to ${upiId}`,
    );

    return res.json({
      success: true,
      message: 'Coins redeemed successfully',
      data: {
        redeemedCoins: requestedCoins,
        redeemedValue: Number((requestedCoins * COIN_TO_RUPEE).toFixed(2)),
      },
    });
  } catch (error) {
    console.error('[USERS] redeemCoins failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to redeem coins', data: null });
  }
};

export const getLeaderboard = async (_req, res) => {
  try {
    const result = await pool.query(
      `
        SELECT
          u.id,
          u.name,
          u.referral_code,
          COALESCE(w.coins, 0) AS coins
        FROM users u
        LEFT JOIN wallets w ON w.user_id = u.id
        ORDER BY COALESCE(w.coins, 0) DESC, u.created_at ASC
        LIMIT 20
      `,
    );

    return res.json({
      success: true,
      message: 'Leaderboard loaded',
      data: {
        users: result.rows.map((row, index) => ({
          rank: index + 1,
          id: String(row.id),
          name: row.name,
          referralCode: row.referral_code,
          coins: Number(row.coins),
        })),
      },
    });
  } catch (error) {
    console.error('[USERS] getLeaderboard failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to load leaderboard', data: null });
  }
};

export const getReferralStats = async (req, res) => {
  const userId = getAuthedUserId(req, res);
  if (!userId) return;

  try {
    const userResult = await pool.query(
      `
        SELECT id, referral_code
        FROM users
        WHERE id = $1
        LIMIT 1
      `,
      [userId],
    );
    const user = userResult.rows[0];
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found', data: null });
    }

    const referrals = await pool.query(
      `
        SELECT
          r.id,
          r.status,
          r.coins_earned,
          r.created_at,
          u.name AS referred_name
        FROM referrals r
        JOIN users u ON u.id = r.referred_user_id
        WHERE r.referrer_user_id = $1
        ORDER BY r.created_at DESC
      `,
      [userId],
    );

    const totalEarned = referrals.rows.reduce((sum, row) => sum + Number(row.coins_earned || 0), 0);
    return res.json({
      success: true,
      message: 'Referral stats loaded',
      data: {
        referralCode: user.referral_code || '',
        referralLink: `fanverse.app/invite/${user.referral_code || ''}`,
        totalReferrals: referrals.rows.length,
        totalCoinsEarned: totalEarned,
        referrals: referrals.rows.map((row) => ({
          id: String(row.id),
          name: row.referred_name,
          status: row.status,
          coinsEarned: Number(row.coins_earned || 0),
          createdAt: row.created_at,
        })),
      },
    });
  } catch (error) {
    console.error('[USERS] getReferralStats failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to load referrals', data: null });
  }
};

export const getReferralTree = async (req, res) => {
  const userId = getAuthedUserId(req, res);
  if (!userId) return;

  try {
    const tree = await fetchReferralTree(userId);
    return res.json({
      success: true,
      message: 'Referral tree loaded',
      data: tree,
    });
  } catch (error) {
    console.error('[USERS] getReferralTree failed:', error);
    return res.status(500).json({ success: false, message: 'Failed to load referral tree', data: null });
  }
};

