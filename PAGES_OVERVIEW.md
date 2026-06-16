# Cinepass — Pages Overview

This document explains each Android app page, its purpose, and how pages interconnect. It also lists which pages depend on authentication or on API data.

---

## Splash Screen
- Purpose: Initial loading screen while app checks auth state, fetches initial config or remote assets.
- Dependencies: None to display; typically transitions to `Home` if user is logged in or `Login` if not.
- Notes: Do not modify per user instruction.

## Login
- Purpose: Authenticate users (phone/email + OTP or password). Obtains a session token used for subsequent requests.
- Dependency: Backend `/api/auth/login` and `/api/auth/verify-otp`.
- Redirects to: `Home` on success.

## Register
- Purpose: User registration; optionally upload `selfie` and accept referral code via query param.
- Dependency: Backend `/api/auth/register` (multipart/form-data for selfie).
- Notes: If opened from a referral link, the referral token/code is passed to register and stored.

## Home
- Purpose: Main app landing page showing feed, featured events, quick actions.
- Dependency: May use `/api/feed/posts`, `/api/events`.
- Independent of: Profile editing flows, but relies on auth for personalized content.

## Referral (Refer & Earn)
- Purpose: Shows user's referral code, share link, rewards structure, active referral offers and T&Cs.
- Dependencies:
  - `ProfileViewModel.getProfile()` -> `/api/users/profile` for referral code and shareLink
  - `/api/users/referrals` and `/api/users/referrals/tree` for deeper referral stats or tree view
- Related pages: `Profile` (shares referral info), `Referral Tree` (Profile sub-page)

## Wallet
- Purpose: Show coin balance, rupee equivalent, redeem/withdraw flow, transactions list.
- Dependencies:
  - `/api/users/wallet` for summary
  - `/api/users/wallet/transactions` for transactions
  - `/api/users/wallet/redeem` to submit redemption
- Notes: Wallet actions require authentication and are transactional (server-validated).

## Profile (Main)
- Purpose: User identity, avatar, stats (referrals, coins, events, saved), quick links to Edit, Achievements, Referral Tree, Settings, and Logout.
- Dependencies: `/api/users/profile` for most displayed data including selfie, coins, referralCode.
- Sub-pages:
  - Edit Profile — updates local profile fields and optionally uploads selfie (calls `auth/register` or a specific update API if present).
  - Referral Tree — visual network graph built from `/api/users/referrals/tree`.
  - Achievements — UI computed from `profile` + `wallet` + `referrals`.
  - Settings — toggles and account actions (logout, delete account, etc.).

## Referral Tree (Profile Sub-page)
- Purpose: Visualize the user's referral network (referrer, you, level 1, level 2 nodes).
- Dependency: `/api/users/referrals/tree`.
- Notes: Purely a read-only visualization that uses nested `ReferralTreeNode` objects.

## Achievements (Profile Sub-page)
- Purpose: Show badges and progress; many achievements are derived from referral and wallet counts.
- Dependencies: client composes using `ProfileUiState` (from `/api/users/profile`) and `wallet` data.

## Settings (Profile Sub-page)
- Purpose: Toggle notifications, email preferences, app appearance, and account actions (change password, privacy docs, delete account).
- Dependencies: Mostly local; account actions call backend endpoints (not all may exist in current API).

## Events / Event Detail / Register for Event
- Purpose: Browse upcoming events; have ability to view details and register for an event.
- Dependencies: `/api/events`, `/api/events/:id`, and `POST /api/events/:eventId/register`.
- Notes: Registration requires authentication.

## Tickets
- Purpose: Show user's booked/owned tickets and allow cancellation.
- Dependencies: `/api/tickets/mine`, `/api/tickets/:ticketId` and `DELETE /api/tickets/:ticketId`.

## Feed
- Purpose: App content feed (posts, announcements, promos).
- Dependencies: `/api/feed/posts` or similar.

---

## Page Dependency Graph (high level)
- Unauthenticated entry points: `Splash` -> either `Login` or `Home` (if already authed).
- Authenticated-only pages: `Profile`, `Wallet`, `Tickets`, `Events Register`, `Referral` (personalized), `Wallet Redeem`.
- Pages that depend on server data:
  - `Profile` depends on `/api/users/profile`.
  - `Wallet` depends on `/api/users/wallet` and `/api/users/wallet/transactions`.
  - `Referral Tree` depends on `/api/users/referrals/tree`.
  - `Events` depends on `/api/events`.

## Independent pages/components
- `Splash`, `Login`, `Register` are entry points and operate independently but produce an auth token used across the app.
- Static pages (Privacy Policy, Terms) are independent and do not require API calls; shown via WebView or local assets.

---

## Notes and Next Steps
- I created `API_DOCS.md` summarizing the server routes discovered under `server/src/routes` and `server/src/controllers`.
- If you want, I can generate:
  - A Postman collection or an OpenAPI (Swagger) spec file based on these controllers.
  - A per-endpoint example request/response JSON files.

Which of those would you like next? If you prefer an OpenAPI YAML, I can generate it and place it at `docs/openapi.yaml`.
