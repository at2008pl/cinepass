package com.rs3films.app.utils

object Constants {
    // ⚠️ CHANGE THIS to your computer's IP address
    // Find it with: ifconfig (Mac/Linux) or ipconfig (Windows)
    // Example: "http://192.168.1.5:3000/api/"
    const val BASE_URL = "http://10.0.2.2:3000/api/"
    // Note: 10.0.2.2 works for Android Emulator to reach localhost
    // For real device: use your WiFi IP e.g. "http://192.168.1.5:3000/api/"

    // Prefs keys
    const val PREFS_NAME = "rs3films_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_PHONE = "user_phone"
    const val KEY_REFERRAL_CODE = "referral_code"
    const val KEY_COINS = "coins"
    const val KEY_IS_VERIFIED = "is_verified"

    // Coin config
    const val COIN_TO_RUPEE = 0.1  // 1 coin = ₹0.10
    const val MAX_COIN_DISCOUNT_PERCENT = 0.30 // max 30% via coins

    // RS3 Films movie event ID (from seeded data)
    const val MOVIE_EVENT_ID = "event-kalki-001"
    const val MOVIE_TITLE = "ಲವ್ಕರ್"  // Kannada title from poster
    const val MOVIE_TAGLINE = "An emotional journey of love"
}
