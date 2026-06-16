# 🚀 CinePass / RS³ Films - Complete Setup Guide

**Updated:** March 5, 2026  
**Status:** Upgraded to RS³ Films Schema v1.0

---

## Prerequisites

- ✅ PostgreSQL 12+ installed and running
- ✅ Node.js 18+ (for backend)
- ✅ JDK 21 (for Android app)
- ✅ Android Studio (optional, for emulator)

---

## 1. Database Setup

### Start PostgreSQL
```powershell
# Verify PostgreSQL is running
psql -U postgres -c "SELECT version();"
```

### Initialize Schema
```powershell
cd d:\cinepass\fanverse_web\backend

# Install dependencies if not done
npm install

# Create all tables (users, admin_users, offers, feed_posts, etc.)
npm run db:init
```

**Expected Output:**
```
✅ Database tables created successfully
```

### Seed Sample Data (Optional)
```powershell
npm run db:seed
```

**Sample Users Created:**
- **Admin:** priya@rs3.com / admin123
- **Ambassador:** Arjun Kumar (+919876543210) - 1240 coins
- **User:** Priya Mehta (+919012345678) - 560 coins

---

## 2. Backend Server (API)

### Configure Environment
Check `fanverse_web/backend/.env`:
```env
DATABASE_URL=postgresql://postgres:Animisha%402025@localhost:5432/Fanverse
JWT_SECRET=!@#$%^&*()_++_)(*&^%$#@!)
PORT=4001
NODE_ENV=development
```

### Start Backend
```powershell
cd d:\cinepass\fanverse_web\backend

# Development mode (auto-reload)
npm run dev

# OR production mode
npm start
```

**Backend runs on:** `http://localhost:4001`

### Test Endpoints
```powershell
# Health check
curl http://localhost:4001/v1/health

# Get app config
curl http://localhost:4001/v1/app/config

# Get feed
curl http://localhost:4001/v1/app/feed
```

---

## 3. Frontend Dashboard (Admin Panel)

```powershell
cd d:\cinepass\fanverse_web

# Install dependencies
npm install

# Start dev server
npm run dev
```

**Dashboard runs on:** `http://localhost:5173` (or next available port)

### Login
- Email: `priya@rs3.com`
- Password: `admin123`

---

## 4. Android App

### Set Java Environment
```powershell
# Set JAVA_HOME to JDK root (NOT bin folder)
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "User")

# Restart PowerShell
exit
```

### Configure API Base URL
Edit `app/src/main/java/com/cinepass/data/api/ApiClient.kt`:
```kotlin
private const val BASE_URL = "http://10.0.2.2:4001/" // For emulator
// OR
private const val BASE_URL = "http://192.168.x.x:4001/" // For physical device
```

### Build APK
```powershell
cd d:\cinepass

# Clean previous builds
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug
```

**APK location:** `app\build\outputs\apk\debug\app-debug.apk`

### Install on Device/Emulator
```powershell
# If device connected or emulator running
.\gradlew installDebug

# OR manually install APK
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Test App
1. **Open app** on device/emulator
2. **Login with OTP:**
   - Phone: `+919876543210`
   - OTP: `123456` (mock OTP in dev mode)
3. **Check profile** - Should show Arjun Kumar with 1240 coins
4. **View offers** - Should display offers from database
5. **Referral code** - Should show `RS3_ARJUN24`

---

## 5. Full Project Running

You should now have:

| Component | URL/Location | Status |
|-----------|--------------|--------|
| **PostgreSQL** | localhost:5432 | ✅ Running |
| **Backend API** | http://localhost:4001 | ✅ Running |
| **Admin Dashboard** | http://localhost:5173 | ✅ Running |
| **Android App** | Device/Emulator | ✅ Installed |

---

## API Endpoints Reference

### Public (No Auth)
```
GET  /v1/health                    # Health check
GET  /v1/app/config                # App configuration
GET  /v1/app/feed                  # Home feed posts
GET  /v1/app/cms/{section}        # CMS content
POST /v1/auth/app/otp/send        # Send OTP to phone
POST /v1/auth/app/otp/verify      # Verify OTP and login
```

### Authenticated (Bearer Token Required)
```
GET  /v1/app/member/profile       # User profile
GET  /v1/app/member/wallet        # Wallet & transactions
GET  /v1/app/member/referrals     # Referral stats
GET  /v1/app/member/referral-tree # Referral tree visualization
GET  /v1/app/offers?page=referral # Get offers (page: referral/wallet/home)
POST /v1/app/offers/{id}/claim    # Claim an offer
```

### Admin Dashboard
```
POST /v1/auth/login               # Admin login
GET  /v1/dashboard/stats          # Dashboard statistics
GET  /v1/members                  # List members (users)
GET  /v1/offers                   # Manage offers
GET  /v1/feed                     # Manage feed posts
GET  /v1/points-rules             # Manage point rules
POST /v1/upload                   # Upload media to Cloudinary
```

---

## Troubleshooting

### Backend Fails to Start
```powershell
# Check PostgreSQL connection
psql -U postgres -d Fanverse

# If database doesn't exist
psql -U postgres -c "CREATE DATABASE Fanverse;"

# Re-run init
npm run db:init
```

### Android Build Fails
```powershell
# Check Java version
java -version
# Should show: openjdk version "21.x.x"

# Check JAVA_HOME
echo $env:JAVA_HOME
# Should point to: C:\Program Files\Java\jdk-21

# Clear Gradle cache
.\gradlew clean
Remove-Item -Recurse -Force .gradle
```

### Cannot Connect to Backend from Android
- **Emulator:** Use `http://10.0.2.2:4001/`
- **Physical Device:** Use computer's IP (e.g., `http://192.168.1.100:4001/`)
- **Firewall:** Allow port 4001 in Windows Firewall

### Database Connection Error
```powershell
# Check .env DATABASE_URL format
# postgresql://user:password@host:port/database

# Verify credentials
psql -U postgres
\l  # List databases
\dt # List tables in current database
```

---

## Next Steps

### Customize Branding
1. Update `app_content` table in database (CMS section)
2. Replace logo in Android app (`app/src/main/res/drawable`)
3. Update colors in `app/src/main/java/com/cinepass/ui/theme/Color.kt`

### Deploy Backend
1. Set up production PostgreSQL database
2. Deploy to cloud (Heroku, AWS, DigitalOcean)
3. Update Android app `BASE_URL` to production API

### Publish Android App
1. Build release APK: `.\gradlew assembleRelease`
2. Sign APK with release keystore
3. Upload to Google Play Console

---

## Schema Documentation

Refer to these files for complete schema details:
- **Tables:** `fanverse_web/backend/src/db/init.js`
- **Sample Data:** `fanverse_web/backend/src/db/seed.js`
- **Upgrade Guide:** `SCHEMA_UPGRADE_SUMMARY.md`

---

## Support Contacts

- **Database Issues:** Check PostgreSQL logs
- **Backend Errors:** Check `fanverse_web/backend` console output
- **Android Errors:** Check Logcat in Android Studio
- **Schema Questions:** See `init.js` for authoritative table definitions
