
/**
 * Run: node src/db/init.js
 * Creates all tables in PostgreSQL per RS³ Films Implementation Guide v1.0.
 */
require('dotenv').config();
const pool = require('./pool');

const SQL = `

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLE: admin_users
-- Separate from regular users. Admin dashboard accounts only.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS admin_users (
  id           SERIAL PRIMARY KEY,
  name         VARCHAR(100)  NOT NULL,
  email        VARCHAR(100)  UNIQUE NOT NULL,
  password     VARCHAR(255)  NOT NULL,                    -- bcrypt hashed, cost 12
  role         VARCHAR(30)   NOT NULL DEFAULT 'content',  -- super_admin / content / analytics / moderator
  active       BOOLEAN       NOT NULL DEFAULT true,
  last_login   TIMESTAMPTZ,
  invited_by   INT           REFERENCES admin_users(id),  -- super admin who sent invite
  created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLE: users
-- Central table. Every other table FKs back to users.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
  id                  SERIAL PRIMARY KEY,
  name                VARCHAR(100)  NOT NULL,
  phone               VARCHAR(20)   UNIQUE NOT NULL,        -- 10-digit Indian mobile, unique
  email               VARCHAR(100)  UNIQUE NOT NULL,        -- used for login
  gender              VARCHAR(20),                          -- Male / Female / Non-binary / Prefer not to say
  dob                 DATE,                                 -- server-validated: age >= 13
  referral_code       VARCHAR(50)   UNIQUE NOT NULL,        -- format RS3_XXXXXX, server-generated
  referred_by_code    VARCHAR(50)   NOT NULL,               -- mandatory; code entered at registration, retained for audit
  referred_by_user_id INT           REFERENCES users(id),   -- resolved from referred_by_code by server
  address_line        VARCHAR(255),
  city                VARCHAR(100),                         -- auto-populated from pincode via India Post API
  state               VARCHAR(100),
  pincode             VARCHAR(20),
  password            VARCHAR(255)  NOT NULL,               -- bcrypt hashed, cost 12; never returned in responses
  selfie_url          TEXT,                                 -- Cloudinary secure_url; set after upload; never binary
  otp_verified        BOOLEAN       NOT NULL DEFAULT false, -- coin rewards released only when true
  coin_balance        INTEGER       NOT NULL DEFAULT 0,     -- denormalized; reconciled nightly from coin_transactions
  status              VARCHAR(20)   NOT NULL DEFAULT 'pending', -- pending / verified / ambassador / suspended
  fcm_token           TEXT,                                 -- Firebase push token; updated on every login
  created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(), -- server-set; never from client
  updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW()  -- auto-updated via trigger
);

-- Auto-update trigger for users.updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLE: referral_chains
-- Stores the resolved referral tree. One row per referrer-referee pair per
-- level. Max level_number derived from active point_rules — no hardcoded depth.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS referral_chains (
  id           SERIAL PRIMARY KEY,
  referrer_id  INT          NOT NULL REFERENCES users(id), -- person who receives the coin reward
  referee_id   INT          NOT NULL REFERENCES users(id), -- newly registered user
  level        INTEGER      NOT NULL,                      -- 1=direct, 2=one level up, 3=two levels up
  reward_coins INTEGER,                                    -- snapshot of coins at time of registration; preserved if rule changes later
  status       VARCHAR(20)  NOT NULL DEFAULT 'pending',    -- pending / awarded / cancelled
  rewarded_at  TIMESTAMPTZ,                                -- when coins were actually credited (after OTP verification)
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLE: point_rules
-- Admin-configurable via dashboard. Server reads at every registration.
-- No values are hardcoded in application code.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS point_rules (
  id           SERIAL PRIMARY KEY,
  rule_key     VARCHAR(60)  UNIQUE NOT NULL,               -- snake_case; e.g. referral_l1, profile_complete
  label        VARCHAR(100) NOT NULL,                      -- display name in admin panel + app transaction history
  coins        INTEGER      NOT NULL,                      -- editable by admin; effective immediately for future events
  rule_type    VARCHAR(30)  NOT NULL,                      -- referral_level / registration_bonus / event_bonus / custom
  level_number INTEGER,                                    -- for referral_level type: 1,2,3... NULL for non-chain rules
  active       BOOLEAN      NOT NULL DEFAULT true,         -- inactive rules skipped; e.g. setting referral_l3 false reduces chain to L2
  description  TEXT,
  updated_by   INT          REFERENCES admin_users(id),
  updated_at   TIMESTAMPTZ
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLE: coin_transactions
-- Append-only ledger. NEVER UPDATE OR DELETE ROWS.
-- coin_balance on users = SUM of this table per user. This is the audit trail.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS coin_transactions (
  id           SERIAL PRIMARY KEY,
  user_id      INT          NOT NULL REFERENCES users(id),
  type         VARCHAR(40)  NOT NULL, -- earned_referral_l1 / earned_referral_l2 / earned_referral_l3
                                      -- earned_bonus / redeemed_offer / admin_adjustment / reversed
  coins        INTEGER      NOT NULL, -- positive = credit, negative = debit
  reference_id INTEGER,               -- FK to referral_chains.id, offer_redemptions.id, or admin action depending on type
  note         TEXT,                  -- human-readable; shown in app transaction history
  created_by   INT          REFERENCES admin_users(id), -- populated for admin_adjustment; NULL for system events
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLE: feed_posts
-- Every post visible in the app's home feed.
-- Media stored in Cloudinary; only the URL is in PostgreSQL.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS feed_posts (
  id            SERIAL PRIMARY KEY,
  layout        VARCHAR(30)  NOT NULL DEFAULT 'card',   -- hero / card / reel / banner / update / grid2
  title         TEXT         NOT NULL,
  subtitle      TEXT,                                   -- optional tagline
  body          TEXT,                                   -- rich text / markdown content
  media_url     TEXT,                                   -- Cloudinary secure_url; for video: HLS .m3u8 manifest URL
  thumbnail_url TEXT,                                   -- auto-generated by Cloudinary for video posts; used as poster frame
  link_url      TEXT,                                   -- external link for content_type=link
  content_type  VARCHAR(20)  NOT NULL DEFAULT 'image',  -- image / video / text / link
  status        VARCHAR(20)  NOT NULL DEFAULT 'draft',  -- draft / live / scheduled
  scheduled_at  TIMESTAMPTZ,                            -- BullMQ job fires at this time to set status=live
  author_id     INT          REFERENCES admin_users(id),
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

DROP TRIGGER IF EXISTS trg_feed_posts_updated_at ON feed_posts;
CREATE TRIGGER trg_feed_posts_updated_at
  BEFORE UPDATE ON feed_posts
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLE: offers
-- Generalised offer model: movie tickets, event passes, coupon codes,
-- merchandise, custom rewards. No cash/bank integration.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS offers (
  id               SERIAL PRIMARY KEY,
  title            TEXT         NOT NULL,
  description      TEXT,
  image_url        TEXT,                                -- offer banner; Cloudinary URL
  reward_type      VARCHAR(30)  NOT NULL,               -- movie_ticket / event_pass / coupon_code / merchandise / custom
  reward_value     TEXT         NOT NULL,               -- coupon code / ticket reference / pass code delivered to user on claim
  coin_cost        INTEGER      NOT NULL DEFAULT 0,    -- coins deducted on claim; 0 = free/milestone-based
  target_referrals INTEGER,                             -- NULL if coin-cost offer; set if milestone-based (e.g. 5 = refer 5)
  page             VARCHAR(20)  NOT NULL DEFAULT 'referral', -- referral / wallet / home / global
  display_order    INTEGER      NOT NULL DEFAULT 0,    -- admin controls sort; lower = shown first
  active           BOOLEAN      NOT NULL DEFAULT true,  -- if false, hidden in app completely
  max_claims       INTEGER,                             -- NULL = unlimited; atomic increment check prevents over-claiming
  claims_count     INTEGER      NOT NULL DEFAULT 0,    -- server-incremented on each redemption; never trust client
  valid_from       TIMESTAMPTZ,                         -- optional start date
  valid_until      TIMESTAMPTZ,                         -- optional expiry; server checks on every claim attempt
  created_by       INT          REFERENCES admin_users(id),
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

DROP TRIGGER IF EXISTS trg_offers_updated_at ON offers;
CREATE TRIGGER trg_offers_updated_at
  BEFORE UPDATE ON offers
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLE: offer_redemptions
-- Records each user claim. FK to users + offers.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS offer_redemptions (
  id           SERIAL PRIMARY KEY,
  user_id      INT          NOT NULL REFERENCES users(id),
  offer_id     INT          NOT NULL REFERENCES offers(id),
  coins_spent  INTEGER,                                  -- snapshot of coin_cost at time of claim
  reward_sent  TEXT,                                     -- coupon code or ticket reference sent to user
  status       VARCHAR(20)  NOT NULL DEFAULT 'claimed',  -- claimed / fulfilled / expired / cancelled
  claimed_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  fulfilled_at TIMESTAMPTZ                               -- admin-set when physically delivering ticket/pass
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TABLE: app_content
-- The app's CMS. Every configurable string, image URL, number, or boolean
-- in the Android app and dashboard is stored here.
-- App fetches this table on launch and caches it locally (Redis + Room DB).
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS app_content (
  id           SERIAL PRIMARY KEY,
  content_key  VARCHAR(100) UNIQUE NOT NULL,             -- e.g. splash_image_url, terms_and_conditions, min_redeem_coins
  content_type VARCHAR(30)  NOT NULL DEFAULT 'text',     -- text / richtext / image_url / number / url / boolean
  value        TEXT         NOT NULL DEFAULT '',         -- for image_url: Cloudinary URL; for richtext: HTML; for number: '200'
  label        VARCHAR(100) NOT NULL,                    -- human-friendly name shown in admin dashboard CMS editor
  section      VARCHAR(50)  NOT NULL,                    -- splash / onboarding / legal / contact / wallet / branding / social / system
  updated_by   INT          REFERENCES admin_users(id),
  updated_at   TIMESTAMPTZ
);

-- ─────────────────────────────────────────────────────────────────────────────
-- INDEXES
-- ─────────────────────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_users_referral_code         ON users(referral_code);
CREATE INDEX IF NOT EXISTS idx_users_referred_by           ON users(referred_by_user_id);
CREATE INDEX IF NOT EXISTS idx_referral_chains_referrer    ON referral_chains(referrer_id);
CREATE INDEX IF NOT EXISTS idx_referral_chains_referee     ON referral_chains(referee_id);
CREATE INDEX IF NOT EXISTS idx_coin_tx_user                ON coin_transactions(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_feed_posts_status           ON feed_posts(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_offers_active_page          ON offers(active, page) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_offer_redemptions_user      ON offer_redemptions(user_id, claimed_at DESC);
CREATE INDEX IF NOT EXISTS idx_app_content_key             ON app_content(content_key);
`;

(async () => {
  try {
    await pool.query(SQL);
    console.log('✅ Database tables created successfully');
    process.exit(0);
  } catch (err) {
    console.error('❌ Error creating tables:', err.message);
    process.exit(1);
  }
})();