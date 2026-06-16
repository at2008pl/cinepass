# Cinepass App - Testing Guide

## ✅ All Integration Complete!

All 4 main screens now have the new bottom navigation, and ProfileScreen includes the referral chain tree visualization.

---

## 🔧 Changes Summary

### Frontend (Android App)

#### 1. New Components Created
- ✅ `AppBottomNavigation.kt` - 4-tab bottom navigation (Home/Referral/Wallet/Profile)
- ✅ `ReferralChainTree.kt` - Tree visualization component with recursive rendering
- ✅ `ReferralTree.kt` - Data models for tree structure

#### 2. Updated Screens
- ✅ **HomeScreen.kt** - Now uses `AppBottomNavigation`, added `onReferralClick` parameter
- ✅ **ReferralScreen.kt** - Wrapped in Scaffold with bottom navigation
- ✅ **WalletScreen.kt** - Replaced TopAppBar with bottom navigation
- ✅ **ProfileScreen.kt** - Added bottom navigation + referral chain tree section
- ✅ **AppNavigation.kt** - Updated all route definitions to pass tab navigation callbacks

#### 3. Updated ViewModels & Repositories
- ✅ **ProfileViewModel.kt** - Added `referralTree` state flow and `loadReferralTree()` function
- ✅ **ProfileRepository.kt** - Added `getReferralTree()` function that calls API

#### 4. Updated API Service
- ✅ **ApiService.kt** - Fixed `referral_code` parameter (was `referralCode`)
- ✅ **ApiService.kt** - Added `getReferralTree()` endpoint call

### Backend (Node.js Server)

#### 1. New API Endpoints
- ✅ `GET /api/users/referrals/tree` - Returns hierarchical referral tree

#### 2. Services
- ✅ **referralService.js** - Added `getReferralTree()` with recursive PostgreSQL queries
- ✅ **referralService.js** - Fixed `handleReferralRewards()` to update wallets table

#### 3. Controllers & Routes
- ✅ **usersController.js** - Added `getReferralTree` controller function
- ✅ **routes/users.js** - Added tree route mapping

#### 4. Bug Fixes
- ✅ **authController.js** - Added input sanitization (strips surrounding quotes)
- ✅ **authController.js** - Accept both `referral_code` and `referralCode` parameters
- ✅ **authController.js** - Login backward compatibility for quoted passwords

#### 5. Database Scripts
- ✅ **cleanQuotes.js** - One-time migration script to clean existing data

---

## 🧪 Testing Steps

### Step 1: Clean & Restart Server

```powershell
cd D:\cinepass\server

# (Optional) Clean existing quoted data from database
node src/scripts/cleanQuotes.js

# Restart server
npm start
```

**Expected Output:**
```
Server running on port 4000
Database connected
✓ Referral tree endpoint registered at GET /api/users/referrals/tree
```

### Step 2: Test Backend API Directly

```powershell
# First, get an access token by logging in
$loginResponse = Invoke-RestMethod -Uri "http://192.168.29.212:4000/api/auth/login" -Method POST -Body (@{email="test@example.com"; password="password123"} | ConvertTo-Json) -ContentType "application/json"
$token = $loginResponse.data.accessToken

# Test the referral tree endpoint
Invoke-RestMethod -Uri "http://192.168.29.212:4000/api/users/referrals/tree" -Headers @{Authorization="Bearer $token"}
```

**Expected Response Structure:**
```json
{
  "success": true,
  "data": {
    "referrer": {
      "id": "2",
      "name": "John Doe",
      "email": "john@example.com",
      "referralCode": "RS3_ABC123",
      "coins": 500,
      "joinedDate": "2024-01-15T10:30:00.000Z",
      "children": []
    },
    "children": [
      {
        "id": "4",
        "name": "Jane Smith",
        "referralCode": "RS3_XYZ789",
        "coins": 250,
        "children": [/* nested children */]
      }
    ],
    "totalDescendants": 5
  }
}
```

### Step 3: Rebuild Android App

```powershell
cd D:\cinepass

# Clean previous build
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug
```

**Check for compilation errors.** Expected output:
```
BUILD SUCCESSFUL in 45s
87 actionable tasks: 87 executed
```

### Step 4: Install App on Device

```powershell
# Install via ADB
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Copy to server APK folder (for web download)
Copy-Item "app\build\outputs\apk\debug\app-debug.apk" "server\apk\cinepass.apk" -Force
```

### Step 5: Test Navigation Flow

1. **Open the app**
2. **Login/Register** with a test account
3. **Navigate between tabs** using bottom navigation:
   - Tap **Home** → Should show feed with events
   - Tap **Referral** → Should show referral link, terms & conditions
   - Tap **Wallet** → Should show coins, transactions
   - Tap **Profile** → Should show profile info, stats, badges

**✅ Expected:** Smooth navigation between all 4 tabs, selected tab highlighted in blue (FanAccent)

### Step 6: Test Referral Chain Tree (Profile Screen)

1. **Navigate to Profile tab**
2. **Scroll down** past profile card, stats, and badges
3. **Find "My Referral Network" section**

**✅ Expected Visual Elements:**
- **Section title**: "My Referral Network"
- **If you have a referrer:**
  - Purple card with arrow up icon
  - Shows person who referred you (name, code, coins)
- **Gold "YOU" card** marking your position
- **Level 1 referrals** (blue cards) - people you directly referred
- **Level 2 referrals** (green cards) - people your referrals referred (indented 24dp)
- **Level 3 referrals** (orange cards) - third level (indented 48dp)
- **Total descendants count** at the top

**If no referrals exist:**
```
📊 My Referral Network
────────────────────────
No referrals yet. Share your referral link to start building your network!
```

### Step 7: Test Referral Rewards Flow (End-to-End)

#### User A (Referrer):
1. **Register** new account "User A"
2. **Navigate to Referral screen**
3. **Copy referral code** (e.g., `RS3_ABC123`)
4. **Note current coins** in Wallet

#### User B (First Referral):
1. **Register** new account "User B"
2. **Enter User A's referral code** during registration
3. **Complete registration**

#### Verify Rewards:
1. **Login as User A**
2. **Check Wallet** → Should show +100 coins
3. **Check Profile → Referral Network** → Should show User B as Level 1 (blue card)
4. **Check Transactions** → Should show "Referral Reward - Level 1: User B"

#### User C (Second-Level Referral):
1. **Register** new account "User C"
2. **Enter User B's referral code**
3. **Complete registration**

#### Verify Multi-Level Rewards:
1. **Login as User A** → Wallet shows +40 coins (level 2 reward)
2. **Login as User B** → Wallet shows +100 coins (level 1 reward)
3. **Login as User A** → Profile → Referral Network shows:
   - User B (blue, Level 1)
   - User C under User B (green, Level 2, indented)

### Step 8: Test Quote Sanitization

1. **Register a new user** with form data
2. **Check database** (or login to verify):
   ```sql
   SELECT name, email, phone FROM users WHERE email = 'newuser@example.com';
   ```
   **Expected:** All fields should NOT have surrounding quotes

3. **Verify backward compatibility:**
   - Old users with quoted passwords (`"password123"`) should still be able to login
   - New registrations store unquoted values

---

## 🐛 Common Issues & Fixes

### Issue 1: "Column 'coins' does not exist"
**Cause:** Old code trying to update `users.coins` instead of `wallets.coins`
**Fix:** Already fixed in `referralService.js` lines 103-115

### Issue 2: Referral code not working during registration
**Cause:** Android sending `referralCode` (camelCase), server expecting `referral_code`
**Fix:** Already fixed in `ApiService.kt` line 23

### Issue 3: Tree not showing in Profile
**Possible Causes:**
- API endpoint not responding → Check server logs
- No referrals exist → Expected behavior, shows empty state
- API call failing → Check ProfileViewModel logs: `adb logcat | grep ProfileViewModel`

**Debug:**
```powershell
# Check server logs
npm start | grep "referral"

# Check Android logs
adb logcat | grep -i "profile\|referral\|tree"
```

### Issue 4: Bottom navigation not appearing
**Possible Causes:**
- Scaffold `bottomBar` not rendering → Check `padding` is applied to content
- Import missing → Verify `import com.cinepass.ui.components.AppBottomNavigation`

**Debug:**
```kotlin
// In any screen file, verify these imports exist:
import com.cinepass.ui.components.AppBottomNavigation
import com.cinepass.ui.components.BottomNavTab
```

### Issue 5: Tree API returns 500 error
**Possible Causes:**
- Database connection failed
- Recursive query syntax error
- User has no referrals (should return empty arrays, not error)

**Debug:**
```sql
-- Test query manually in PostgreSQL:
SELECT u.id, u.name, u.email, u.referral_code, w.coins, u.created_at 
FROM users u 
LEFT JOIN wallets w ON w.user_id = u.id 
WHERE u.referred_by_user_id = YOUR_USER_ID;
```

### Issue 6: App crashes on Profile screen
**Check Logcat:**
```powershell
adb logcat | grep -i "exception\|error\|crash"
```

**Common causes:**
- Null pointer in tree rendering → Tree data should be wrapped in `treeState?.let { }`
- Missing ViewModel dependency → Hilt injection not configured

---

## 📊 Test Checklist

### Backend Tests
- [ ] Server starts without errors
- [ ] `GET /api/users/referrals/tree` returns tree structure
- [ ] Registration accepts `referral_code` parameter
- [ ] Registration accepts `referralCode` parameter (backward compat)
- [ ] Referral rewards credit to wallets table
- [ ] Multi-level rewards calculate correctly (100/40/15)
- [ ] Input sanitization removes surrounding quotes
- [ ] Login works for both quoted and unquoted passwords

### Frontend Tests
- [ ] App builds without compilation errors
- [ ] Bottom navigation appears on all 4 screens
- [ ] Tab selection highlights correctly (blue accent)
- [ ] Navigation between tabs works smoothly
- [ ] Profile screen loads without crashing
- [ ] Referral tree section appears in Profile
- [ ] Tree displays referrer (if exists) with purple card
- [ ] Tree displays "YOU" card in gold
- [ ] Tree displays level 1/2/3 descendants with correct colors
- [ ] Tree shows empty state when no referrals exist
- [ ] Wallet screen shows correct coin balance
- [ ] Referral screen shows share link and terms

### E2E Flow Tests
- [ ] New user registration with referral code succeeds
- [ ] Referrer receives 100 coins automatically
- [ ] Referrer's tree shows new referral as level 1 (blue)
- [ ] Second-level referral triggers 40 coin reward
- [ ] Third-level referral triggers 15 coin reward
- [ ] Tree displays all 3 levels with correct indentation
- [ ] Transaction history logs all referral rewards

---

## 🎯 Success Criteria

### Visual Design Matches Diagram
- ✅ 4-tab bottom navigation (Home/Referral/Wallet/Profile)
- ✅ Rounded pill design with border stroke
- ✅ Selected tab highlighted in accent blue
- ✅ Icons + labels for each tab

### Referral Tree Visualization
- ✅ Shows one person above (referrer) if exists
- ✅ Shows current user ("YOU") in prominent gold card
- ✅ Shows all descendants below (max 3 levels)
- ✅ Color-coded by level: Blue (L1), Green (L2), Orange (L3)
- ✅ Indented to show hierarchy (24dp per level)
- ✅ Displays name, referral code, coins for each node
- ✅ Shows total descendants count

### Referral System Functionality
- ✅ Referral codes auto-filled from encrypted links
- ✅ Rewards credited instantly on registration
- ✅ Multi-level tracking (3 levels deep)
- ✅ Correct reward amounts: 100 / 40 / 15 coins
- ✅ Transaction logs created for audit trail
- ✅ referral_chains table tracks relationships

### Data Integrity
- ✅ No more quoted strings in database
- ✅ Wallet balances accurate across all users
- ✅ Referral chain relationships preserved
- ✅ Transaction history immutable (append-only)

---

## 📝 Next Steps (Optional Enhancements)

1. **Add Bank Details Storage**
   - Create `bank_details` table
   - Add POST endpoint: `/users/bank-details`
   - Store account holder name, account number, IFSC, UPI ID
   - Show saved details in WalletScreen redeem dialog

2. **Enhance Terms & Conditions**
   - Fetch from backend config instead of hardcoded
   - Add "View Full T&C" link to external page
   - Track user acceptance with timestamp

3. **Add Referral Analytics**
   - Conversion rate (registrations → event attendees)
   - Top referrers leaderboard
   - Monthly/weekly performance charts

4. **Push Notifications for Referrals**
   - Notify when someone uses your code
   - Notify when rewards are credited
   - Notify when descendants reach milestones

5. **Gamification**
   - Referral streaks (consecutive days with new referrals)
   - Achievement badges (10/50/100 referrals milestones)
   - Seasonal challenges with bonus rewards

---

## 🆘 Support & Debugging

### View Server Logs
```powershell
cd D:\cinepass\server
npm start | Out-File -FilePath logs.txt
```

### View Android Logs
```powershell
adb logcat -v time > android_logs.txt
```

### Database Queries for Debugging
```sql
-- Check referral chain for a user
SELECT 
  rc.level,
  u.name AS referred_user,
  u.referral_code,
  rc.reward_amount,
  rc.created_at
FROM referral_chains rc
JOIN users u ON u.id = rc.referred_id
WHERE rc.referrer_id = YOUR_USER_ID
ORDER BY rc.created_at DESC;

-- Check wallet balance
SELECT u.name, w.coins, w.total_earned, w.total_spent
FROM users u
JOIN wallets w ON w.user_id = u.id
WHERE u.id = YOUR_USER_ID;

-- Check for quoted strings (should return 0 after cleanup)
SELECT COUNT(*) FROM users WHERE name LIKE '"%"' OR email LIKE '"%"';
```

---

## ✅ Final Verification

After completing all tests, verify:

1. **App opens without crashes**
2. **Bottom navigation visible on all 4 main screens**
3. **Tab switching works in both directions**
4. **Profile screen shows referral tree (or empty state)**
5. **New registrations with referral codes trigger rewards**
6. **Tree updates in real-time when new referrals join**
7. **No quoted strings in database after new registrations**

**If all items checked:** 🎉 **Integration Successful!**

