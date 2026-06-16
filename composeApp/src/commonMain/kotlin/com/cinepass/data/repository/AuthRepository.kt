package com.cinepass.data.repository

import com.cinepass.data.api.ApiService
import com.cinepass.data.api.models.LoginRequest
import com.cinepass.data.prefs.UserPrefs

class AuthRepository(
    private val apiService: ApiService,
    private val userPrefs: UserPrefs,
) {
    suspend fun login(identifier: String, password: String): Result<Unit> {
        return runCatching {
            val response = apiService.login(LoginRequest(identifier = identifier, password = password))
            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.data == null) {
                error(body?.message ?: "Login failed")
            }

            val auth = body.data
            userPrefs.saveAuthData(
                accessToken = auth.accessToken,
                refreshToken = auth.refreshToken,
                userId = auth.user.id,
                name = auth.user.name,
                email = auth.user.email,
                phone = auth.user.phone,
                referralCode = auth.user.referralCode,
                coins = auth.user.coins,
                isVerified = auth.user.isVerified,
                selfieUrl = auth.user.selfieUrl,
            )
        }
    }
}

