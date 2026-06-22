package com.cinepass.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class WalletData(
    val coins: Int,
    val rupeeValue: String,
    val totalEarned: Int,
    val totalSpent: Int,
    val totalReferrals: Int
)

@Serializable
data class TransactionData(
    val id: String,
    val type: String,
    val coins: Int,
    val description: String,
    val createdAt: String
)

@Serializable
data class PaginatedResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val pagination: PaginationData
)

@Serializable
data class PaginationData(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

@Serializable
data class RedeemRequest(
    val coins: Int,
    val upiId: String
)

