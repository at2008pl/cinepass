const router = require('express').Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const multer = require('multer');
const path = require('path');
const pool = require('../db/pool');

// Default multer (memory) for light endpoints; disk storage for register (selfie)
const upload = multer();
const registerStorage = multer.diskStorage({
  destination: 'uploads/',
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname) || '.jpg';
    cb(null, `selfie-${Date.now()}-${Math.random().toString(36).slice(2)}${ext}`);
  },
});
const registerUpload = multer({ storage: registerStorage, limits: { fileSize: 20 * 1024 * 1024 } });

// Strip surrounding JSON quotes that Retrofit/Gson adds to @Part String fields
const s = (v) => v == null ? null : String(v).replace(/^"|"$/g, '').trim();

function toAuthPayload(user, accessToken, refreshToken) {
  return {
    success: true,
    message: 'OK',
    data: {
      accessToken,
      refreshToken,
      user: {
        id: String(user.id),
        name: user.name || 'User',
        email: user.email || '',
        phone: user.phone || '',
        referralCode: user.referral_code || '',
        isVerified: true,
        isAmbassador: true,
        coins: user.coin_balance || 0,
        selfieUrl: user.selfie_url || null
      }
    }
  };
}

// ── Admin Login ────────────────────────────────────────
router.post('/login', async (req, res) => {
  try {
    const { email, password, identifier } = req.body;

    // Mobile app login compatibility: identifier + password -> users table
    if (identifier && password) {
      const { rows } = await pool.query(
        'SELECT * FROM users WHERE (email = $1 OR phone = $1 OR referral_code = $1) AND status != $2 LIMIT 1',
        [identifier, 'banned']
      );
      if (!rows.length) {
        return res.status(401).json({ success: false, message: 'Invalid credentials' });
      }

      const user = rows[0];
      const valid = await bcrypt.compare(password, user.password);
      if (!valid) {
        return res.status(401).json({ success: false, message: 'Invalid credentials' });
      }

      const accessToken = jwt.sign(
        { id: user.id, phone: user.phone, role: 'user' },
        process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
      );
      const refreshToken = jwt.sign(
        { id: user.id, type: 'refresh' },
        process.env.JWT_SECRET,
        { expiresIn: '30d' }
      );

      return res.json(toAuthPayload(user, accessToken, refreshToken));
    }

    // Admin dashboard login (existing behavior)
    if (!email || !password) {
      return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'Email and password required' } });
    }

    const { rows } = await pool.query('SELECT * FROM admin_users WHERE email = $1 AND active = true', [email]);
    if (!rows.length) {
      return res.status(401).json({ error: { code: 'AUTH_FAILED', message: 'Invalid credentials' } });
    }

    const admin = rows[0];
    const valid = await bcrypt.compare(password, admin.password);
    if (!valid) {
      return res.status(401).json({ error: { code: 'AUTH_FAILED', message: 'Invalid credentials' } });
    }

    await pool.query('UPDATE admin_users SET last_login = NOW() WHERE id = $1', [admin.id]);

    const token = jwt.sign(
      { id: admin.id, email: admin.email, role: admin.role },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );

    res.json({
      token,
      admin: { id: admin.id, name: admin.name, email: admin.email, role: admin.role }
    });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── App Register ────────────────────────────────────────
router.post('/register', registerUpload.any(), async (req, res) => {
  try {
    const name          = s(req.body.name);
    const email         = s(req.body.email);
    const phone         = s(req.body.phone);
    const password      = s(req.body.password);
    const confirmPwd    = s(req.body.confirmPassword);
    const referredByCode = s(req.body.referralCode) || s(req.body.referral_code) || 'RS3_DEFAULT';
    const gender        = s(req.body.gender);
    const dob           = s(req.body.dob);
    const addressLine   = s(req.body.addressLine) || s(req.body.address_line);
    const city          = s(req.body.city);
    const stateName     = s(req.body.state);
    const pincode       = s(req.body.pincode);

    if (!name || !phone || !password) {
      return res.status(400).json({ success: false, message: 'Name, phone and password are required' });
    }
    if (confirmPwd && confirmPwd !== password) {
      return res.status(400).json({ success: false, message: 'Passwords do not match' });
    }

    const cleanPhone = phone.replace(/\s/g, '');
    const cleanEmail = email && email !== '' ? email : `${cleanPhone}@temp.com`;

    const existing = await pool.query(
      'SELECT id FROM users WHERE phone = $1 OR email = $2 LIMIT 1',
      [cleanPhone, cleanEmail]
    );
    if (existing.rows.length) {
      return res.status(409).json({ success: false, message: 'User already exists with this phone or email' });
    }

    // Save selfie if uploaded
    let selfieUrl = null;
    const selfieFile = (req.files || []).find(f => f.fieldname === 'selfie');
    if (selfieFile) {
      const host = process.env.MEDIA_BASE_URL || `${req.protocol}://${req.get('host')}`;
      selfieUrl = `${host}/uploads/${selfieFile.filename}`;
    }

    const hashedPassword = await bcrypt.hash(password, 12);
    const referralGenerated = 'RS3_' + Math.random().toString(36).slice(2, 8).toUpperCase();

    // Resolve referral code → referrer user id
    let referredByUserId = null;
    if (referredByCode && referredByCode !== 'RS3_DEFAULT') {
      const refResult = await pool.query(
        'SELECT id FROM users WHERE referral_code = $1 LIMIT 1',
        [referredByCode]
      );
      if (refResult.rows.length) referredByUserId = refResult.rows[0].id;
    }

    const { rows } = await pool.query(
      `INSERT INTO users
         (name, phone, email, password, referral_code, referred_by_code, referred_by_user_id,
          gender, dob, address_line, city, state, pincode, selfie_url, otp_verified, status)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,true,'active')
       RETURNING *`,
      [
        name, cleanPhone, cleanEmail, hashedPassword, referralGenerated, referredByCode,
        referredByUserId, gender || null, dob || null, addressLine || null,
        city || null, stateName || null, pincode || null, selfieUrl
      ]
    );

    const newUser = rows[0];

    // ── Build referral chains and award coins ──────────────
    if (referredByUserId) {
      try {
        const rulesRes = await pool.query(
          `SELECT level_number, coins FROM point_rules
           WHERE rule_type = 'referral_level' AND active = true ORDER BY level_number`
        );
        const coinsPerLevel = {};
        rulesRes.rows.forEach(r => { coinsPerLevel[r.level_number] = parseInt(r.coins); });

        let ancestorId = referredByUserId;
        let level = 1;
        while (ancestorId && level <= 5) {
          const dup = await pool.query(
            'SELECT id FROM referral_chains WHERE referrer_id=$1 AND referee_id=$2 AND level=$3',
            [ancestorId, newUser.id, level]
          );
          if (!dup.rows.length) {
            const reward = coinsPerLevel[level] || 0;
            const chainRes = await pool.query(
              `INSERT INTO referral_chains (referrer_id, referee_id, level, reward_coins, status, rewarded_at)
               VALUES ($1,$2,$3,$4,$5,NOW()) RETURNING id`,
              [ancestorId, newUser.id, level, reward, reward > 0 ? 'awarded' : 'pending']
            );
            if (reward > 0) {
              const chainId = chainRes.rows[0].id;
              await pool.query('UPDATE users SET coin_balance = coin_balance + $1 WHERE id = $2', [reward, ancestorId]);
              await pool.query(
                `INSERT INTO coin_transactions (user_id, type, coins, reference_id, note)
                 VALUES ($1,$2,$3,$4,$5)`,
                [ancestorId, `earned_referral_l${level}`, reward, chainId,
                 `Referral reward — Level ${level} (${newUser.name})`]
              );
            }
          }
          const parent = await pool.query('SELECT referred_by_user_id FROM users WHERE id=$1', [ancestorId]);
          ancestorId = parent.rows[0]?.referred_by_user_id ?? null;
          level++;
        }
      } catch (chainErr) {
        console.error('Referral chain build error:', chainErr.message);
        // Non-fatal — registration still succeeds
      }
    }

    const accessToken = jwt.sign(
      { id: newUser.id, phone: newUser.phone, role: 'user' },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );
    const refreshToken = jwt.sign(
      { id: newUser.id, type: 'refresh' },
      process.env.JWT_SECRET,
      { expiresIn: '30d' }
    );

    res.status(201).json(toAuthPayload(newUser, accessToken, refreshToken));
  } catch (err) {
    console.error('--- /register error ---');
    console.error('Request body:', req.body);
    console.error('Files:', req.files);
    console.error('Error:', err);
    console.error('Stack:', err.stack);
    res.status(500).json({ success: false, message: err.message });
  }
});

// ── App OTP Send (mock) ────────────────────────────────
router.post('/app/otp/send', async (req, res) => {
  try {
    const { phone } = req.body;
    if (!phone) {
      return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'Phone number required' } });
    }
    // In production, integrate Twilio/MSG91 here
    console.log(`📱 OTP sent to ${phone}: 123456 (mock)`);
    res.json({ message: 'OTP sent successfully' });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

// ── App OTP Verify ─────────────────────────────────────
router.post('/app/otp/verify', async (req, res) => {
  try {
    const { phone, otp } = req.body;
    if (!phone || !otp) {
      return res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: 'Phone and OTP required' } });
    }

    // Mock OTP verification (accept 123456)
    if (otp !== '123456') {
      return res.status(401).json({ error: { code: 'INVALID_OTP', message: 'Invalid or expired OTP' } });
    }

    // Find or create user
    let { rows } = await pool.query('SELECT * FROM users WHERE phone = $1', [phone.replace(/\s/g, '')]);
    let user;

    if (!rows.length) {
      const code = 'RS3_' + Date.now().toString(36).toUpperCase();
      const hashedPassword = await bcrypt.hash(Math.random().toString(36), 12);
      const result = await pool.query(
        'INSERT INTO users (name, phone, email, password, referral_code, referred_by_code, otp_verified) VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *',
        ['New User', phone.replace(/\s/g, ''), `${phone}@temp.com`, hashedPassword, code, 'RS3_DEFAULT', true]
      );
      user = result.rows[0];
    } else {
      user = rows[0];
      await pool.query('UPDATE users SET otp_verified = true WHERE id = $1', [user.id]);
    }

    const token = jwt.sign(
      { id: user.id, phone: user.phone },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );

    res.json({ token, user: { id: user.id, name: user.name, phone: user.phone, coins: user.coin_balance } });
  } catch (err) {
    res.status(500).json({ error: { code: 'SERVER_ERROR', message: err.message } });
  }
});

module.exports = router;
