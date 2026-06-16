# RS³ Films — Backend API

Node.js + Express + PostgreSQL backend for the RS³ Films Admin Dashboard and Kotlin Mobile App.

## Quick Start

### 1. Prerequisites
- Node.js 18+
- PostgreSQL 14+

### 2. Setup

```bash
cd backend
cp .env.example .env
# Edit .env with your PostgreSQL connection string
npm install
```

### 3. Initialize Database

```bash
# Create tables
npm run db:init

# Seed sample data (optional)
npm run db:seed
```

### 4. Run

```bash
# Development (with auto-reload)
npm run dev

# Production
npm start
```

Server starts at `http://localhost:4000`

## API Base URL

```
http://localhost:4000/v1
```

## Default Admin Login

After seeding:
- **Email:** `priya@rs3.com`
- **Password:** `admin123`

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/login` | — | Admin login |
| POST | `/auth/app/otp/send` | — | Send OTP to mobile user |
| POST | `/auth/app/otp/verify` | — | Verify OTP |
| GET | `/dashboard/stats` | Admin | Dashboard statistics |
| GET | `/dashboard/referral-activity` | Admin | Referral activity chart |
| GET | `/members` | Admin | List members (paginated) |
| GET | `/members/:id` | Admin | Member detail |
| GET | `/members/:id/referral-tree` | Admin | Referral tree |
| GET | `/members/:id/coin-history` | Admin | Coin transaction history |
| GET/POST/PUT/DELETE | `/feed` | Admin | Feed post CRUD |
| PATCH | `/feed/:id/status` | Admin | Update feed status |
| GET/POST/PUT/DELETE | `/offers` | Admin | Offer CRUD |
| PATCH | `/offers/:id/toggle` | Admin | Toggle offer active |
| GET | `/redemptions` | Admin | List redemptions |
| PATCH | `/redemptions/:id/fulfill` | Admin | Mark as fulfilled |
| GET/POST/PUT/DELETE | `/points-rules` | Admin | Points rules CRUD |
| PATCH | `/points-rules/:id/toggle` | Admin | Toggle rule |
| GET | `/cms` | Admin | Get all CMS content |
| PUT | `/cms/:section` | Admin | Update CMS section |
| GET | `/admins` | Admin | List admins |
| POST | `/admins/invite` | Super | Invite admin |
| PUT | `/admins/:id/role` | Super | Change role |
| PATCH | `/admins/:id/toggle` | Super | Toggle active |
| GET | `/analytics/overview` | Admin | Analytics overview |
| GET | `/analytics/growth` | Admin | Growth charts |
| POST | `/upload` | Admin | File upload |
| **Mobile App Endpoints** | | |
| GET | `/app/offers` | App | Available offers |
| POST | `/app/offers/:id/claim` | App | Claim an offer |
| GET | `/app/cms/:section` | — | CMS content |
| GET | `/app/config` | — | App config |
| GET | `/app/feed` | — | Live feed posts |
| GET | `/app/member/profile` | App | User profile |
| PUT | `/app/member/profile` | App | Update profile |
| POST | `/app/referral/apply` | App | Apply referral code |

## Connecting Frontend

Set the API base URL in your frontend:

```typescript
// src/services/api.ts
const API_BASE = 'http://localhost:4000/v1';
```

## Connecting Kotlin App

Use Retrofit/Ktor with the same base URL. All endpoints return JSON.
Refer to `docs/API_DOCUMENTATION.md` for full request/response schemas.
