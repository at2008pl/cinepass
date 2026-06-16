import pool from './db.js';

export async function initializeDatabase() {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id SERIAL PRIMARY KEY,
      name VARCHAR(100) NOT NULL,
      phone VARCHAR(20) NOT NULL,
      email VARCHAR(100) UNIQUE NOT NULL,
      gender VARCHAR(20),
      dob DATE,
      referral_code VARCHAR(50),
      referred_by_code VARCHAR(50),
      address_line VARCHAR(255),
      city VARCHAR(100),
      state VARCHAR(100),
      pincode VARCHAR(20),
      password VARCHAR(255) NOT NULL,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
  `);

  await pool.query(`
    ALTER TABLE users
      ADD COLUMN IF NOT EXISTS referred_by_code VARCHAR(50);
  `);

  await pool.query(`
    CREATE UNIQUE INDEX IF NOT EXISTS idx_users_referral_code
    ON users (referral_code)
    WHERE referral_code IS NOT NULL;
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS wallets (
      user_id INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
      coins INT NOT NULL DEFAULT 0,
      total_earned INT NOT NULL DEFAULT 0,
      total_spent INT NOT NULL DEFAULT 0,
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS wallet_transactions (
      id SERIAL PRIMARY KEY,
      user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      type VARCHAR(40) NOT NULL,
      coins INT NOT NULL,
      description TEXT NOT NULL DEFAULT '',
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS referrals (
      id SERIAL PRIMARY KEY,
      referrer_user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      referred_user_id INT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
      status VARCHAR(20) NOT NULL DEFAULT 'confirmed',
      coins_earned INT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS events (
      id VARCHAR(64) PRIMARY KEY,
      title VARCHAR(255) NOT NULL,
      description TEXT,
      banner_url TEXT,
      teaser_url TEXT,
      date DATE NOT NULL,
      venue VARCHAR(255) NOT NULL,
      city VARCHAR(100) NOT NULL,
      organizer_name VARCHAR(120) NOT NULL DEFAULT 'RS3 Films',
      ticket_price NUMERIC(10,2) NOT NULL DEFAULT 0,
      discount_for_registered INT NOT NULL DEFAULT 5,
      coins_per_referral INT NOT NULL DEFAULT 50,
      attendance_bonus INT NOT NULL DEFAULT 100,
      status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS showtimes (
      id VARCHAR(64) PRIMARY KEY,
      event_id VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
      start_time TIMESTAMPTZ NOT NULL,
      format VARCHAR(40) NOT NULL,
      price NUMERIC(10,2) NOT NULL,
      available_seats INT NOT NULL DEFAULT 150,
      priority_seats INT NOT NULL DEFAULT 40
    );
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS event_registrations (
      id SERIAL PRIMARY KEY,
      event_id VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
      user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      has_priority BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      UNIQUE(event_id, user_id)
    );
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS tickets (
      id VARCHAR(64) PRIMARY KEY,
      user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      event_id VARCHAR(64) NOT NULL REFERENCES events(id) ON DELETE CASCADE,
      showtime_id VARCHAR(64) NOT NULL REFERENCES showtimes(id) ON DELETE CASCADE,
      seat_labels TEXT[] NOT NULL,
      total_price NUMERIC(10,2) NOT NULL,
      coins_used INT NOT NULL DEFAULT 0,
      discount NUMERIC(10,2) NOT NULL DEFAULT 0,
      final_price NUMERIC(10,2) NOT NULL,
      qr_code TEXT NOT NULL,
      status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
  `);

  await pool.query(`
    INSERT INTO wallets (user_id)
    SELECT id FROM users
    ON CONFLICT (user_id) DO NOTHING;
  `);

  await pool.query(`
    INSERT INTO events (
      id, title, description, banner_url, teaser_url, date, venue, city, organizer_name, ticket_price,
      discount_for_registered, coins_per_referral, attendance_bonus, status
    )
    VALUES (
      'event-kalki-001',
      'Yugaal Habba',
      'An emotional journey of love',
      '/media/images/hero-main.jpeg',
      'https://youtu.be/c6KhdWGiZzs?si=vy-ki_lUmOSjSuUz',
      '2026-06-30',
      'RS3 Grand Arena',
      'Bengaluru',
      'RS3 Films',
      450,
      5,
      50,
      150,
      'UPCOMING'
    )
    ON CONFLICT (id) DO UPDATE
    SET
      title = EXCLUDED.title,
      description = EXCLUDED.description,
      banner_url = EXCLUDED.banner_url,
      teaser_url = EXCLUDED.teaser_url,
      date = EXCLUDED.date,
      venue = EXCLUDED.venue,
      city = EXCLUDED.city,
      organizer_name = EXCLUDED.organizer_name,
      ticket_price = EXCLUDED.ticket_price,
      discount_for_registered = EXCLUDED.discount_for_registered,
      coins_per_referral = EXCLUDED.coins_per_referral,
      attendance_bonus = EXCLUDED.attendance_bonus,
      status = EXCLUDED.status,
      updated_at = NOW();
  `);

  await pool.query(`
    INSERT INTO showtimes (id, event_id, start_time, format, price, available_seats, priority_seats)
    VALUES
      ('show-1000', 'event-kalki-001', '2026-06-30T10:00:00+05:30', '4DX', 500, 120, 35),
      ('show-1330', 'event-kalki-001', '2026-06-30T13:30:00+05:30', 'IMAX', 600, 120, 35),
      ('show-1700', 'event-kalki-001', '2026-06-30T17:00:00+05:30', '2D', 400, 120, 35)
    ON CONFLICT (id) DO UPDATE
    SET
      event_id = EXCLUDED.event_id,
      start_time = EXCLUDED.start_time,
      format = EXCLUDED.format,
      price = EXCLUDED.price,
      available_seats = EXCLUDED.available_seats,
      priority_seats = EXCLUDED.priority_seats;
  `);

  // Add referral system columns and tables
  await pool.query(`
    ALTER TABLE users ADD COLUMN IF NOT EXISTS referred_by_user_id INTEGER REFERENCES users(id);
  `);

  await pool.query(`
    ALTER TABLE users ADD COLUMN IF NOT EXISTS selfie_url TEXT;
  `);

  await pool.query(`
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
  `);

  await pool.query(`
    CREATE INDEX IF NOT EXISTS idx_referral_chains_referrer ON referral_chains(referrer_id);
  `);

  await pool.query(`
    CREATE INDEX IF NOT EXISTS idx_referral_chains_referred ON referral_chains(referred_id);
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS transactions (
      id SERIAL PRIMARY KEY,
      user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      amount INTEGER NOT NULL,
      type VARCHAR(50) NOT NULL,
      description TEXT,
      related_user_id INTEGER REFERENCES users(id),
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
  `);

  await pool.query(`
    CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id);
  `);

  await pool.query(`
    CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
  `);

  console.log('[DB] Core schema initialized');
}

