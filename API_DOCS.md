# Cinepass — API Reference

Base URL (dev): `http://localhost:<PORT>/api` (server root is `server/src` in this repository).

Authentication: most user endpoints require an Authorization header. The backend uses a session token parsed by `getUserIdFromAuthorization`. Send: `Authorization: Bearer <token>`.

See controllers in `server/src/controllers` for implementation details.

---

## Auth

### POST /api/auth/register
- Description: Register a new user. Supports an optional `selfie` file upload (multipart/form-data).
- Content-Type: `multipart/form-data`
- Body fields:
  - `name` (string)
  - `email` (string)
  - `phone` (string)
  - `password` (string) — if password flow used
  - `selfie` (file) — optional image file field
  - `ref` or `r` (string) — optional referral code or encrypted referral token
- Success Response (200):
```json
{ "success": true, "message": "Registered", "data": { /* user object, token maybe */ } }
```

### POST /api/auth/login
- Description: Login by phone/email + OTP or password depending on authController.
- Content-Type: `application/json`
- Body:
  - `phone` or `email`
  - `password` or `otp`
- Response:
```json
{ "success": true, "message": "Logged in", "data": { "token": "...", "user": { /* profile */ } } }
```

### POST /api/auth/verify-otp
- Description: Verify OTP sent during login/registration.
- Body: `{ "phone": "...", "otp": "1234" }`

---

## Users / Profile / Wallet / Referrals
(Controller: `server/src/controllers/usersController.js`)

All endpoints below expect `Authorization: Bearer <token>` (or `?userId=` for debug in some controller helpers).

### GET /api/users/profile
- Description: Returns the authenticated user's profile, wallet coins and referral summary.
- Response shape (data):
```json
{
  "id": "<id>",
  "name": "...",
  "email": "...",
  "phone": "...",
  "referralCode": "RS3_...",
  "isAmbassador": true|false,
  "coins": 1234,
  "selfieUrl": "...",
  "referralStats": {
     "directReferrals": 5,
     "totalEarnings": 4500,
     "byLevel": { "1": 100, "2": 40 },
     "shareLink": "https://..."
  }
}
```

### GET /api/users/wallet
- Description: Wallet summary for the authenticated user.
- Query: none
- Response data example:
```json
{ "coins": 1200, "rupeeValue": "₹120", "totalEarned": 1500, "totalSpent": 300, "totalReferrals": 7 }
```

### GET /api/users/wallet/transactions
- Description: Paginated list of wallet transactions.
- Query params: `page`, `limit`
- Response shape:
```json
{ "success": true, "data": [ { "id":"...","type":"EARNED|SPENT|REDEEMED","coins":100,"description":"...","createdAt":"..." } ], "pagination": { "total": 42, "page":1, "limit":20, "totalPages":3 } }
```

### POST /api/users/wallet/redeem
- Description: Request redemption (send coins to UPI). Body includes `coins` and `upiId`.
- Body:
```json
{ "coins": 100, "upiId": "name@bank" }
```
- Responses:
  - 200 success: `{ success: true, message: 'Coins redeemed successfully', data: { redeemedCoins, redeemedValue } }`
  - 400 for invalid/missing fields or insufficient coins.

### GET /api/users/leaderboard
- Description: Top users by wallet coins. Returns up to 20 ranks.
- Response data: list of users with `rank`, `id`, `name`, `referralCode`, `coins`.

### GET /api/users/referrals
- Description: Detailed list of the user's referrals (status, coins earned per referral).
- Response data: `{ referralCode, referralLink, totalReferrals, totalCoinsEarned, referrals: [ ... ] }`

### GET /api/users/referrals/tree
- Description: Referral tree structure. Returns a `ReferralTreeResponse`:
```json
{ "referrer": { id,name,referralCode,coins,joinedDate,children:[ ... ] }, "children": [ ... ], "totalDescendants": 12 }
```

---

## Events
(controller: `eventsController.js`)

### GET /api/events
- Description: List events.
- Response: array of event summaries.

### GET /api/events/:id
- Description: Event details by id.

### POST /api/events/:eventId/register
- Description: Register the authenticated user for an event.
- Body: usually none, uses auth session; may accept extras.

---

## Tickets
(controller: `ticketsController.js`)

### GET /api/tickets/mine
- Description: List tickets for authenticated user.

### GET /api/tickets/:ticketId
- Description: Get a single ticket detail.

### DELETE /api/tickets/:ticketId
- Description: Cancel/delete a ticket (if allowed).

---

## Feed
(controller: `feedController.js`)

### GET /api/feed/posts
- Description: Paginated list of feed posts for the app.

---

## Referral / Download (web entrypoints)
(routes: `server/src/routes/referral.js` and `download.js`)

### GET /register?r=<encryptedToken> or /register?ref=RS3_ABC
- Description: Branded referral entrypoint. Redirects user to `/download` preserving referral query param.

### GET /s/:token
- Description: Short obfuscated referral links; decrypts token and redirects to `/download` with `ref` or `r`.

### GET /download/app and related
- Description: Web pages or helper routes to download the Android APK or deep link to app stores.

---

## Notes / Usage Guidance
- All `GET /api/users/*` and transaction endpoints require authentication. Use the token returned from `/auth/login` or your session cookie.
- `POST /api/auth/register` accepts `multipart/form-data` for `selfie` upload; server stores selfies under `media/selfies`.
- The referral tree endpoint returns nested `ReferralTreeNode` objects; UI can render this into the tree view.
- Wallet coin conversion constant: `COIN_TO_RUPEE = 0.1` (see `usersController.js`).

---

## Where to find server code
- Routes are in `server/src/routes/*.js` (auth.js, users.js, events.js, tickets.js, feed.js, referral.js).
- Controllers are in `server/src/controllers/*.js`.
- Services (business logic) in `server/src/services/*.js`.
- DB queries are in `server/src/db.js` and repository/service modules.

If you want, I can generate an OpenAPI (Swagger) YAML from these controllers next — tell me if you want a full OpenAPI spec or Postman collection.
