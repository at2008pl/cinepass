# RS³ Films Android App Integration Plan

## ✅ Completed

### 1. Backend Configuration
- Updated `Constants.kt` to point to fanverse_web backend (http://192.168.29.211:4001/v1/)
- Created `Rs3ApiModels.kt` with complete data models matching backend schema
- Created `Rs3ApiService.kt` with all app endpoints
- Updated `ApiClient.kt` to expose `rs3Api` instance

## 🚀 Implementation Roadmap

### Phase 1: Core Infrastructure (CURRENT)
- [x] API models and service setup
- [ ] Authentication flow (OTP-based login)
- [ ] Secure token storage (Android Keystore)
- [ ] Retrofit interceptor for auto-token refresh

### Phase 2: Main Screens (According to RS3FilmsApp_final.jsx)
- [ ] HomeScreen - Feed + Offers carousel
- [ ] WalletScreen - Coins + Transactions
- [ ] ProfileScreen - User info + Edit
- [ ] ReferralScreen - Stats + Invite code

### Phase 3: Secondary Screens
- [ ] OfferDetailScreen
- [ ] ReferralTreeScreen - Visualize referral chain
- [ ] My RewardsScreen - Claimed offers
- [ ] SettingsScreen

### Phase 4: Navigation & Polish
- [ ] Update AppNavigation.kt with all routes
- [ ] Bottom navigation bar
- [ ] Deep linking for referral codes
- [ ] Push notifications setup

## 📐 Design System (From RS3FilmsApp_final.jsx)

### Colors
```kotlin
val WarmIvory = Color(0xFFFAF7F2)
val ChampagneGold = Color(0xFFA67C2E)
val DeepEbony = Color(0xFF1C1408)
val GoldBright = Color(0xFFC9A84C)
val MutedGray = Color(0xFFA89880)
```

### Typography
- Display: Cormorant Garamond (serif, elegant)
- Body: Outfit (sans-serif, modern)

### Key UI Components
1. **Rs3GradientButton** - Gold gradient CTA
2. **OfferCard** - Image + title + referral badge
3. **FeedPostCard** - Layout types: image, video, text, twocolumn
4. **CoinBalance** - Animated coin counter
5. **ReferralCode** - Copy-to-clipboard card
6. **ReferralTreeNode** - Visual tree diagram

## 🔗 API Integration Points

### Authentication
- POST `/v1/auth/app/otp/send` → Send OTP
- POST `/v1/auth/app/otp/verify` → Login & get JWT

### Home Feed
- GET `/v1/app/feed?page=1&limit=20` → Get posts
- GET `/v1/app/offers?page=home` → Get home offers

### Profile
- GET `/v1/app/member/profile` → User data + stats
- PUT `/v1/app/member/profile` → Update name/email

### Wallet
- GET `/v1/app/member/profile` → Current coin balance
- GET endpoint needed for transactions (TODO: check backend)

### Referrals
- GET `/v1/app/member/profile` → Referral code + count
- POST `/v1/app/referral/apply` → Apply referral code
- GET endpoint needed for referral tree (TODO: check backend)

### Offers
- GET `/v1/app/offers?page=referral` → Referral offers
- GET `/v1/app/offers?page=wallet` → Wallet offers
- POST `/v1/app/offers/:id/claim` → Claim offer

## 📝 Next Steps

1. Read RS3FilmsApp_final.jsx HomeScreen section
2. Implement HomeViewModel with RS³ API calls
3. Design HomeScreen UI with Compose matching JSX design
4. Repeat for Wallet, Profile, Referral screens
5. Test end-to-end with backend running

## 🔧 Backend Notes

- Server runs on `localhost:4001`
- Database: PostgreSQL (Fanverse)
- JWT expires in 7 days
- OTP mock code: `123456`
- All app routes prefixed with `/v1/app/`

