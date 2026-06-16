import pool from '../db.js';
import bcrypt from 'bcrypt';
import { buildAccessToken, buildRefreshToken } from '../utils/authSession.js';
import { applyWalletTransaction, ensureWallet, getWalletSummary } from '../services/walletService.js';
import { generateReferralCode, handleReferralRewards } from '../services/referralService.js';

const normalizePhone = (phone) => {
  const digits = String(phone || '').replace(/\D/g, '');
  if (!digits) return '';
  if (digits.length === 10) return `+91${digits}`;
  if (digits.length === 12 && digits.startsWith('91')) return `+${digits}`;
  return String(phone).trim();
};

// Helper to trim and remove surrounding double quotes if present
const sanitize = (val) => {
  if (val === undefined || val === null) return null;
  const s = String(val).trim();
  const stripped = s.replace(/^"|"$/g, '');
  return stripped === '' ? null : stripped;
};

const mapUserForResponse = (userRow, coins) => ({
  id: String(userRow.id),
  name: (userRow.name || '').replace(/^"|"$/g, ''),
  email: (userRow.email || '').replace(/^"|"$/g, ''),
  phone: (userRow.phone || '').replace(/^"|"$/g, ''),
  referralCode: (userRow.referral_code || '').replace(/^"|"$/g, ''),
  isVerified: true,
  isAmbassador: Number(coins || 0) >= 3000,
  coins: Number(coins || 0),
  selfieUrl: (userRow.selfie_url || '').replace(/^"|"$/g, '') || null,
});

export const registerUser = async (req, res) => {
  console.log('Register request body:', req.body);
  console.log('Register selfie file:', req.file);

  // Extract raw values (some older clients send quoted strings)
  const {
    name: rawName,
    phone: rawPhone,
    email: rawEmail,
    gender: rawGender,
    dob: rawDob,
    referral_code: rawReferralCode,
    referralCode: rawReferralCodeCamel,
    address_line: rawAddressLine,
    city: rawCity,
    state: rawState,
    pincode: rawPincode,
    password: rawPassword,
    confirmPassword: rawConfirmPassword,
  } = req.body || {};

  // Helper to trim and remove surrounding double quotes if present
  const sanitize = (val) => {
    if (val === undefined || val === null) return null;
    const s = String(val).trim();
    const stripped = s.replace(/^"|"$/g, '');
    return stripped === '' ? null : stripped;
  };

  const name = sanitize(rawName);
  const phone = sanitize(rawPhone);
  const email = sanitize(rawEmail);
  const gender = sanitize(rawGender);
  const dob = sanitize(rawDob);
  // Accept either snake_case or camelCase from older clients
  const referral_code = sanitize(rawReferralCode) || sanitize(rawReferralCodeCamel);
  const address_line = sanitize(rawAddressLine);
  const city = sanitize(rawCity);
  const state = sanitize(rawState);
  const pincode = sanitize(rawPincode);
  const password = sanitize(rawPassword);
  const confirmPassword = sanitize(rawConfirmPassword);
  // Handle selfie file
  let selfieUrl = null;
  if (req.file) {
    selfieUrl = `/media/selfies/${req.file.filename}`;
    console.log('Selfie saved at:', selfieUrl);
  } else {
    console.error('Selfie image is required');
    return res.status(400).json({ success: false, message: 'Selfie image is required', data: null });
  }

  if (!name || !phone || !email || !password || !confirmPassword) {
    console.error('Required fields missing:', { name, phone, email, password, confirmPassword });
    return res.status(400).json({ success: false, message: 'Required fields missing', data: null });
  }
  if (password !== confirmPassword) {
    console.error('Passwords do not match');
    return res.status(400).json({ success: false, message: 'Passwords do not match', data: null });
  }

  try {
    const normalizedPhone = normalizePhone(phone);
    const cleanEmail = String(email).trim().toLowerCase();
    // Accept either snake_case (referral_code) or camelCase (referralCode) from older clients
    const referredByCode = String(referral_code || req.body.referralCode || '').trim() || null;

    const duplicate = await pool.query(
      `
        SELECT id
        FROM users
        WHERE email = $1 OR phone = $2
        LIMIT 1
      `,
      [cleanEmail, normalizedPhone],
    );
    if (duplicate.rows.length > 0) {
      return res.status(409).json({
        success: false,
        message: 'User already exists with this email or phone',
        data: null,
      });
    }

    const hashedPassword = password; // Store plain text password to match app
    const generatedCode = await generateReferralCode();

    const result = await pool.query(
      `
        INSERT INTO users (
          name, phone, email, gender, dob, referral_code, address_line, city, state, pincode, password, selfie_url
        )
        VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12)
        RETURNING id, name, phone, email, referral_code, selfie_url
      `,
      [
        name.trim().replace(/^"|"$/g, ''),
        normalizedPhone.replace(/^"|"$/g, ''),
        cleanEmail.replace(/^"|"$/g, ''),
        (gender || '').replace(/^"|"$/g, '') || null,
        (dob || '').replace(/^"|"$/g, '') || null,
        generatedCode,
        (address_line || '').replace(/^"|"$/g, '') || null,
        (city || '').replace(/^"|"$/g, '') || null,
        (state || '').replace(/^"|"$/g, '') || null,
        (pincode || '').replace(/^"|"$/g, '') || null,
        hashedPassword,
        selfieUrl,
      ],
    );
    const user = result.rows[0];

    await ensureWallet(user.id);

    // Handle multi-level referral rewards
    if (referredByCode) {
      try {
        await handleReferralRewards(user.id, referredByCode);
      } catch (error) {
        console.error('Error processing referral rewards:', error);
        // Don't fail registration if referral processing fails
      }
    }

    const wallet = await getWalletSummary(user.id);
    const accessToken = buildAccessToken(user.id);
    const refreshToken = buildRefreshToken(user.id);

    return res.status(201).json({
      success: true,
      message: 'Registration successful',
      data: {
        user: mapUserForResponse(user, wallet?.coins || 0),
        accessToken,
        refreshToken,
      },
    });
  } catch (err) {
    console.error('Registration error:', err);
    return res.status(500).json({ success: false, message: 'Server error', data: null });
  }
};

export const loginUser = async (req, res) => {
  const { identifier: rawIdentifier, password: rawPassword } = req.body || {};
  const identifier = sanitize(rawIdentifier) || null;
  const password = sanitize(rawPassword) || null;
  console.log('Login request:', { identifier: rawIdentifier, password: rawPassword });
  if (!identifier || !password) {
    return res.status(400).json({ success: false, message: 'All fields required', data: null });
  }
  try {
    const cleanIdentifier = String(identifier).trim().toLowerCase();
    const normalizedPhone = normalizePhone(identifier);
    console.log('Login cleanIdentifier:', cleanIdentifier);
    console.log('Login normalizedPhone:', normalizedPhone);
    const result = await pool.query(
      `
        SELECT
          u.*,
          COALESCE(w.coins, 0) AS wallet_coins
        FROM users u
        LEFT JOIN wallets w ON w.user_id = u.id
        WHERE u.email = $1 OR u.phone = $2 OR u.phone = $3
        LIMIT 1
      `,
      [cleanIdentifier, normalizedPhone, identifier],
    );
    console.log('Login DB result:', result.rows);
    if (result.rows.length === 0) {
      console.error('No user found for login:', { cleanIdentifier, normalizedPhone, identifier });
      return res.status(401).json({ success: false, message: 'Invalid credentials', data: null });
    }

    const user = result.rows[0];
    // stored password may have surrounding quotes from older registrations
    const storedPasswordRaw = user.password == null ? '' : String(user.password);
    const storedPasswordStripped = storedPasswordRaw.replace(/^"|"$/g, '');
    const inputPasswordRaw = rawPassword == null ? '' : String(rawPassword);
    const inputPasswordStripped = password; // already sanitized

    const match = (
      inputPasswordStripped === storedPasswordStripped || // stripped vs stripped
      inputPasswordRaw === storedPasswordRaw // exact raw match
    );
    console.log('Password match:', match);
    if (!match) {
      console.error('Password mismatch for user:', user);
      return res.status(401).json({ success: false, message: 'Invalid credentials', data: null });
    }

    await ensureWallet(user.id);

    const accessToken = buildAccessToken(user.id);
    const refreshToken = buildRefreshToken(user.id);
    return res.json({
      success: true,
      message: 'Login successful',
      data: {
        user: mapUserForResponse(user, user.wallet_coins || 0),
        accessToken,
        refreshToken,
        referredBy: user.referred_by_code || null,
      },
    });
  } catch (err) {
    console.error('[LOGIN] Error:', err);
    return res.status(500).json({ success: false, message: 'Server error', data: null });
  }
};

export const verifyOtp = async (req, res) => {
  const { phone, code } = req.body || {};
  if (!phone || !code) {
    return res.status(400).json({ success: false, message: 'Phone and code are required', data: null });
  }
  if (!/^\d{4,6}$/.test(String(code))) {
    return res.status(400).json({ success: false, message: 'Invalid OTP code', data: null });
  }
  return res.json({
    success: true,
    message: 'OTP verified',
    data: {
      phone: String(phone),
      verified: true,
    },
  });
};
