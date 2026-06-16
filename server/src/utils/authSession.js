export const buildAccessToken = (userId) => {
  const nonce = Math.random().toString(36).slice(2, 8);
  return `cp_${userId}_${Date.now()}_${nonce}`;
};

export const buildRefreshToken = (userId) => {
  const nonce = Math.random().toString(36).slice(2, 8);
  return `rp_${userId}_${Date.now()}_${nonce}`;
};

export const getUserIdFromAuthorization = (authorizationHeader) => {
  if (!authorizationHeader) return null;
  const raw = authorizationHeader.replace(/^Bearer\s+/i, '').trim();
  if (!raw) return null;

  if (/^\d+$/.test(raw)) {
    return Number(raw);
  }

  const parts = raw.split('_');
  if (parts.length < 2) return null;
  const userId = Number(parts[1]);
  return Number.isFinite(userId) ? userId : null;
};

