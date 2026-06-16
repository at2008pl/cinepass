import pool from '../db.js';

const ALLOWED_SECTIONS = new Set(['hero', 'trending', 'updates', 'trailers']);

export const listFeedPosts = async (req, res) => {
  const requestedSection = (req.query.section || '').toString().toLowerCase();
  const hasSectionFilter = ALLOWED_SECTIONS.has(requestedSection);

  const query = `
    SELECT
      id,
      slug,
      title,
      subtitle,
      description,
      section,
      media_type,
      media_url,
      thumbnail_url,
      cta_text,
      sort_order,
      is_active,
      created_at,
      updated_at
    FROM media_posts
    WHERE is_active = TRUE
      ${hasSectionFilter ? 'AND section = $1' : ''}
    ORDER BY
      CASE section
        WHEN 'hero' THEN 1
        WHEN 'trending' THEN 2
        WHEN 'updates' THEN 3
        WHEN 'trailers' THEN 4
        ELSE 99
      END,
      sort_order ASC,
      created_at DESC
  `;

  try {
    const result = hasSectionFilter
      ? await pool.query(query, [requestedSection])
      : await pool.query(query);

    return res.json({
      success: true,
      message: 'Feed loaded',
      data: {
        posts: result.rows,
      },
    });
  } catch (error) {
    console.error('[FEED] Failed to load posts:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to load feed',
      data: { posts: [] },
    });
  }
};
