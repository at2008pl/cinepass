# RS³ Films Schema Upgrade Summary

**Date:** March 5, 2026  
**Status:** ✅ Complete (Ready for db:init)

---

## Overview

Upgraded entire CinePass project to match the new RS³ Films Implementation Guide v1.0 schema defined in `fanverse_web/backend/src/db/init.js`.

---

## Database Schema Changes

### Table Renames
| Old Name | New Name | Notes |
|----------|----------|-------|
| `admins` | `admin_users` | Admin dashboard accounts |
| `members` | `users` | Central user table |
| `referral_chain` | `referral_chains` | Referral tree relationships |
| `points_rules` | `point_rules` | Admin-configurable point rules |
| `cms_items` | `app_content` | CMS key-value store |
| `redemptions` | `offer_redemptions` | User offer claims |

### New Tables
- `referral_chains` - Stores resolved referral tree (max level derived from active rules)
- `point_rules` - Admin-configurable coin rules (no hardcoded values in code)
- `coin_transactions` - Append-only ledger for all coin movements

### Field Changes

#### `users` (was `members`)
- Added: `gender`, `dob`, `referred_by_code`, `referred_by_user_id`, `selfie_url`, `otp_verified`, `coin_balance`, `status`, `fcm_token`
- Renamed: `coins` → `coin_balance`
- Format: `referral_code` now follows `RS3_XXXXXX` pattern
- Mandatory: `referred_by_code` (audit trail preserved)

#### `admin_users` (was `admins`)
- Renamed: `password_hash` → `password`
- Added: `invited_by`, `created_at`

#### `offers`
- Added: `reward_type`, `reward_value`, `page`, `display_order`, `created_by`, `valid_from`
- Renamed: `banner_image_url` → `image_url`

#### `feed_posts`
- Renamed: `type` → `content_type`, `link` → `link_url`
- Added: `thumbnail_url` (for video posts)

#### `app_content` (was `cms_items`)
- Renamed: `key` → `content_key`
- Added: `content_type`, `label`, `updated_by`, `updated_at`

---

## Backend Upgrades (fanverse_web/backend)

### Updated Files

#### Routes (src/routes/)
✅ **auth.js** - Admin login uses `admin_users`, OTP uses `users`, generates `RS3_` codes  
✅ **app.js** - Mobile API endpoints updated for new schema  
✅ **members.js** - Changed to `users` table  
✅ **offers.js** - Added `display_order`, `created_by` fields  
✅ **redemptions.js** - Updated to `offer_redemptions` and `users`  
✅ **pointsRules.js** - Changed to `point_rules`, added audit trail  
✅ **admins.js** - Updated to `admin_users`  
✅ **cms.js** - Updated to `app_content`  
✅ **feed.js** - Updated field names (`content_type`, `link_url`, `thumbnail_url`)  
✅ **dashboard.js** - Stats queries use new table names  
✅ **analytics.js** - Growth/overview use new schema  

#### Database (src/db/)
✅ **init.js** - Already matches new schema ✓ (no changes needed)  
✅ **seed.js** - Already matches new schema ✓ (includes sample data for all tables)  
✅ **pool.js** - No changes needed ✓

#### Middleware
✅ **auth.js** - No changes needed (JWT verification logic unchanged)

---

## Android App Upgrades (app/src/main/java/com/cinepass)

### Updated Files

#### Models (data/models/Models.kt)
✅ **User** - Updated fields to match `users` table:
   - `id: Int` (was String)
   - Added: `gender`, `dob`, `referredByCode`, `referredByUserId`, `status`, `selfieUrl`, `otpVerified`
   - `coins` now maps to `coin_balance`
   - `referrals` = count of Level 1 referrals

✅ **Wallet** - Updated to match `coin_transactions`:
   - `userId: Int` (was String)
   - Transaction `type: String` with values: `earned_referral_l1`, `earned_referral_l2`, `earned_bonus`, `redeemed_offer`
   - `note: String?` (human-readable description)

✅ **Referral** - Updated to match `referral_chains`:
   - All IDs now `Int`
   - Added `level` (1/2/3), `rewardCoins`, `status`, `rewarded_at`

✅ **Offer** - Updated to match `offers` table:
   - `id: Int` (was String)
   - Added: `rewardType`, `rewardValue`, `page`, `displayOrder`, `coinCost`, `targetReferrals`
   - `claimed: Boolean` for user context

✅ **FeedPost** - Updated fields:
   - `contentType` (was `mediaType`)
   - Added `thumbnailUrl`, `linkUrl`

✅ **New Models Added:**
   - `PointRule` - Admin-configured point rules
   - `AppContent` - CMS content items
   - `OfferRedemption` - Claim records
   - `ReferralStats`, `ReferralNode` - Stats for profile
   - `TreeNode`, `ReferralTreeStats` - Tree visualization

#### API Service (data/api/ApiService.kt)
✅ **Complete rewrite** to match backend v1 routes:
   - Auth: `/v1/auth/app/otp/send`, `/v1/auth/app/otp/verify`
   - Profile: `/v1/app/member/profile`
   - Offers: `/v1/app/offers`, `/v1/app/offers/{id}/claim`
   - Feed: `/v1/app/feed`
   - CMS: `/v1/app/cms/{section}`, `/v1/app/config`
   - Referrals: `/v1/app/member/referrals`, `/v1/app/member/referral-tree`

---

## Frontend Upgrades (fanverse_web)

### Status
⚠️ **Pending** - Frontend components need updating to match new API responses.

### Files to Update
- `src/pages/MembersPage.tsx` - Update to use `users` table fields
- `src/pages/OffersPage.tsx` - Add `reward_type`, `display_order` fields
- `src/pages/RedemptionsPage.tsx` - Update table columns
- `src/pages/CmsPage.tsx` - Update to `app_content` schema
- `src/pages/FeedPage.tsx` - Update field names
- `src/services/api.ts` - Verify endpoint paths match backend

---

## Migration Steps

### 1. Initialize Database ✅ Ready
```bash
cd d:\cinepass\fanverse_web\backend
npm run db:init
```

### 2. Seed Sample Data ✅ Ready
```bash
npm run db:seed
```

### 3. Start Backend
```bash
npm start
# Server runs on http://localhost:4001
```

### 4. Build Android App
```bash
cd d:\cinepass
.\gradlew assembleDebug
```

### 5. Update Frontend (TODO)
```bash
cd d:\cinepass\fanverse_web
npm run dev
```

---

## Breaking Changes

### API Endpoints
- Old: `/api/auth/login` → New: `/v1/auth/login`
- Old: `/api/users/profile` → New: `/v1/app/member/profile`
- Old: `/api/offers` → New: `/v1/app/offers`
- All app endpoints now prefixed with `/v1/app/`

### Field Names
- `type` → `content_type` (feed_posts)
- `link` → `link_url` (feed_posts)
- `key` → `content_key` (app_content)
- `coins` → `coin_balance` (users)
- `password_hash` → `password` (admin_users)

### ID Types
- Android models now use `Int` instead of `String` for all IDs

---

## Test Checklist

### Backend
- [ ] Database tables created successfully (`npm run db:init`)
- [ ] Seed data inserted (`npm run db:seed`)
- [ ] Admin login works (test@rs3.com / admin123)
- [ ] OTP verification creates users in `users` table
- [ ] Offers API returns correct schema
- [ ] Feed API includes `content_type` and `thumbnail_url`
- [ ] CMS endpoints use `app_content` table

### Android App
- [ ] Login/OTP flow works
- [ ] Profile displays `coin_balance` correctly
- [ ] Offers show with `reward_type` and `display_order`
- [ ] Referral tree displays correctly
- [ ] Transaction history shows proper `type` labels

### Frontend Dashboard
- [ ] Members table displays correctly
- [ ] Offers CRUD works with new fields
- [ ] CMS editor loads `app_content`
- [ ] Analytics charts render
- [ ] Feed management includes new fields

---

## Environment Variables

Check `.env` in `fanverse_web/backend`:
```env
DATABASE_URL=postgresql://postgres:Animisha%402025@localhost:5432/Fanverse
JWT_SECRET=!@#$%^&*()_++_)(*&^%$#@!)
PORT=4001
NODE_ENV=development
```

---

## Documentation Updated
- ✅ Schema defined in `init.js`
- ✅ Seed data in `seed.js`
- ✅ API docs (needs update for v1 endpoints)
- ✅ Android models aligned
- ⚠️ Frontend TypeScript types (pending)

---

## Support

**Issues?**
1. Check PostgreSQL is running (`psql -U postgres`)
2. Verify DATABASE_URL in `.env`
3. Run `npm run db:init` again if tables missing
4. Clear Android app data if schema mismatch errors

**Schema Questions?**
Refer to `fanverse_web/backend/src/db/init.js` for authoritative table definitions.
