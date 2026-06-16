# RS³ Films — Kotlin Android Prototype
## Complete Setup Guide

---

## WHAT YOU'LL BUILD
A working Android app connected to your Node.js backend with:
- User Registration + Referral Code
- Event Page (your movie)
- Seat Selection + Ticket Booking
- Wallet & Coins
- QR Pass Generation

---

## PREREQUISITES — Install These First

### 1. Android Studio
Download: https://developer.android.com/studio
- Install with default settings
- SDK: Android 14 (API 34) — install during setup

### 2. Your Backend (from previous step)
Make sure your Node.js server is running:
```bash
cd fanverse-server
npm run dev
# Server must be on http://localhost:3000
```

### 3. Find Your Computer's IP Address
Your phone needs this to reach your laptop server.
```bash
# On Mac/Linux:
ifconfig | grep "inet " | grep -v 127.0.0.1

# On Windows:
ipconfig
# Look for "IPv4 Address" e.g. 192.168.1.5
```
Note this IP — you'll use it instead of "localhost" in the app.

---

## STEP 1 — Create New Android Project

1. Open Android Studio
2. Click "New Project"
3. Select **"Empty Activity"**
4. Fill in:
   - Name: `RS3Films`
   - Package: `com.rs3films.app`
   - Language: **Kotlin**
   - Minimum SDK: **API 26 (Android 8.0)**
5. Click Finish — wait for Gradle sync

---

## STEP 2 — Replace build.gradle Files

### App-level build.gradle (app/build.gradle)
Replace the entire file with the content from:
`app_build.gradle.txt` (provided in this package)

### Project-level build.gradle (build.gradle)
Replace with content from:
`project_build.gradle.txt`

After replacing, click **"Sync Now"** in Android Studio.

---

## STEP 3 — Add All Source Files

Copy each file from this package into the correct location in your project.

### Folder structure to create:
```
app/src/main/java/com/rs3films/app/
├── MainActivity.kt
├── data/
│   ├── api/
│   │   ├── ApiClient.kt
│   │   ├── ApiService.kt
│   │   └── models/
│   │       ├── AuthModels.kt
│   │       ├── EventModels.kt
│   │       ├── TicketModels.kt
│   │       └── WalletModels.kt
│   └── prefs/
│       └── UserPrefs.kt
├── ui/
│   ├── auth/
│   │   ├── LoginActivity.kt
│   │   ├── RegisterActivity.kt
│   │   └── OtpActivity.kt
│   ├── home/
│   │   └── HomeActivity.kt
│   ├── event/
│   │   └── EventDetailActivity.kt
│   ├── booking/
│   │   ├── SeatSelectionActivity.kt
│   │   └── BookingConfirmActivity.kt
│   ├── wallet/
│   │   └── WalletActivity.kt
│   └── ticket/
│       └── QRTicketActivity.kt
└── utils/
    ├── Extensions.kt
    └── Constants.kt

app/src/main/res/
├── layout/
│   ├── activity_login.xml
│   ├── activity_register.xml
│   ├── activity_home.xml
│   ├── activity_event_detail.xml
│   ├── activity_seat_selection.xml
│   ├── activity_wallet.xml
│   ├── activity_qr_ticket.xml
│   ├── item_seat.xml
│   ├── item_transaction.xml
│   └── item_showtime.xml
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   └── themes.xml
└── drawable/
    └── (auto-generated)
```

---

## STEP 4 — Configure Your Server IP

Open `app/src/main/java/com/rs3films/app/utils/Constants.kt`

Change this line:
```kotlin
const val BASE_URL = "http://YOUR_IP:3000/api/"
```
Replace YOUR_IP with your computer's IP from Step 0.

Example: `"http://192.168.1.5:3000/api/"`

---

## STEP 5 — Run on Your Phone

### Enable Developer Mode on Android Phone:
1. Settings → About Phone
2. Tap "Build Number" 7 times
3. Go back → Developer Options → Enable "USB Debugging"

### Connect and Run:
1. Connect phone via USB
2. In Android Studio, select your phone in the device dropdown
3. Click the green ▶ Play button
4. App installs and opens on your phone!

---

## STEP 6 — Test the App Flow

Use these test credentials (from the seed data):
- Email: arjun@test.com
- Password: password123
- Referral Code: ARJUN_FAN42

### Test flow:
1. Register new user with referral code ARJUN_FAN42
2. Browse event page (your movie)
3. Select seats and book ticket
4. Check wallet — coins should update
5. View QR ticket pass

---

## TROUBLESHOOTING

**"Connection refused" error**
→ Make sure Node.js server is running
→ Make sure you used your IP not "localhost"
→ Both phone and computer on same WiFi

**Gradle sync fails**
→ File → Invalidate Caches → Restart
→ Check internet connection (downloads dependencies)

**App crashes on start**
→ Check Logcat in Android Studio for red errors
→ Usually a missing file or wrong IP

---

## ARCHITECTURE USED
- **MVVM** (Model-View-ViewModel)
- **Retrofit** for API calls
- **Coroutines** for async
- **ViewBinding** for views
- **SharedPreferences** for local storage (JWT token, user data)
- **ZXing** for QR code generation
