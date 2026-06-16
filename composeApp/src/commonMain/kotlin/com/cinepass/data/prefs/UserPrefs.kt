package com.cinepass.data.prefs

import com.russhwolf.settings.Settings
import com.cinepass.utils.Constants

class UserPrefs(private val settings: Settings = Settings()) {

    constructor(context: Any?) : this(Settings())

    var rememberedIdentifier: String?
        get() = settings.getStringOrNull("remembered_identifier")
        set(value) {
            if (value != null) {
                settings.putString("remembered_identifier", value)
            } else {
                settings.remove("remembered_identifier")
            }
        }

    var accessToken: String?
        get() = settings.getStringOrNull(Constants.KEY_ACCESS_TOKEN)
        set(value) {
            if (value != null) {
                settings.putString(Constants.KEY_ACCESS_TOKEN, value)
            } else {
                settings.remove(Constants.KEY_ACCESS_TOKEN)
            }
        }

    var refreshToken: String?
        get() = settings.getStringOrNull(Constants.KEY_REFRESH_TOKEN)
        set(value) {
            val trimmed = value?.trim('"', ' ')
            if (trimmed != null) {
                settings.putString(Constants.KEY_REFRESH_TOKEN, trimmed)
            } else {
                settings.remove(Constants.KEY_REFRESH_TOKEN)
            }
        }

    var userId: String?
        get() = settings.getStringOrNull(Constants.KEY_USER_ID)
        set(value) {
            val trimmed = value?.trim('"', ' ')
            if (trimmed != null) {
                settings.putString(Constants.KEY_USER_ID, trimmed)
            } else {
                settings.remove(Constants.KEY_USER_ID)
            }
        }

    var userName: String?
        get() = settings.getStringOrNull(Constants.KEY_USER_NAME)
        set(value) {
            val trimmed = value?.trim('"', ' ')
            if (trimmed != null) {
                settings.putString(Constants.KEY_USER_NAME, trimmed)
            } else {
                settings.remove(Constants.KEY_USER_NAME)
            }
        }

    var userEmail: String?
        get() = settings.getStringOrNull(Constants.KEY_USER_EMAIL)
        set(value) {
            val trimmed = value?.trim('"', ' ')
            if (trimmed != null) {
                settings.putString(Constants.KEY_USER_EMAIL, trimmed)
            } else {
                settings.remove(Constants.KEY_USER_EMAIL)
            }
        }

    var userPhone: String?
        get() = settings.getStringOrNull(Constants.KEY_USER_PHONE)
        set(value) {
            val trimmed = value?.trim('"', ' ')
            if (trimmed != null) {
                settings.putString(Constants.KEY_USER_PHONE, trimmed)
            } else {
                settings.remove(Constants.KEY_USER_PHONE)
            }
        }

    var referralCode: String?
        get() = settings.getStringOrNull(Constants.KEY_REFERRAL_CODE)
        set(value) {
            val trimmed = value?.trim('"', ' ')
            if (trimmed != null) {
                settings.putString(Constants.KEY_REFERRAL_CODE, trimmed)
            } else {
                settings.remove(Constants.KEY_REFERRAL_CODE)
            }
        }

    var coins: Int
        get() = settings.getInt(Constants.KEY_COINS, 0)
        set(value) = settings.putInt(Constants.KEY_COINS, value)

    var isVerified: Boolean
        get() = settings.getBoolean(Constants.KEY_IS_VERIFIED, false)
        set(value) = settings.putBoolean(Constants.KEY_IS_VERIFIED, value)

    var selfieUrl: String?
        get() = settings.getStringOrNull(Constants.KEY_SELFIE_URL)
        set(value) {
            val trimmed = value?.trim('"', ' ')
            if (trimmed != null) {
                settings.putString(Constants.KEY_SELFIE_URL, trimmed)
            } else {
                settings.remove(Constants.KEY_SELFIE_URL)
            }
        }

    /** True once the user has completed the first-launch onboarding slides. */
    var hasSeenOnboarding: Boolean
        get() = settings.getBoolean("has_seen_onboarding", false)
        set(value) = settings.putBoolean("has_seen_onboarding", value)

    val bearerToken: String
        get() = "Bearer $accessToken"

    val isLoggedIn: Boolean
        get() = accessToken != null

    fun saveAuthData(
        accessToken: String?,
        refreshToken: String?,
        userId: String?,
        name: String?,
        email: String?,
        phone: String?,
        referralCode: String?,
        coins: Int,
        isVerified: Boolean,
        selfieUrl: String? = null
    ) {
        val cleanAccessToken = accessToken?.trim('"', ' ')
        if (cleanAccessToken != null) settings.putString(Constants.KEY_ACCESS_TOKEN, cleanAccessToken) else settings.remove(Constants.KEY_ACCESS_TOKEN)

        val cleanRefreshToken = refreshToken?.trim('"', ' ')
        if (cleanRefreshToken != null) settings.putString(Constants.KEY_REFRESH_TOKEN, cleanRefreshToken) else settings.remove(Constants.KEY_REFRESH_TOKEN)

        val cleanUserId = userId?.trim('"', ' ')
        if (cleanUserId != null) settings.putString(Constants.KEY_USER_ID, cleanUserId) else settings.remove(Constants.KEY_USER_ID)

        val cleanName = name?.trim('"', ' ')
        if (cleanName != null) settings.putString(Constants.KEY_USER_NAME, cleanName) else settings.remove(Constants.KEY_USER_NAME)

        val cleanEmail = email?.trim('"', ' ')
        if (cleanEmail != null) settings.putString(Constants.KEY_USER_EMAIL, cleanEmail) else settings.remove(Constants.KEY_USER_EMAIL)

        val cleanPhone = phone?.trim('"', ' ')
        if (cleanPhone != null) settings.putString(Constants.KEY_USER_PHONE, cleanPhone) else settings.remove(Constants.KEY_USER_PHONE)

        val cleanReferral = referralCode?.trim('"', ' ')
        if (cleanReferral != null) settings.putString(Constants.KEY_REFERRAL_CODE, cleanReferral) else settings.remove(Constants.KEY_REFERRAL_CODE)

        settings.putInt(Constants.KEY_COINS, coins)
        settings.putBoolean(Constants.KEY_IS_VERIFIED, isVerified)

        val cleanSelfie = selfieUrl?.trim('"', ' ')
        if (cleanSelfie != null) settings.putString(Constants.KEY_SELFIE_URL, cleanSelfie) else settings.remove(Constants.KEY_SELFIE_URL)
    }

    fun saveRememberedIdentifier(identifier: String?) {
        if (identifier != null) {
            settings.putString("remembered_identifier", identifier)
        } else {
            settings.remove("remembered_identifier")
        }
    }

    fun setFcmToken(token: String) {
        settings.putString("fcm_token", token)
    }

    fun getFcmToken(): String? {
        return settings.getStringOrNull("fcm_token")
    }

    fun clearFcmToken() {
        settings.remove("fcm_token")
    }

    fun clear() = settings.clear()
}
