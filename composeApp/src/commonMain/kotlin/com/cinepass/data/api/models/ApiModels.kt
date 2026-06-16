package com.cinepass.data.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// ─── Request bodies ───────────────
@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val gender: String,
    val dob: String,
    val address_line: String,
    val city: String,
    val state: String,
    val pincode: String,
    val password: String,
    val confirmPassword: String,
    @SerialName("referral_code") val referralCode: String? = null,
    val otp: String
)

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val code: String
)

// ─── Responses ───────────────
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

@Serializable
data class AuthData(
    val user: UserData,
    val accessToken: String = "",
    val refreshToken: String = "",
    val referredBy: String? = null
)

@Serializable
data class ReferralLevel(
    val level: Int,
    val count: Int,
    val earned: Int
)

@Serializable
data class ReferralStats(
    val directReferrals: Int = 0,
    val totalEarnings: Int = 0,
    val byLevel: List<ReferralLevel> = emptyList(),
    val shareLink: String = ""
)

@Serializable
data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val referralCode: String = "",
    val isVerified: Boolean = true,
    val isAmbassador: Boolean = false,
    val coins: Int = 0,
    val selfieUrl: String? = null,
    val referralStats: ReferralStats? = null
)

