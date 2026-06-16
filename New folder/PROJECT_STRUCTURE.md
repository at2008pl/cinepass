# CinePass - Android App Structure

## Tech Stack
- Language: Kotlin
- Architecture: MVVM + Clean Architecture
- UI: Jetpack Compose
- Navigation: Navigation Component
- DI: Hilt (Dagger)
- Network: Retrofit + OkHttp
- Local DB: Room
- State: StateFlow + ViewModel
- Image Loading: Coil

## Folder Structure

```
app/src/main/java/com/cinepass/
│
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   └── AuthViewModel.kt
│   │
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── FeedItem.kt
│   │
│   ├── events/
│   │   ├── EventListScreen.kt
│   │   ├── EventDetailScreen.kt
│   │   ├── EventViewModel.kt
│   │   └── CreateEventScreen.kt
│   │
│   ├── wallet/
│   │   ├── WalletScreen.kt
│   │   ├── WalletViewModel.kt
│   │   └── TransactionItem.kt
│   │
│   ├── tickets/
│   │   ├── TicketScreen.kt
│   │   ├── TicketViewModel.kt
│   │   └── QRCodeScreen.kt
│   │
│   └── profile/
│       ├── ProfileScreen.kt
│       ├── ProfileViewModel.kt
│       └── ReferralScreen.kt
│
├── data/
│   ├── api/
│   │   ├── ApiService.kt
│   │   └── ApiClient.kt
│   │
│   ├── models/
│   │   ├── User.kt
│   │   ├── Event.kt
│   │   ├── Ticket.kt
│   │   ├── Wallet.kt
│   │   └── Referral.kt
│   │
│   └── repository/
│       ├── AuthRepository.kt
│       ├── EventRepository.kt
│       ├── WalletRepository.kt
│       └── TicketRepository.kt
│
├── di/
│   ├── AppModule.kt
│   └── NetworkModule.kt
│
├── navigation/
│   └── AppNavigation.kt
│
├── utils/
│   ├── Extensions.kt
│   ├── QRCodeGenerator.kt
│   └── Constants.kt
│
└── MainActivity.kt
    CinePassApp.kt (Application class)
```
