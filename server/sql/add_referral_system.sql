-- Add referral system columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS referred_by_user_id INTEGER REFERENCES users(id);
ALTER TABLE users ADD COLUMN IF NOT EXISTS coins INTEGER DEFAULT 0;
ALTER TABLE users ALTER COLUMN referral_code SET NOT NULL;

-- Create unique index on referral_code
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_referral_code ON users(referral_code);

-- Create referral_chains table to track multi-level referrals
CREATE TABLE IF NOT EXISTS referral_chains (
  id SERIAL PRIMARY KEY,
  referrer_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  referred_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  level INTEGER NOT NULL CHECK (level >= 1 AND level <= 3),
  reward_amount INTEGER NOT NULL,
  reward_given BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(referrer_id, referred_id)
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_referral_chains_referrer ON referral_chains(referrer_id);
CREATE INDEX IF NOT EXISTS idx_referral_chains_referred ON referral_chains(referred_id);

-- Create transactions table for coin ledger (append-only for audit trail)
CREATE TABLE IF NOT EXISTS transactions (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  amount INTEGER NOT NULL,
  type VARCHAR(50) NOT NULL, -- 'referral_level_1', 'referral_level_2', 'referral_level_3', 'event_booking', etc.
  description TEXT,
  related_user_id INTEGER REFERENCES users(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for transactions
CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
