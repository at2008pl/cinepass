Place feed media files here so `/media/...` URLs can be served by the API server.

Current seeded paths from `create_media_posts_table.sql`:
- `images/hero-main.jpeg`
- `images/trending-main.jpeg`
- `images/trending-alt-1.jpeg`
- `images/update-1.jpeg`
- `images/update-2.jpeg`
- `images/poster-wide-1.jpeg`
- `images/poster-wide-2.jpeg`
- `images/rslogo.jpeg`

Note:
- The seeded trailer row currently uses a remote sample MP4 URL.
- If you add your own video file, place it in `videos/` and update `media_posts.media_url`.

You can store any image/video names you want, then update `media_posts.media_url` and `thumbnail_url` accordingly.
