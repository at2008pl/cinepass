import pool from '../db.js';
import { encryptReferralCode, normalizeReferralCode } from '../utils/referralLinkCrypto.js';

// Reward structure for each level
const REWARDS = {
  1: 100,  // Direct referral
  2: 40,   // Second level
  3: 15    // Third level
};

/**
 * Generate a unique referral code
 * Format: RS3_XXXXXX (6 random chars)
 */
export async function generateReferralCode() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let code = '';
  let isUnique = false;
  
  while (!isUnique) {
    code = 'RS3_';
    for (let i = 0; i < 6; i++) {
      code += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    
    // Check if code already exists
    const result = await pool.query(
      'SELECT id FROM users WHERE referral_code = $1',
      [code]
    );
    
    if (result.rows.length === 0) {
      isUnique = true;
    }
  }
  
  return code;
}

/**
 * Handle referral rewards when a new user registers
 * Walks up the chain (max 3 levels) and rewards each user
 */
export async function handleReferralRewards(newUserId, referralCode) {
  if (!referralCode) {
    console.log('No referral code provided');
    return;
  }

  const client = await pool.connect();
  
  try {
    await client.query('BEGIN');
    
    // Get the user who owns this referral code
    const referrerResult = await client.query(
      'SELECT id, referred_by_user_id FROM users WHERE referral_code = $1',
      [referralCode]
    );
    
    if (referrerResult.rows.length === 0) {
      console.log(`Referral code ${referralCode} not found`);
      await client.query('ROLLBACK');
      return;
    }
    
    // Update new user's referred_by
    await client.query(
      'UPDATE users SET referred_by_user_id = $1 WHERE id = $2',
      [referrerResult.rows[0].id, newUserId]
    );
    
    // Walk up the chain (max 3 levels)
    const chain = [];
    let currentUser = referrerResult.rows[0];
    let level = 1;
    
    while (currentUser && level <= 3) {
      chain.push({ userId: currentUser.id, level });
      
      if (!currentUser.referred_by_user_id) {
        break;
      }
      
      // Get the next user in chain
      const nextResult = await client.query(
        'SELECT id, referred_by_user_id FROM users WHERE id = $1',
        [currentUser.referred_by_user_id]
      );
      
      if (nextResult.rows.length === 0) {
        break;
      }
      
      currentUser = nextResult.rows[0];
      level++;
    }
    
    // Reward each user in the chain
    for (const { userId, level } of chain) {
      const rewardAmount = REWARDS[level];
      
      // Add coins to user's wallet (wallets table) within the same transaction
      await client.query(
        `
          UPDATE wallets
          SET
            coins = COALESCE(coins, 0) + $1,
            total_earned = COALESCE(total_earned, 0) + CASE WHEN $1 > 0 THEN $1 ELSE 0 END,
            total_spent = COALESCE(total_spent, 0) + CASE WHEN $1 < 0 THEN ABS($1) ELSE 0 END,
            updated_at = NOW()
          WHERE user_id = $2
        `,
        [rewardAmount, userId]
      );
      
      // Log transaction
      await client.query(
        `INSERT INTO transactions (user_id, amount, type, description, related_user_id)
         VALUES ($1, $2, $3, $4, $5)`,
        [
          userId,
          rewardAmount,
          `referral_level_${level}`,
          `Earned ${rewardAmount} coins from level ${level} referral`,
          newUserId
        ]
      );
      
      // Log referral chain
      await client.query(
        `INSERT INTO referral_chains (referrer_id, referred_id, level, reward_amount, reward_given)
         VALUES ($1, $2, $3, $4, $5)
         ON CONFLICT (referrer_id, referred_id) DO NOTHING`,
        [userId, newUserId, level, rewardAmount, true]
      );
      
      console.log(`Rewarded user ${userId} with ${rewardAmount} coins (level ${level})`);
    }
    
    await client.query('COMMIT');
    console.log(`Successfully processed referral chain for user ${newUserId}`);
    
  } catch (error) {
    await client.query('ROLLBACK');
    console.error('Error handling referral rewards:', error);
    throw error;
  } finally {
    client.release();
  }
}

/**
 * Get referral statistics for a user
 */
export async function getReferralStats(userId) {
  try {
    // Get direct referrals count
    const directResult = await pool.query(
      'SELECT COUNT(*) as count FROM users WHERE referred_by_user_id = $1',
      [userId]
    );
    
    // Get total referral earnings
    const earningsResult = await pool.query(
      `SELECT COALESCE(SUM(amount), 0) as total 
       FROM transactions 
       WHERE user_id = $1 AND type LIKE 'referral_level_%'`,
      [userId]
    );
    
    // Get referral chain details
    const chainResult = await pool.query(
      `SELECT level, COUNT(*) as count, SUM(reward_amount) as total_earned
       FROM referral_chains
       WHERE referrer_id = $1
       GROUP BY level
       ORDER BY level`,
      [userId]
    );
    
    return {
      directReferrals: parseInt(directResult.rows[0].count),
      totalEarnings: parseInt(earningsResult.rows[0].total),
      byLevel: chainResult.rows.map(row => ({
        level: row.level,
        count: parseInt(row.count),
        earned: parseInt(row.total_earned)
      }))
    };
  } catch (error) {
    console.error('Error getting referral stats:', error);
    throw error;
  }
}

/**
 * Get the referral tree for a user (one above, all below up to 3 levels)
 */
export async function getReferralTree(userId) {
  try {
    // Get the user who referred this user (one above)
    const referrerResult = await pool.query(
      `SELECT u.id, u.name, u.email, u.referral_code, u.created_at, COALESCE(w.coins, 0) as coins
       FROM users u
       LEFT JOIN wallets w ON w.user_id = u.id
       INNER JOIN users child ON child.referred_by_user_id = u.id
       WHERE child.id = $1`,
      [userId]
    );

    const referrer = referrerResult.rows.length > 0 ? {
      id: referrerResult.rows[0].id,
      name: referrerResult.rows[0].name,
      email: referrerResult.rows[0].email,
      referralCode: referrerResult.rows[0].referral_code,
      coins: parseInt(referrerResult.rows[0].coins),
      joinedDate: referrerResult.rows[0].created_at
    } : null;

   // Get all users referred by this user (all below, level 1)
    const level1Result = await pool.query(
      `SELECT u.id, u.name, u.email, u.referral_code, u.created_at, COALESCE(w.coins, 0) as coins
       FROM users u
       LEFT JOIN wallets w ON w.user_id = u.id
       WHERE u.referred_by_user_id = $1
       ORDER BY u.created_at DESC`,
      [userId]
    );

    const level1 = await Promise.all(level1Result.rows.map(async (row) => {
      // Get level 2 for each level 1 user
      const level2Result = await pool.query(
        `SELECT u.id, u.name, u.email, u.referral_code, u.created_at, COALESCE(w.coins, 0) as coins
         FROM users u
         LEFT JOIN wallets w ON w.user_id = u.id
         WHERE u.referred_by_user_id = $1
         ORDER BY u.created_at DESC`,
        [row.id]
      );

      const level2 = await Promise.all(level2Result.rows.map(async (l2row) => {
        // Get level 3 for each level 2 user
        const level3Result = await pool.query(
          `SELECT u.id, u.name, u.email, u.referral_code, u.created_at, COALESCE(w.coins, 0) as coins
           FROM users u
           LEFT JOIN wallets w ON w.user_id = u.id
           WHERE u.referred_by_user_id = $1
           ORDER BY u.created_at DESC`,
          [l2row.id]
        );

        return {
          id: l2row.id,
          name: l2row.name,
          email: l2row.email,
          referralCode: l2row.referral_code,
          coins: parseInt(l2row.coins),
          joinedDate: l2row.created_at,
          children: level3Result.rows.map(l3row => ({
            id: l3row.id,
            name: l3row.name,
            email: l3row.email,
            referralCode: l3row.referral_code,
            coins: parseInt(l3row.coins),
            joinedDate: l3row.created_at,
            children: []
          }))
        };
      }));

      return {
        id: row.id,
        name: row.name,
        email: row.email,
        referralCode: row.referral_code,
        coins: parseInt(row.coins),
        joinedDate: row.created_at,
        children: level2
      };
    }));

    return {
      referrer,
      children: level1,
      totalDescendants: level1.length + level1.reduce((sum, l1) => 
        sum + l1.children.length + l1.children.reduce((s2, l2) => s2 + l2.children.length, 0), 0)
    };
  } catch (error) {
    console.error('Error getting referral tree:', error);
    throw error;
  }
}

/**
 * Get shareable download link for a referral code
 */
export function generateShareLink(referralCode) {
  const serverUrl = (process.env.SERVER_URL || 'http://192.168.29.212:4000').replace(/\/$/, '');
  const shareBaseUrl = (process.env.SHARE_LINK_BASE_URL || serverUrl).replace(/\/$/, '');
  const appPackage = 'com.cinepass';
  const normalizedCode = normalizeReferralCode(referralCode);
  const shouldEncrypt = process.env.ENCRYPT_REFERRAL_LINK !== 'false';
  
  // For now, use server-hosted APK download page
  // Later, switch to Play Store URL
  const isProduction = process.env.NODE_ENV === 'production';
  
  if (isProduction) {
    // Play Store link (for future)
    const playStoreUrl = `https://play.google.com/store/apps/details?id=${appPackage}`;
    const referrerParams = `utm_source=cinepass&utm_medium=referral&ref=${normalizedCode || ''}`;
    return `${playStoreUrl}&referrer=${encodeURIComponent(referrerParams)}`;
  } else {
    if (!normalizedCode) {
      return `${shareBaseUrl}/download`;
    }

    // Server APK download page (for testing)
    if (shouldEncrypt) {
      const token = encryptReferralCode(normalizedCode);
      if (token) {
        return `${shareBaseUrl}/s/${token}`;
      }
    }

    // Backward-compatible fallback
    return `${shareBaseUrl}/register?ref=${encodeURIComponent(normalizedCode)}`;
  }
}
