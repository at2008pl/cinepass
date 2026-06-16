package com.cinepass.data.repository

import com.cinepass.data.api.ApiService
import com.cinepass.data.prefs.UserPrefs

class ReferralRepository(
    private val apiService: ApiService,
    private val userPrefs: UserPrefs,
) {
    suspend fun getReferralCode(): String {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                response.body()?.referralCode ?: ""
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }
    
    fun getReferralLink(code: String): String {
        return "https://fanverse.app/invite/$code"
    }
}

