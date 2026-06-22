package com.cinepass.data.repository

import com.cinepass.data.api.ApiService
import com.cinepass.data.api.models.ReferralStats
import com.cinepass.data.models.ReferralTreeResponse
import com.cinepass.data.prefs.UserPrefs

data class ProfileData(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val referralCode: String,
    val coins: Int,
    val isAmbassador: Boolean,
    val selfieUrl: String? = null,
    val referralStats: ReferralStats? = null,
)

class ProfileRepository(
    private val apiService: ApiService,
    private val userPrefs: UserPrefs,
) {
    suspend fun getProfile(): ProfileData {
        val response = apiService.getProfile(userPrefs.bearerToken)
        val data = response.body()?.data
        val profile = ProfileData(
            id = data?.id?.trim('"', ' ') ?: (userPrefs.userId ?: ""),
            name = data?.name?.trim('"', ' ') ?: (userPrefs.userName ?: "Fan"),
            email = data?.email?.trim('"', ' ') ?: (userPrefs.userEmail ?: ""),
            phone = data?.phone?.trim('"', ' ') ?: (userPrefs.userPhone ?: ""),
            referralCode = data?.referralCode?.trim('"', ' ') ?: (userPrefs.referralCode ?: ""),
            coins = data?.coins ?: userPrefs.coins,
            isAmbassador = data?.isAmbassador ?: false,
            selfieUrl = normalizeMediaUrl(data?.selfieUrl?.trim('"', ' ') ?: userPrefs.selfieUrl),
            referralStats = data?.referralStats
        )

        userPrefs.userId = profile.id
        userPrefs.userName = profile.name
        userPrefs.userEmail = profile.email
        userPrefs.userPhone = profile.phone
        userPrefs.referralCode = profile.referralCode
        userPrefs.coins = profile.coins
        userPrefs.selfieUrl = profile.selfieUrl

        return profile
    }

    suspend fun getReferralTree(): ReferralTreeResponse {
        val response = apiService.getReferralTree(userPrefs.bearerToken)
        if (response.isSuccessful && response.body()?.success == true) {
            return response.body()!!.data!!
        } else {
            throw Exception("Failed to load referral tree")
        }
    }

    private fun normalizeMediaUrl(url: String?): String? {
        val cleaned = url?.trim('"', ' ') ?: return null
        if (cleaned.isBlank()) return null
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) return cleaned
        val serverRoot = com.cinepass.utils.Constants.BASE_URL.removeSuffix("api/")
        return serverRoot + cleaned.trimStart('/')
    }
}

