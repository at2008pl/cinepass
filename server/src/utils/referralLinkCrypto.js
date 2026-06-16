import crypto from 'crypto';

const REFERRAL_CODE_REGEX = /^RS3_[A-Z0-9]{6}$/;

function getSecret() {
  return process.env.REFERRAL_LINK_SECRET || process.env.JWT_SECRET || 'cinepass-dev-referral-secret-change-me';
}

function getKey() {
  const secret = getSecret();
  return crypto.createHash('sha256').update(secret).digest();
}

export function normalizeReferralCode(code) {
  if (!code || typeof code !== 'string') return null;
  const trimmed = code.trim().toUpperCase();
  if (!REFERRAL_CODE_REGEX.test(trimmed)) return null;
  return trimmed;
}

export function encryptReferralCode(referralCode) {
  const normalized = normalizeReferralCode(referralCode);
  if (!normalized) return null;

  const key = getKey();
  const iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv('aes-256-gcm', key, iv);
  const encrypted = Buffer.concat([cipher.update(normalized, 'utf8'), cipher.final()]);
  const tag = cipher.getAuthTag();

  // Compact token format: base64url(iv + tag + ciphertext)
  // This avoids dot-separated segments so chat apps are more likely to linkify the full URL.
  const packed = Buffer.concat([iv, tag, encrypted]);
  const token = packed.toString('base64url');

  return token;
}

export function decryptReferralToken(token) {
  if (!token || typeof token !== 'string') return null;

  try {
    let iv;
    let tag;
    let encrypted;

    // Backward compatibility for old dotted token format: iv.tag.cipher
    if (token.includes('.')) {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const [ivPart, tagPart, dataPart] = parts;
      iv = Buffer.from(ivPart, 'base64url');
      tag = Buffer.from(tagPart, 'base64url');
      encrypted = Buffer.from(dataPart, 'base64url');
    } else {
      // New compact token format: base64url(iv + tag + ciphertext)
      const packed = Buffer.from(token, 'base64url');
      if (packed.length <= 28) return null; // 12-byte IV + 16-byte tag + >=1 byte data

      iv = packed.subarray(0, 12);
      tag = packed.subarray(12, 28);
      encrypted = packed.subarray(28);
    }

    const key = getKey();
    const decipher = crypto.createDecipheriv('aes-256-gcm', key, iv);
    decipher.setAuthTag(tag);

    const decrypted = Buffer.concat([decipher.update(encrypted), decipher.final()]).toString('utf8');
    return normalizeReferralCode(decrypted);
  } catch (_error) {
    return null;
  }
}
