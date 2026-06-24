/**
 * Rewrites media URLs so mobile clients never receive localhost links.
 * Uploads stored as http://localhost:6055/uploads/... are unreachable from phones.
 */
function mediaBaseUrl() {
  return (process.env.MEDIA_BASE_URL || 'http://117.198.99.60:6055').replace(/\/$/, '');
}

function resolveMediaUrl(url) {
  if (!url || typeof url !== 'string') return url;

  const base = mediaBaseUrl();

  if (url.startsWith('/')) {
    return `${base}${url}`;
  }

  try {
    const parsed = new URL(url);
    if (parsed.hostname === 'localhost' || parsed.hostname === '127.0.0.1') {
      return `${base}${parsed.pathname}${parsed.search}`;
    }
  } catch {
    // not a valid absolute URL — return as-is
  }

  return url;
}

function normalizeFeedRow(row) {
  if (!row) return row;
  return {
    ...row,
    media_url: resolveMediaUrl(row.media_url),
    thumbnail_url: resolveMediaUrl(row.thumbnail_url),
  };
}

function normalizeFeedRows(rows) {
  return rows.map(normalizeFeedRow);
}

module.exports = { resolveMediaUrl, normalizeFeedRow, normalizeFeedRows, mediaBaseUrl };
