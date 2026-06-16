# 🚀 CinePass Referral System - Quick Start Guide

## ✅ Implementation Status: COMPLETE

All components have been successfully implemented:

### ✓ Backend (Server)
- [x] Multi-level referral service (3 levels: 100/40/15 coins)
- [x] Database schema (referral_chains, transactions)
- [x] Registration API with referral handling
- [x] Profile API with referral stats
- [x] Download page endpoint with beautiful UI
- [x] APK serving endpoint

### ✓ Android App
- [x] DataStore for referral code persistence
- [x] ClipboardHelper for auto-detection
- [x] MainActivity deep link handling
- [x] RegisterScreen auto-fill from preferences
- [x] ProfileScreen share button
- [x] API models with ReferralStats

---

## 🏃 Quick Start (Testing Before Play Store)

### Step 1: Build the APK

```powershell
# In PowerShell from d:\cinepass
.\gradlew assembleDebug
```

This creates: `app\build\outputs\apk\debug\app-debug.apk`

### Step 2: Copy APK to Server Folder

```powershell
# Copy the built APK to server folder
Copy-Item "app\build\outputs\apk\debug\app-debug.apk" "server\apk\cinepass.apk"
```

### Step 3: Update Server URL (Important!)

Edit your server environment/config so `SERVER_URL` matches your machine IP:

```javascript
// Replace with your actual local network IP
const SERVER_URL = process.env.SERVER_URL || 'http://192.168.29.212:4000';
```

For branded shared links (no IP shown), set:

```powershell
$env:SHARE_LINK_BASE_URL = "https://rs3films.com"
```

Now generated share links look like:
`https://rs3films.com/register?r=...`

**How to find your IP:**
```powershell
ipconfig
# Look for "IPv4 Address" under your active network adapter
# Example: 192.168.29.212
```

### Step 4: Start the Server

```powershell
cd server
npm install  # First time only
npm start
```

You should see:
```
✅ Server running on port 4000
✅ PostgreSQL connected
```

### Step 5: Test the Complete Flow

#### 5.1 Test Download Page
Open in browser on your phone (connected to same WiFi):
```
http://192.168.29.212:4000/download?ref=RS3_TEST01
```

You should see:
- ✅ Beautiful gradient background
- ✅ Referral code displayed: **RS3_TEST01**
- ✅ "Copy Code" button
- ✅ "Open CinePass" button (green)
- ✅ "Download CinePass" button (purple)
- ✅ Installation instructions

#### 5.2 Test APK Download
Click "Download CinePass" button on the page.

Expected behavior:
- ✅ Toast message: "Download started! Check your notifications."
- ✅ APK file downloads (~20-50 MB)
- ✅ Android asks permission to install from unknown source
- ✅ Install completes successfully

#### 5.3 Test Auto-Fill (Clipboard Method)

**On Phone 1 (User A):**
1. Install and open CinePass
2. Register new account (User A)
3. Login and go to Profile tab
4. Note your referral code (e.g., `RS3_ABC123`)
5. Click "Share Referral" button
6. Copy the link or send via WhatsApp/SMS to yourself

**On Phone 2 (User B):**
1. Click the shared link from Profile (now typically encrypted, like `http://192.168.29.212:4000/download?r=...`)
2. Wait for page to load (code auto-copies to clipboard)
3. Click "Download CinePass"
4. Install APK
5. Open app for the first time
6. Navigate to Register screen
7. **VERIFY**: Referral code field should show `RS3_ABC123` automatically!

#### 5.4 Test Deep Link (If App Already Installed)

**On Phone 2 (if CinePass already installed):**
1. Open the shared link in browser
2. Click "Open CinePass" (green button)
3. App should open directly
4. Navigate to Register screen
5. **VERIFY**: Referral code field should show `RS3_ABC123`

#### 5.5 Complete Registration and Verify Rewards

**On Phone 2:**
1. Complete registration form (with auto-filled referral code)
2. Submit registration
3. Login to new account

**On Phone 1 (User A):**
1. Pull to refresh Profile screen
2. **VERIFY**: 
   - Total Referred: **1**
   - Total Coins Earned: **+100**
   - Level 1 Earnings: **100 coins** / **1 user**

---

## 🔍 How It Works

### User Journey: A → B → C → D

```
User A (RS3_A00001)
  └─ refers User B (+100 coins to A)
      └─ refers User C (+100 to B, +40 to A)
          └─ refers User D (+100 to C, +40 to B, +15 to A)
```

### Technical Flow

#### 1. Share Link Generation (App-side)
```javascript
// app/src/main/java/com/cinepass/utils/ReferralLinkBuilder.kt
// App now generates direct branded links, e.g.:
// https://rs3films.com/register?ref=RS3_ABC123&utm_source=cinepass&utm_medium=referral
// Server /register endpoint handles redirect.
```

#### 2. Download Page (Browser)
```html
<!-- HTML page auto-copies code to clipboard -->
<script>
  const referralCode = 'RS3_ABC123';
  navigator.clipboard.writeText(referralCode);
  localStorage.setItem('cinepass_referral', referralCode);
</script>
```

#### 3. App Launch (MainActivity)
```kotlin
// Priority 1: Check deep link
val code = intent.data?.getQueryParameter("ref")

// Priority 2: Check clipboard
if (code == null) {
  code = ClipboardHelper.getReferralCodeFromClipboard(context)
}

// Save for registration
if (code != null) {
  ReferralPreferences.savePendingReferralCode(code)
}
```

#### 4. Registration Screen
```kotlin
LaunchedEffect(Unit) {
  val pendingCode = ReferralPreferences.getPendingReferralCode()
  if (pendingCode != null) {
    referralCode = pendingCode  // Auto-fill!
    ReferralPreferences.clearPendingReferralCode()
  }
}
```

#### 5. Backend Processing
```javascript
// server/src/controllers/authController.js
const newUser = await createUser(userData);

if (referralCode) {
  const referrer = await findUserByReferralCode(referralCode);
  const rewards = await handleReferralRewards(newUser.id, referrer.id);
  // rewards = [
  //   { userId: referrer.id, level: 1, amount: 100 },
  //   { userId: referrer.referredBy.id, level: 2, amount: 40 },
  //   { userId: referrer.referredBy.referredBy.id, level: 3, amount: 15 }
  // ]
}
```

---

## 🧪 Testing Scenarios

### Scenario 1: Single Referral (A → B)
```
1. User A registers → gets code RS3_A00001
2. User A shares link
3. User B opens link → code auto-copied
4. User B installs app → code auto-filled
5. User B registers with code RS3_A00001
6. Result:
   - User A: +100 coins
   - Transaction: "Referral reward - Level 1 from User B"
```

### Scenario 2: Chain Referral (A → B → C)
```
1. User B (referred by A) gets code RS3_B00002
2. User B shares link
3. User C opens link, installs, registers with RS3_B00002
4. Result:
   - User B: +100 coins (Level 1 from C)
   - User A: +40 coins (Level 2 from C)
   - Transactions created for both
```

### Scenario 3: Max Chain (A → B → C → D)
```
1. User C (referred by B, who was referred by A) shares code
2. User D registers with C's code
3. Result:
   - User C: +100 coins (Level 1)
   - User B: +40 coins (Level 2)
   - User A: +15 coins (Level 3)
```

### Scenario 4: Invalid Code
```
1. User tries to register with "INVALID123"
2. Registration succeeds but no referral rewards
3. Server logs: "Referral code not found: INVALID123"
```

### Scenario 5: Self-Referral Prevention
```
1. User A tries to use their own code
2. Backend should reject (implement check if needed)
3. Error: "Cannot refer yourself"
```

---

## 📊 Database Queries for Testing

### Check User's Referral Stats
```sql
SELECT 
  u.id,
  u.full_name,
  u.referral_code,
  u.coins,
  COUNT(DISTINCT rc.new_user_id) as total_referred,
  COUNT(DISTINCT rc1.new_user_id) as level1_count,
  COUNT(DISTINCT rc2.new_user_id) as level2_count,
  COUNT(DISTINCT rc3.new_user_id) as level3_count
FROM users u
LEFT JOIN referral_chains rc1 ON rc1.level_1_user_id = u.id
LEFT JOIN referral_chains rc2 ON rc2.level_2_user_id = u.id
LEFT JOIN referral_chains rc3 ON rc3.level_3_user_id = u.id
LEFT JOIN referral_chains rc ON rc.level_1_user_id = u.id 
                              OR rc.level_2_user_id = u.id 
                              OR rc.level_3_user_id = u.id
WHERE u.id = 1
GROUP BY u.id;
```

### View All Transactions
```sql
SELECT 
  t.id,
  u.full_name,
  t.amount,
  t.type,
  t.description,
  t.metadata,
  t.created_at
FROM transactions t
JOIN users u ON u.id = t.user_id
ORDER BY t.created_at DESC
LIMIT 20;
```

### View Referral Chains
```sql
SELECT 
  rc.id,
  new_user.full_name as new_user,
  l1.full_name as level_1_referrer,
  l2.full_name as level_2_referrer,
  l3.full_name as level_3_referrer,
  rc.created_at
FROM referral_chains rc
JOIN users new_user ON new_user.id = rc.new_user_id
LEFT JOIN users l1 ON l1.id = rc.level_1_user_id
LEFT JOIN users l2 ON l2.id = rc.level_2_user_id
LEFT JOIN users l3 ON l3.id = rc.level_3_user_id
ORDER BY rc.created_at DESC;
```

---

## 🐛 Common Issues & Solutions

### Issue: "APK file not found" when clicking Download

**Fix:**
```powershell
# Ensure APK is in correct location
Test-Path "server\apk\cinepass.apk"
# Should return: True

# If False, rebuild:
.\gradlew assembleDebug
Copy-Item "app\build\outputs\apk\debug\app-debug.apk" "server\apk\cinepass.apk"
```

### Issue: Download page shows broken link or wrong IP

**Fix:**
```javascript
// Update server\src\services\referralService.js
const SERVER_URL = 'http://YOUR_CORRECT_IP:4000';

// Or set environment variable:
// In PowerShell:
$env:SERVER_URL = "http://192.168.29.212:4000"
npm start
```

### Issue: Referral code not auto-filling

**Check:**
1. Open browser DevTools on download page
2. Console should show: `Referral code: RS3_ABC123`
3. Check clipboard: Long-press any text field → Paste (should show the code)

**Android Logs:**
```powershell
adb logcat | Select-String "MainActivity|ReferralPreferences"
# Should show:
# MainActivity: Found referral code from clipboard: RS3_ABC123
# ReferralPreferences: Saved pending code: RS3_ABC123
```

### Issue: Deep link not opening app

**Fix:**
```powershell
# Test deep link manually:
adb shell am start -a android.intent.action.VIEW -d "cinepass://register?ref=RS3_TEST01" com.cinepass

# Should open app and auto-fill code
```

### Issue: "Cannot connect to server" when clicking download

**Check:**
1. Phone and computer on same WiFi network
2. Server is running (`npm start` in server folder)
3. Firewall allows port 4000
4. Correct IP address in share link

**Test server accessibility:**
```powershell
# From phone browser:
http://YOUR_IP:4000/api/health
# Should return: {"status": "ok"}
```

---

## 📱 Migration to Play Store (Future)

When ready to publish to Google Play Store:

### 1. Update Share Link Format

**Change in `referralService.js`:**
```javascript
generateShareLink(referralCode) {
  // Production: Play Store link with Install Referrer
  const playStoreUrl = 'https://play.google.com/store/apps/details';
  const packageName = 'com.cinepass';
  const referrer = encodeURIComponent(`utm_source=cinepass&utm_medium=referral&ref=${referralCode}`);
  return `${playStoreUrl}?id=${packageName}&referrer=${referrer}`;
}
```

### 2. Implement Install Referrer in MainActivity

```kotlin
// Already added dependency:
// implementation("com.android.installreferrer:installreferrer:2.2")

private fun checkInstallReferrer() {
    val referrerClient = InstallReferrerClient.newBuilder(this).build()
    
    referrerClient.startConnection(object : InstallReferrerStateListener {
        override fun onInstallReferrerSetupFinished(responseCode: Int) {
            if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                val response = referrerClient.installReferrer
                val referrerUrl = response.installReferrer
                
                // Parse: utm_source=cinepass&utm_medium=referral&ref=RS3_ABC123
                val uri = Uri.parse("?$referrerUrl")
                val refCode = uri.getQueryParameter("ref")
                
                lifecycleScope.launch {
                    refCode?.let {
                        ReferralPreferences(this@MainActivity).savePendingReferralCode(it)
                    }
                }
            }
            referrerClient.endConnection()
        }
        
        override fun onInstallReferrerServiceDisconnected() {
            // Retry connection if needed
        }
    })
}
```

### 3. Enable Android App Links (HTTPS Deep Links)

**Already configured in AndroidManifest.xml:**
```xml
<intent-filter android:autoVerify="true">
    <data android:scheme="https" 
          android:host="cinepass.app" 
          android:pathPrefix="/register" />
</intent-filter>
```

**Setup `.well-known/assetlinks.json` on your domain:**
```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.cinepass",
    "sha256_cert_fingerprints": [
      "YOUR_APP_CERTIFICATE_FINGERPRINT"
    ]
  }
}]
```

Host at: `https://cinepass.app/.well-known/assetlinks.json`

### 4. Update Download Endpoint (Keep for Testing)

The server download page can remain for internal testing, but production share links should use Play Store URLs.

---

## ✅ Pre-Launch Checklist

Before deploying to production:

### Backend
- [ ] Environment variables set (DATABASE_URL, JWT_SECRET, SERVER_URL)
- [ ] Database migrations run
- [ ] SSL certificate configured (HTTPS)
- [ ] Server monitoring setup
- [ ] Backup strategy in place

### Android
- [ ] Build release APK (not debug)
- [ ] Sign APK with release keystore
- [ ] ProGuard/R8 enabled
- [ ] Test on multiple devices
- [ ] Test offline behavior
- [ ] Privacy policy updated

### Testing
- [ ] Test complete referral flow (A → B → C → D)
- [ ] Test invalid referral codes
- [ ] Test self-referral prevention
- [ ] Test duplicate registrations
- [ ] Load test server (100+ concurrent users)
- [ ] Security audit

### Documentation
- [ ] API documentation complete
- [ ] User guide for referral system
- [ ] Admin dashboard for monitoring
- [ ] Support team training

---

## 📞 Support & Debugging

### View Logs

**Server Logs:**
```powershell
cd server
npm start
# Watch for:
# - "✅ Referral rewards processed for user X"
# - "⚠️ Referral code not found: XXX"
```

**Android Logs:**
```powershell
adb logcat -s "MainActivity:D" "ReferralPreferences:D" "RegisterScreen:D"
```

### Test API Directly

**Get Profile with Referral Stats:**
```powershell
$headers = @{"Authorization" = "Bearer YOUR_TOKEN"}
Invoke-RestMethod -Uri "http://localhost:4000/api/users/profile" -Headers $headers | ConvertTo-Json -Depth 5
```

**Register with Referral Code:**
```powershell
$body = @{
  fullName = "Test User"
  email = "test@example.com"
  phone = "9876543210"
  password = "Test@1234"
  referralCode = "RS3_ABC123"
  # ... other fields
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:4000/api/auth/register" -Method Post -Body $body -ContentType "application/json"
```

---

## 🎉 Success!

Your CinePass referral system is now fully implemented and ready for testing!

**Next Steps:**
1. Build APK: `.\gradlew assembleDebug`
2. Copy to server: `Copy-Item ...`
3. Update SERVER_URL in referralService.js
4. Start server: `npm start`
5. Test complete flow with 2 devices
6. Monitor database for referral_chains and transactions
7. Celebrate! 🎊

**Need Help?**
- Check server logs for errors
- Check Android logcat for debug info
- Verify database records
- Test each component individually

---

**Built for RS³ FILMS CinePass - MLM-Lite Referral System v1.0**
