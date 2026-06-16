import express from 'express';
import { decryptReferralToken } from '../utils/referralLinkCrypto.js';

const router = express.Router();

/**
 * Branded referral entrypoint
 * GET /register?r=<encryptedToken>
 * GET /register?ref=RS3_ABC123 (backward-compatible)
 */
router.get('/register', (req, res) => {
  const target = new URL('/download', `${req.protocol}://${req.get('host')}`);

  const encrypted = typeof req.query.r === 'string' ? req.query.r : '';
  const plain = typeof req.query.ref === 'string' ? req.query.ref : '';

  if (encrypted) {
    target.searchParams.set('r', encrypted);
  } else if (plain) {
    target.searchParams.set('ref', plain);
  }

  return res.redirect(302, target.toString());
});

/**
 * Short unreadable referral route
 * GET /s/<encryptedToken>
 */
router.get('/s/:token', (req, res) => {
  const rawToken = req.params.token || '';
  const token = rawToken.replace(/[)\]}>.,!?;:]+$/, '');
  const target = new URL('/download', `${req.protocol}://${req.get('host')}`);

  const decryptedCode = decryptReferralToken(token);
  if (decryptedCode) {
    // Final URL becomes readable for web/debugging while keeping shared link obfuscated
    target.searchParams.set('ref', decryptedCode);
  } else {
    // Fallback to token path if decryption fails for any reason
    target.searchParams.set('r', token);
  }

  return res.redirect(302, target.toString());
});

export default router;
