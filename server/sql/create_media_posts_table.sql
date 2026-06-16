CREATE TABLE IF NOT EXISTS media_posts (
  id SERIAL PRIMARY KEY,
  slug VARCHAR(120) UNIQUE NOT NULL,
  title VARCHAR(255) NOT NULL,
  subtitle VARCHAR(255),
  description TEXT,
  section VARCHAR(20) NOT NULL CHECK (section IN ('hero', 'trending', 'updates', 'trailers')),
  media_type VARCHAR(10) NOT NULL CHECK (media_type IN ('image', 'video')),
  media_url TEXT NOT NULL,
  thumbnail_url TEXT,
  cta_text VARCHAR(80),
  sort_order INT NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_media_posts_section_order
  ON media_posts(section, sort_order, created_at DESC);

INSERT INTO media_posts (
  slug, title, subtitle, description, section, media_type, media_url, thumbnail_url, cta_text, sort_order
) VALUES
  (
    'pyaar-hero-main',
    'Pyaar',
    'An emotional journey of love',
    'Official hero poster.',
    'hero',
    'image',
    '/media/images/hero-main.jpeg',
    NULL,
    'Explore',
    1
  ),
  (
    'rayabhariyagi-main',
    'Rayabhariyagi Naaniruve',
    'Trending now',
    'Main trending visual from campaign creatives.',
    'trending',
    'image',
    '/media/images/trending-main.jpeg',
    NULL,
    'View',
    1
  ),
  (
    'rayabhariyagi-alt-1',
    'Rayabhariyagi Feature',
    'Trending now',
    'Alternate trending poster.',
    'trending',
    'image',
    '/media/images/trending-alt-1.jpeg',
    NULL,
    'View',
    2
  ),
  (
    'pyaar-update-1',
    'First Single Out Soon!',
    'Latest update',
    'New song update from the campaign.',
    'updates',
    'image',
    '/media/images/update-1.jpeg',
    NULL,
    'Read',
    1
  ),
  (
    'pyaar-update-2',
    'Bigg Boss Grand Entry',
    'Latest update',
    'Promotional update poster.',
    'updates',
    'image',
    '/media/images/update-2.jpeg',
    NULL,
    'Read',
    2
  ),
  (
    'pyaar-poster-wide-1',
    'Pyaar Official Poster',
    'Latest update',
    'Wide promotional poster.',
    'updates',
    'image',
    '/media/images/poster-wide-1.jpeg',
    NULL,
    'Read',
    3
  ),
  (
    'pyaar-trailer-preview',
    'Official Trailer',
    'YouTube',
    'Trailer playback inside app (YouTube).',
    'trailers',
    'video',
    'https://youtu.be/c6KhdWGiZzs?si=vy-ki_lUmOSjSuUz',
    '/media/images/poster-wide-2.jpeg',
    'Watch',
    1
  ),
  (
    'pyaar-trailer-preview-2',
    'Official Trailer 2',
    'YouTube',
    'Second trailer playback inside app (YouTube).',
    'trailers',
    'video',
    'https://youtu.be/x7hrdQKtmVs?si=-NSXqEwfAgoPahkr',
    '/media/images/update-1.jpeg',
    'Watch',
    2
  )
ON CONFLICT (slug) DO UPDATE
SET
  title = EXCLUDED.title,
  subtitle = EXCLUDED.subtitle,
  description = EXCLUDED.description,
  section = EXCLUDED.section,
  media_type = EXCLUDED.media_type,
  media_url = EXCLUDED.media_url,
  thumbnail_url = EXCLUDED.thumbnail_url,
  cta_text = EXCLUDED.cta_text,
  sort_order = EXCLUDED.sort_order,
  is_active = TRUE,
  updated_at = NOW();
