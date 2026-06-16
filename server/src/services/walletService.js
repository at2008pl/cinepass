import pool from '../db.js';

export async function ensureWallet(userId) {
  await pool.query(
    `
      INSERT INTO wallets (user_id)
      VALUES ($1)
      ON CONFLICT (user_id) DO NOTHING
    `,
    [userId],
  );
}

export async function applyWalletTransaction(userId, coins, type, description) {
  await ensureWallet(userId);

  await pool.query(
    `
      UPDATE wallets
      SET
        coins = coins + $2,
        total_earned = total_earned + CASE WHEN $2 > 0 THEN $2 ELSE 0 END,
        total_spent = total_spent + CASE WHEN $2 < 0 THEN ABS($2) ELSE 0 END,
        updated_at = NOW()
      WHERE user_id = $1
    `,
    [userId, coins],
  );

  await pool.query(
    `
      INSERT INTO wallet_transactions (user_id, type, coins, description)
      VALUES ($1, $2, $3, $4)
    `,
    [userId, type, coins, description],
  );
}

export async function getWalletSummary(userId) {
  await ensureWallet(userId);
  const walletResult = await pool.query(
    `
      SELECT user_id, coins, total_earned, total_spent
      FROM wallets
      WHERE user_id = $1
    `,
    [userId],
  );
  return walletResult.rows[0] || null;
}

