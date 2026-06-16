package com.cinepass.utils

object Constants {
    // RS³ Films Backend API (fanverse_web)
    const val BASE_URL = "http://117.198.99.60:8055/v1/"
    const val PUBLIC_WEB_BASE_URL = "https://rs3films.com"
    const val APP_PACKAGE_NAME = "com.cinepass"
    
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
    const val KEY_SELFIE_URL = "selfie_url"
    
    // Coin config (loaded from server /app/config)
    const val COIN_TO_RUPEE = 0.1  // 1 coin = ₹0.10
    const val MAX_COIN_DISCOUNT_PERCENT = 0.30 // max 30% via coins
    
    // RS3 Films movie branding
    const val MOVIE_TITLE = "ಲವ್ಕರ್"  // Kannada title from poster
    const val MOVIE_TAGLINE = "An emotional journey of love"
    const val BRAND_NAME = "RS³ FILMS"
    const val MOVIE_EVENT_ID = "main-movie-event"
    
    // App config
    const val PAGINATION_LIMIT = 20
}
