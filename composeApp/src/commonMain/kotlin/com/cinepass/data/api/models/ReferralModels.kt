package com.cinepass.data.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ReferralStatsData(
    val referralCode: String,
    val referralLink: String,
    val totalReferrals: Int,
    val totalCoinsEarned: Int,
    val referrals: List<ReferralItemData> = emptyList(),
)

@Serializable
data class ReferralItemData(
    val id: String,
    val name: String,
    val status: String,
    @SerialName("coinsEarned") val coinsEarned: Int,
    @SerialName("createdAt") val createdAt: String,
)


