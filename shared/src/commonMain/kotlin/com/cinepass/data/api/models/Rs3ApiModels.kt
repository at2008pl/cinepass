package com.cinepass.data.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/* ═══════════════════════════════════════════════════════════════════════════
   RS³ FILMS API Models  —  Fanverse Backend Integration
   Matches: fanverse_web/backend schema
   Refactored for Kotlin Multiplatform Serialization
   ═══════════════════════════════════════════════════════════════════════════ */

// ── Base Response Wrappers ──────────────────────────────────────────────────

@Serializable
data class Rs3Response<T>(
    @SerialName("data") val data: T? = null,
    @SerialName("error") val error: Rs3Error? = null
)

@Serializable
data class Rs3Error(
    @SerialName("code") val code: String,
    @SerialName("message") val message: String
)

@Serializable
data class Rs3PaginatedResponse<T>(
    @SerialName("data") val data: List<T>,
    @SerialName("total") val total: Int,
    @SerialName("page") val page: Int,
    @SerialName("limit") val limit: Int
)

// ── Authentication Models ───────────────────────────────────────────────────

@Serializable
data class OtpSendRequest(
    @SerialName("phone") val phone: String
)

@Serializable
data class OtpVerifyRequest(
    @SerialName("phone") val phone: String,
    @SerialName("otp") val otp: String
)

typealias VerifyOtpRequest = OtpVerifyRequest

@Serializable
data class OtpVerifyResponse(
    @SerialName("token") val token: String,
    @SerialName("user") val user: Rs3User
)

@Serializable
data class Rs3User(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String?,
    @SerialName("phone") val phone: String,
    @SerialName("email") val email: String?,
    @SerialName("coins") val coins: Int = 0,
    @SerialName("referral_code") val referralCode: String?,
    @SerialName("status") val status: String = "active",
    @SerialName("created_at") val createdAt: String?
)

// ── Profile Models ──────────────────────────────────────────────────────────

@Serializable
data class Rs3Profile(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String?,
    @SerialName("phone") val phone: String,
    @SerialName("email") val email: String?,
    @SerialName("coins") val coins: Int,
    @SerialName("referral_code") val referralCode: String,
    @SerialName("status") val status: String,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("referrals") val referralsCount: Int = 0
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("name") val name: String?,
    @SerialName("email") val email: String?
)

// ── Offer Models ────────────────────────────────────────────────────────────

@Serializable
data class Rs3Offer(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("page") val page: String, // "referral", "wallet", "home"
    @SerialName("target_referrals") val targetReferrals: Int?,
    @SerialName("coin_cost") val coinCost: Int = 0,
    @SerialName("reward_value") val rewardValue: String?,
    @SerialName("valid_until") val validUntil: String?,
    @SerialName("max_claims") val maxClaims: Int?,
    @SerialName("claims_count") val claimsCount: Int = 0,
    @SerialName("active") val active: Boolean = true,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("claimed") val claimed: Boolean = false
)

@Serializable
data class ClaimOfferResponse(
    @SerialName("redemption_id") val redemptionId: Int,
    @SerialName("reward_value") val rewardValue: String?,
    @SerialName("status") val status: String
)

// ── Feed Post Models ────────────────────────────────────────────────────────

@Serializable
data class Rs3FeedPost(
    @SerialName("id") val id: Int,
    @SerialName("layout") val layout: String,
    @SerialName("title") val title: String?,
    @SerialName("subtitle") val subtitle: String?,
    @SerialName("content_type") val type: String?,
    @SerialName("body") val body: String?,
    @SerialName("link_url") val link: String?,
    @SerialName("media_url") val mediaUrl: String?,
    @SerialName("thumbnail_url") val thumbnailUrl: String?,
    @SerialName("created_at") val createdAt: String?
)

// ── Referral Models ─────────────────────────────────────────────────────────

@Serializable
data class Rs3ReferralEntry(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String?,
    @SerialName("parent_id") val parentId: Int?,
    @SerialName("level") val level: Int,
    @SerialName("coins_awarded") val coinsAwarded: Int,
    @SerialName("join_date") val joinDate: String?
)

@Serializable
data class Rs3ReferralTreeResponse(
    @SerialName("referrals") val referrals: List<Rs3ReferralEntry>,
    @SerialName("chain_depth") val chainDepth: Int,
    @SerialName("total_chain_members") val totalChainMembers: Int
)

@Serializable
data class ApplyReferralRequest(
    @SerialName("referral_code") val referralCode: String
)

@Serializable
data class Rs3ReferralChainItem(
    @SerialName("id") val id: Int,
    @SerialName("referee_id") val refereeId: Int,
    @SerialName("referee_name") val refereeName: String?,
    @SerialName("level") val level: Int,
    @SerialName("coins_awarded") val coinsAwarded: Int,
    @SerialName("created_at") val createdAt: String?
)

// ── Coin Transaction Models ─────────────────────────────────────────────────

@Serializable
data class Rs3CoinTransaction(
    @SerialName("id") val id: Int,
    @SerialName("type") val type: String, // "earned_referral_l1" / "redeemed_offer" / etc.
    @SerialName("coins") val coins: Int,   // positive = credit, negative = debit
    @SerialName("note") val note: String?,
    @SerialName("created_at") val createdAt: String?
)

// ── Wallet Models ───────────────────────────────────────────────────────────

@Serializable
data class Rs3WalletResponse(
    @SerialName("coins") val coins: Int,
    @SerialName("config") val config: Map<String, String>? = null
)

// ── Redemption Models ───────────────────────────────────────────────────────

@Serializable
data class Rs3Redemption(
    @SerialName("id") val id: Int,
    @SerialName("offer_id") val offerId: Int,
    @SerialName("offer_title") val offerTitle: String?,
    @SerialName("reward_value") val rewardValue: String?,
    @SerialName("status") val status: String, // "claimed", "fulfilled", "cancelled"
    @SerialName("created_at") val createdAt: String?,
    @SerialName("fulfilled_at") val fulfilledAt: String?
)

// ── App Config Models ───────────────────────────────────────────────────────

@Serializable
data class Rs3AppConfig(
    @SerialName("coin_to_rupee") val coinToRupee: Double? = 0.1,
    @SerialName("max_coin_discount_percent") val maxCoinDiscountPercent: Double? = 0.3,
    @SerialName("referral_l1_coins") val referralL1Coins: Int? = 100,
    @SerialName("referral_l2_coins") val referralL2Coins: Int? = 50,
    @SerialName("referral_l3_coins") val referralL3Coins: Int? = 25,
    @SerialName("app_version") val appVersion: String? = "1.0.0",
    @SerialName("force_update") val forceUpdate: Boolean = false
)

// ── CMS Item Models ─────────────────────────────────────────────────────────

@Serializable
data class Rs3CmsItem(
    @SerialName("id") val id: Int,
    @SerialName("section") val section: String,
    @SerialName("content_key") val contentKey: String, // DB column is content_key
    @SerialName("value") val value: String
)
