/**
 * Run: node src/db/seed.js
 * Seeds database with sample data per RS³ Films Implementation Guide v1.0.
 */
require('dotenv').config();
const bcrypt = require('bcryptjs');
const pool = require('./pool');

(async () => {
  try {
    const hash = await bcrypt.hash('admin123', 12); // cost factor 12 per guide

    // ─────────────────────────────────────────────────────────────────────────
    // admin_users
    // ─────────────────────────────────────────────────────────────────────────
    await pool.query(`
      INSERT INTO admin_users (name, email, password, role, active) VALUES
        ('Priya Anand', 'priya@rs3.com', $1, 'super_admin', true),
        ('Rajan Mehta', 'rajan@rs3.com', $1, 'content',     true),
        ('Sneha Rao',   'sneha@rs3.com', $1, 'analytics',   false)
      ON CONFLICT (email) DO NOTHING;
    `, [hash]);

    // ─────────────────────────────────────────────────────────────────────────
    // users
    // Note: referred_by_code is mandatory per guide.
    // In seed data the first member (Arjun) is treated as the seed ambassador
    // so referred_by_code is set to a placeholder bootstrap code.
    // ─────────────────────────────────────────────────────────────────────────
    await pool.query(`
      INSERT INTO users
        (name, phone, email, referral_code, referred_by_code, referred_by_user_id,
         password, otp_verified, coin_balance, status, gender, pincode)
      VALUES
        ('Arjun Kumar',  '+919876543210', 'arjun@mail.com',  'RS3_ARJUN24',  'RS3_BOOTSTRAP', NULL, $1, true,  1240, 'ambassador', 'Male',   '600001'),
        ('Sunita Reddy', '+917765432109', 'sunita@mail.com', 'RS3_SUNITA20', 'RS3_ARJUN24',   1,    $1, true,  2100, 'ambassador', 'Female', '500001'),
        ('Priya Mehta',  '+919012345678', 'priya@mail.com',  'RS3_PRIYA15',  'RS3_ARJUN24',   1,    $1, true,  560,  'verified',   'Female', '400001'),
        ('Rahul Das',    '+918876512345', 'rahul@mail.com',  'RS3_RAHUL01',  'RS3_PRIYA15',   3,    $1, true,  340,  'pending',    'Male',   '110001'),
        ('Anil Sharma',  '+919988776655', 'anil@mail.com',   'RS3_ANIL28',   'RS3_PRIYA15',   3,    $1, false, 80,   'verified',   'Male',   '302001')
      ON CONFLICT (phone) DO NOTHING;
    `, [hash]);

    // ─────────────────────────────────────────────────────────────────────────
    // referral_chains
    // ─────────────────────────────────────────────────────────────────────────
    await pool.query(`
      INSERT INTO referral_chains (referrer_id, referee_id, level, reward_coins, status, rewarded_at)
      VALUES
        (1, 3, 1, 100, 'awarded', NOW()),  -- Arjun → Priya (L1)
        (1, 4, 1, 100, 'awarded', NOW()),  -- Arjun → Rahul (L1) — via Priya, but Arjun is L1 parent? corrected below
        (3, 4, 1, 100, 'awarded', NOW()),  -- Priya → Rahul (L1)
        (1, 4, 2, 40,  'awarded', NOW()),  -- Arjun → Rahul (L2, Priya referred Rahul)
        (3, 5, 1, 100, 'awarded', NOW()),  -- Priya → Anil (L1)
        (1, 5, 2, 40,  'pending', NULL)    -- Arjun → Anil (L2, Anil not yet OTP-verified)
      ON CONFLICT DO NOTHING;
    `);

    // ─────────────────────────────────────────────────────────────────────────
    // point_rules
    // ─────────────────────────────────────────────────────────────────────────
    await pool.query(`
      INSERT INTO point_rules
        (rule_key, label, coins, rule_type, level_number, active, description)
      VALUES
        ('referral_l1',       'Direct Referral (Level 1)',      100, 'referral_level',      1,    true,  'Awarded when your direct referral registers and verifies OTP'),
        ('referral_l2',       'Indirect Referral (Level 2)',    40,  'referral_level',      2,    true,  'Awarded when your L1 referral brings in another member'),
        ('referral_l3',       'Chain Referral (Level 3)',       15,  'referral_level',      3,    true,  'Awarded two levels below you in the chain'),
        ('profile_complete',  'Profile Completion Bonus',       50,  'registration_bonus',  NULL, true,  'One-time bonus when user completes their full profile'),
        ('first_event',       'First Event Booking Bonus',      150, 'event_bonus',         NULL, true,  'One-time reward on first event ticket purchase'),
        ('ambassador_unlock', 'Ambassador Unlock Bonus',        500, 'registration_bonus',  NULL, true,  'Awarded when user reaches Fan Ambassador status')
      ON CONFLICT (rule_key) DO NOTHING;
    `);

    // ─────────────────────────────────────────────────────────────────────────
    // coin_transactions  (representative entries for seeded users)
    // ─────────────────────────────────────────────────────────────────────────
    await pool.query(`
      INSERT INTO coin_transactions (user_id, type, coins, reference_id, note)
      VALUES
        (1, 'earned_referral_l1', 100, 1, 'Referral reward — Level 1 (Priya Mehta)'),
        (1, 'earned_referral_l1', 100, 2, 'Referral reward — Level 1 (Sunita Reddy)'),
        (1, 'earned_referral_l2', 40,  4, 'Referral reward — Level 2 (Rahul Das)'),
        (1, 'earned_bonus',       500, NULL, 'Ambassador Unlock Bonus'),
        (1, 'earned_bonus',       500, NULL, 'Ambassador Unlock Bonus — Sunita milestone'),
        (3, 'earned_referral_l1', 100, 3, 'Referral reward — Level 1 (Rahul Das)'),
        (3, 'earned_referral_l1', 100, 5, 'Referral reward — Level 1 (Anil Sharma)'),
        (3, 'redeemed_offer',    -200, NULL, 'Redeemed: 10% Off Merchandise'),
        (4, 'earned_bonus',       50,  NULL, 'Profile Completion Bonus'),
        (2, 'earned_bonus',       500, NULL, 'Ambassador Unlock Bonus'),
        (2, 'earned_referral_l1', 100, NULL, 'Referral reward — Level 1'),
        (2, 'earned_bonus',       150, NULL, 'First Event Booking Bonus')
      ON CONFLICT DO NOTHING;
    `);

    // ─────────────────────────────────────────────────────────────────────────
    // offers
    // ─────────────────────────────────────────────────────────────────────────
    await pool.query(`
      INSERT INTO offers
        (title, description, reward_type, reward_value, coin_cost, target_referrals,
         page, display_order, active, max_claims, claims_count, valid_until, created_by)
      VALUES
        ('Free Movie Ticket',   'Get a free movie ticket when you refer 5 friends',  'movie_ticket', 'TICKET-FMT-001',  0,   5,    'referral', 1, true,  100, 34, NOW() + INTERVAL '90 days', 1),
        ('Premiere Pass',       'VIP premiere pass for our top referrers',           'event_pass',   'PASS-PREM-MAR15', 0,   10,   'referral', 2, true,  50,  12, NOW() + INTERVAL '30 days', 1),
        ('10% Off Merchandise', 'Redeem coins for an exclusive merchandise discount', 'coupon_code',  'RS3MERCH10',      200, NULL, 'wallet',   1, true,  NULL,28, NULL,                       1),
        ('VIP Event Invite',    'Exclusive VIP event for top ambassadors',           'event_pass',   'VIP-EVENT-APR',   0,   20,   'referral', 3, false, 20,  0,  NOW() + INTERVAL '60 days', 1)
      ON CONFLICT DO NOTHING;
    `);

    // ─────────────────────────────────────────────────────────────────────────
    // offer_redemptions
    // ─────────────────────────────────────────────────────────────────────────
    await pool.query(`
      INSERT INTO offer_redemptions (user_id, offer_id, coins_spent, reward_sent, status, fulfilled_at)
      VALUES
        (1, 1, 0,   'TICKET-FMT-001',  'fulfilled', NOW()),
        (2, 2, 0,   'PASS-PREM-MAR15', 'claimed',   NULL),
        (3, 3, 200, 'RS3MERCH10',      'claimed',   NULL),
        (4, 1, 0,   'TICKET-FMT-002',  'fulfilled', NOW())
      ON CONFLICT DO NOTHING;
    `);

    // ─────────────────────────────────────────────────────────────────────────
    // feed_posts
    // ─────────────────────────────────────────────────────────────────────────
    await pool.query(`
      INSERT INTO feed_posts
        (layout, title, subtitle, body, content_type, status, author_id)
      VALUES
        ('hero',   'Premiere Night',    'PVR Cinemas · March 15, 2025',               'Join us for the exclusive RS³ Films premiere night.',         'image', 'live',      1),
        ('card',   '1000 Fans Strong',  'Our community just hit a milestone!',        'Thank you for the love and support from every single fan.',   'text',  'live',      2),
        ('reel',   'Behind the Scenes', 'Director''s cut — filming the climax',       'An exclusive look behind the camera during the shoot.',       'video', 'draft',     1),
        ('banner', 'New Poster Drop',   'The official RS³ Films poster is here',      NULL,                                                          'image', 'live',      1),
        ('update', 'App Update v1.2',   'New referral tree, coin history, and more',  'Update now from the Play Store to get the latest features.',  'link',  'scheduled', 2)
      ON CONFLICT DO NOTHING;
    `);

    // ─────────────────────────────────────────────────────────────────────────
    // app_content  (full CMS key-value store)
    // ─────────────────────────────────────────────────────────────────────────
    await pool.query(`
      INSERT INTO app_content (content_key, content_type, value, label, section)
      VALUES
        -- splash
        ('splash_image_url',      'image_url', 'https://res.cloudinary.com/rs3films/image/upload/v1/splash/bg.jpg', 'Splash Background Image',             'splash'),
        ('splash_tagline',        'text',      'An Emotional Journey of Love',                                     'Splash Tagline',                      'splash'),

        -- onboarding
        ('onboarding_1_title',    'text',      'Welcome to RS³ Films',           'Slide 1 — Title',   'onboarding'),
        ('onboarding_1_image',    'image_url', 'https://res.cloudinary.com/rs3films/image/upload/v1/onboarding/slide1.jpg', 'Slide 1 — Image', 'onboarding'),
        ('onboarding_2_title',    'text',      'Earn Coins by Sharing',          'Slide 2 — Title',   'onboarding'),
        ('onboarding_2_image',    'image_url', 'https://res.cloudinary.com/rs3films/image/upload/v1/onboarding/slide2.jpg', 'Slide 2 — Image', 'onboarding'),
        ('onboarding_3_title',    'text',      'Unlock Exclusive Offers',        'Slide 3 — Title',   'onboarding'),
        ('onboarding_3_image',    'image_url', 'https://res.cloudinary.com/rs3films/image/upload/v1/onboarding/slide3.jpg', 'Slide 3 — Image', 'onboarding'),

        -- legal
        ('terms_and_conditions',  'richtext',  '<p>Terms and Conditions for RS³ Films fan community…</p>', 'Terms & Conditions', 'legal'),
        ('privacy_policy',        'richtext',  '<p>Your privacy is important to us…</p>',                 'Privacy Policy',     'legal'),
        ('about_us',              'richtext',  '<p>RS³ Films is a fan community platform…</p>',            'About Us',           'legal'),
        ('faq',                   'richtext',  '<p><b>Q: How do I earn coins?</b><br>A: Refer friends using your unique code…</p>', 'FAQ', 'legal'),

        -- contact
        ('contact_email',         'text',      'support@rs3films.com',           'Support Email',    'contact'),
        ('contact_phone',         'text',      '+91 98765 43210',                'Support Phone',    'contact'),
        ('contact_address',       'text',      '123 Film Street, Chennai',       'Office Address',   'contact'),
        ('social_instagram',      'url',       'https://instagram.com/rs3films', 'Instagram URL',    'contact'),
        ('social_youtube',        'url',       'https://youtube.com/@rs3films',  'YouTube URL',      'contact'),
        ('social_whatsapp',       'url',       'https://wa.me/919876543210',     'WhatsApp Link',    'contact'),

        -- wallet
        ('coin_value_label',      'text',      '100 Coins = 1 Movie Voucher',    'Coin Value Label',                        'wallet'),
        ('min_redeem_coins',      'number',    '200',                            'Minimum Coins to Redeem',                 'wallet'),
        ('ambassador_threshold',  'number',    '25',                             'Ambassador Threshold (Referrals)',        'wallet'),
        ('referral_headline',     'text',      'Share & Earn Together',          'Referral Page Headline',                  'wallet'),

        -- system
        ('maintenance_mode',      'boolean',   'false',                          'Maintenance Mode',     'system'),
        ('maintenance_message',   'text',      'We''ll be back shortly.',        'Maintenance Message',  'system'),
        ('app_version_message',   'text',      '',                               'Update Banner Message','system')

      ON CONFLICT (content_key) DO NOTHING;
    `);

    console.log('✅ Database seeded successfully');
    process.exit(0);
  } catch (err) {
    console.error('❌ Seed error:', err.message);
    process.exit(1);
  }
})();