# RS³ Films — REST API Documentation

> **Base URL:** `https://api.rs3films.com/v1`  
> **Auth:** Bearer token (JWT) in `Authorization` header  
> **Content-Type:** `application/json`

---

## 1. Authentication

### POST `/auth/login`
Admin login via email + password.

**Request:**
```json
{ "email": "admin@rs3.com", "password": "secret" }
```

**Response (200):**
```json
{
  "token": "eyJhbGci...",
  "admin": { "id": 1, "name": "Priya Anand", "email": "priya@rs3.com", "role": "super_admin" }
}
```

### POST `/auth/app/otp/send`
Send OTP to mobile user.
```json
{ "phone": "+919876543210" }
```

### POST `/auth/app/otp/verify`
Verify OTP and return user token.
```json
{ "phone": "+919876543210", "otp": "123456" }
```

---

## 2. Dashboard

### GET `/dashboard/stats`
**Response:**
```json
{
  "total_members": 4821,
  "active_referrals": 1247,
  "coins_issued": 2400000,
  "offers_claimed": 386,
  "deltas": {
    "members_this_week": 142,
    "referrals_today": 89,
    "coins_today": 18400,
    "offers_today": 24
  }
}
```

### GET `/dashboard/referral-activity?days=7`
```json
{ "data": [42, 68, 55, 82, 91, 74, 89], "labels": ["Mon","Tue","Wed","Thu","Fri","Sat","Sun"] }
```

---

## 3. Members

### GET `/members?page=1&limit=20&search=arjun&status=verified`
**Response:**
```json
{
  "data": [
    {
      "id": 1, "name": "Arjun Kumar", "phone": "+91 98765 43210",
      "email": "arjun@mail.com", "coins": 1240, "referrals": 24,
      "status": "ambassador", "joined": "2025-01-10T00:00:00Z",
      "referral_code": "ARJUN24"
    }
  ],
  "total": 4821, "page": 1, "limit": 20
}
```

### GET `/members/:id`
Full member profile with referral tree.

### GET `/members/:id/referral-tree`
```json
{
  "user_id": 1,
  "direct_referrals": [
    { "id": 2, "name": "Priya Mehta", "level": 1, "coins_earned": 100 }
  ],
  "chain_depth": 3, "total_chain_members": 24
}
```

### GET `/members/:id/coin-history?page=1&limit=20`
```json
{
  "data": [
    { "id": 1, "type": "credit", "amount": 100, "rule_key": "referral_l1", "description": "Direct referral bonus", "created_at": "2025-03-01T10:00:00Z" }
  ]
}
```

---

## 4. Feed Posts

### GET `/feed?page=1&limit=20&status=live`
### POST `/feed`
```json
{
  "layout": "hero", "title": "Premiere Night", "subtitle": "PVR Mar 15",
  "type": "image", "body": "...", "link": null,
  "media_url": "https://cdn.rs3films.com/...",
  "status": "draft"
}
```
### PUT `/feed/:id`
### DELETE `/feed/:id`
### PATCH `/feed/:id/status`
```json
{ "status": "live" }
```

**Layout enum:** `hero | card | reel | banner | update | grid2`  
**Status enum:** `draft | live | scheduled`  
**Type enum:** `image | video | text | link`

---

## 5. Offers

### GET `/offers?page_filter=referral&active=true`
### POST `/offers`
```json
{
  "title": "Free Movie Ticket",
  "description": "Get a free movie ticket when you refer 5 friends",
  "reward_type": "movie_ticket",
  "reward_value": "TICKET-FMT-001",
  "coin_cost": 0,
  "target_referrals": 5,
  "page": "referral",
  "active": true,
  "max_claims": 100,
  "valid_until": "2025-04-30",
  "banner_image_url": null
}
```
### PUT `/offers/:id`
### DELETE `/offers/:id`
### PATCH `/offers/:id/toggle`
```json
{ "active": true }
```

**Reward types:** `movie_ticket | event_pass | coupon_code | merchandise | custom`  
**Page enum:** `referral | wallet | home | global`

### App endpoint: GET `/app/offers?page=referral&user_id=1`
Returns only active offers the user is eligible for, with claim status.

---

## 6. Redemptions

### GET `/redemptions?status=claimed&page=1&limit=20`
```json
{
  "data": [
    {
      "id": 1, "user_id": 1, "user_name": "Arjun Kumar",
      "offer_id": 1, "offer_title": "Free Movie Ticket",
      "reward_value": "TICKET-FMT-001",
      "status": "claimed", "claimed_at": "2025-03-02T14:00:00Z",
      "fulfilled_at": null
    }
  ]
}
```

### PATCH `/redemptions/:id/fulfill`
Mark a redemption as fulfilled (ticket delivered).
```json
{ "status": "fulfilled" }
```

### App endpoint: POST `/app/offers/:offerId/claim`
```json
{ "user_id": 1 }
```
**Response:** `{ "redemption_id": 5, "reward_value": "TICKET-FMT-002", "status": "claimed" }`

---

## 7. Points Rules

### GET `/points-rules`
```json
{
  "referral_levels": [
    { "id": 1, "rule_key": "referral_l1", "label": "Direct Referral (Level 1)", "coins": 100, "level_number": 1, "active": true, "description": "..." }
  ],
  "bonus_rules": [
    { "id": 4, "rule_key": "profile_complete", "label": "Profile Completion Bonus", "coins": 50, "rule_type": "registration_bonus", "active": true, "description": "..." }
  ]
}
```

### POST `/points-rules`
### PUT `/points-rules/:id`
### DELETE `/points-rules/:id`
### PATCH `/points-rules/:id/toggle`
```json
{ "active": false }
```

**Rule types:** `referral_level | registration_bonus | event_bonus`

---

## 8. CMS (App Content)

### GET `/cms`
Returns all CMS items grouped by section.
```json
{
  "splash": [
    { "key": "splash_image_url", "label": "Splash Background Image", "type": "image_url", "value": "https://..." }
  ],
  "onboarding": [...],
  "legal": [...],
  "contact": [...],
  "wallet": [...],
  "system": [...]
}
```

### PUT `/cms/:section`
Bulk update a section.
```json
{
  "items": [
    { "key": "splash_tagline", "value": "An Emotional Journey of Love" }
  ]
}
```

### App endpoint: GET `/app/cms/:section`
Returns CMS content for a specific section (used by Kotlin app).

### App endpoint: GET `/app/config`
Returns system config (maintenance mode, version message, ambassador threshold, etc.).

---

## 9. Admin Management

### GET `/admins`
### POST `/admins/invite`
```json
{ "name": "New Admin", "email": "new@rs3.com", "role": "content" }
```
### PUT `/admins/:id/role`
```json
{ "role": "moderator" }
```
### PATCH `/admins/:id/toggle`
```json
{ "active": false }
```

**Roles:** `super_admin | content | analytics | moderator`

---

## 10. Analytics

### GET `/analytics/overview`
```json
{
  "conversion_rate": 68,
  "avg_coins_per_member": 496,
  "total_offers_claimed": 386,
  "avg_chain_depth": 1.8,
  "churn_30d": 3.2
}
```

### GET `/analytics/growth?months=7`
```json
{
  "labels": ["Sep","Oct","Nov","Dec","Jan","Feb","Mar"],
  "members": [210, 340, 480, 620, 880, 1200, 1450],
  "referrals": [80, 140, 210, 320, 480, 760, 1000],
  "coins": [12000, 18000, 24000, 32000, 48000, 72000, 94000],
  "offers": [2, 8, 14, 28, 54, 96, 138]
}
```

---

## 11. File Upload

### POST `/upload`
Multipart form upload → returns Cloudinary URL.
```
Content-Type: multipart/form-data
Body: file=<binary>, folder="feed|offers|cms"
```
**Response:** `{ "url": "https://res.cloudinary.com/rs3/..." }`

---

## Error Format
All errors follow:
```json
{ "error": { "code": "VALIDATION_ERROR", "message": "Title is required", "field": "title" } }
```

**HTTP Codes:** `200` OK, `201` Created, `400` Bad Request, `401` Unauthorized, `403` Forbidden, `404` Not Found, `409` Conflict, `500` Server Error

---

## Database Tables (PostgreSQL)

| Table | Key Columns |
|-------|-------------|
| `admins` | id, name, email, password_hash, role (enum), active, last_login |
| `members` | id, name, phone, email, coins, referral_code, referred_by (FK→members), status (enum), created_at |
| `referral_chain` | id, referrer_id, referee_id, level, coins_awarded, created_at |
| `feed_posts` | id, layout, title, subtitle, type, body, link, media_url, status, author_id, created_at |
| `offers` | id, title, description, reward_type, reward_value, coin_cost, target_referrals, page, active, max_claims, claims_count, valid_until, banner_image_url |
| `redemptions` | id, user_id, offer_id, reward_value, status, claimed_at, fulfilled_at |
| `points_rules` | id, rule_key (unique), label, coins, rule_type, level_number, active, description |
| `coin_transactions` | id, user_id, type (credit/debit), amount, rule_key, offer_id, description, created_at |
| `cms_items` | key (PK), label, type, value, section |
