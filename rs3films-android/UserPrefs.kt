package com.rs3films.app.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.rs3films.app.utils.Constants

class UserPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    var accessToken: String?
        get() = prefs.getString(Constants.KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(Constants.KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(Constants.KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(Constants.KEY_REFRESH_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(Constants.KEY_USER_ID, null)
        set(value) = prefs.edit().putString(Constants.KEY_USER_ID, value).apply()

    var userName: String?
        get() = prefs.getString(Constants.KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(Constants.KEY_USER_NAME, value).apply()

    var userEmail: String?
        get() = prefs.getString(Constants.KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(Constants.KEY_USER_EMAIL, value).apply()

    var userPhone: String?
        get() = prefs.getString(Constants.KEY_USER_PHONE, null)
        set(value) = prefs.edit().putString(Constants.KEY_USER_PHONE, value).apply()

    var referralCode: String?
        get() = prefs.getString(Constants.KEY_REFERRAL_CODE, null)
        set(value) = prefs.edit().putString(Constants.KEY_REFERRAL_CODE, value).apply()

    var coins: Int
        get() = prefs.getInt(Constants.KEY_COINS, 0)
        set(value) = prefs.edit().putInt(Constants.KEY_COINS, value).apply()

    var isVerified: Boolean
        get() = prefs.getBoolean(Constants.KEY_IS_VERIFIED, false)
        set(value) = prefs.edit().putBoolean(Constants.KEY_IS_VERIFIED, value).apply()

    val bearerToken: String get() = "Bearer $accessToken"

    val isLoggedIn: Boolean get() = accessToken != null

    fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: String,
        name: String,
        email: String,
        phone: String,
        referralCode: String,
        coins: Int,
        isVerified: Boolean
    ) {
        prefs.edit().apply {
            putString(Constants.KEY_ACCESS_TOKEN, accessToken)
            putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
            putString(Constants.KEY_USER_ID, userId)
            putString(Constants.KEY_USER_NAME, name)
            putString(Constants.KEY_USER_EMAIL, email)
            putString(Constants.KEY_USER_PHONE, phone)
            putString(Constants.KEY_REFERRAL_CODE, referralCode)
            putInt(Constants.KEY_COINS, coins)
            putBoolean(Constants.KEY_IS_VERIFIED, isVerified)
            apply()
        }
    }

    fun clear() = prefs.edit().clear().apply()
}
