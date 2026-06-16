package com.cinepass.data.models

data class ReferralTreeNode(
    val id: Int,
    val name: String,
    val email: String,
    val referralCode: String,
    val coins: Int,
    val joinedDate: String,
    val children: List<ReferralTreeNode> = emptyList()
)

data class ReferralTreeResponse(
    val referrer: ReferralTreeNode?,
    val children: List<ReferralTreeNode>,
    val totalDescendants: Int
)
