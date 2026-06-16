package com.cinepass.data.models

// ─── User (from users table) ────────────
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val gender: String? = null,
    val dob: String? = null,
    val referralCode: String,           // RS3_XXXXXX format
    val referredByCode: String,         // Code entered at registration
    val referredByUserId: Int? = null,
    val coins: Int = 0,                 // coin_balance from backend
    val status: String = "pending",     // pending / verified / ambassador / suspended
    val selfieUrl: String? = null,
    val otpVerified: Boolean = false,
    val referrals: Int = 0,             // Count of direct referrals (Level 1)
    val createdAt: String
)

// ─── Event ──────────────────────────────
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val type: EventType,
    val bannerUrl: String,
    val teaserUrl: String? = null,
    val date: String,
    val venue: String,
    val city: String,
    val organizerId: String,
    val isInviteOnly: Boolean = false,
    val ticketPrice: Int,
    val discountForRegistered: Int = 0, // % discount for registered users
    val coinsPerReferral: Int = 50,
    val status: EventStatus = EventStatus.UPCOMING
)

enum class EventType {
    MOVIE, CONCERT, SPORTS, CONFERENCE, MEETUP, OTHER
}

enum class EventStatus {
    UPCOMING, LIVE, COMPLETED, CANCELLED
}

// ─── Ticket ─────────────────────────────
data class Ticket(
    val id: String,
    val eventId: String,
    val userId: String,
    val eventTitle: String,
    val eventDate: String,
    val venue: String,
    val seatInfo: String,
    val qrData: String,             // Data encoded in QR
    val isUsed: Boolean = false,
    val isPriority: Boolean = false // Priority booking user
)

// ─── Wallet (computed from coin_transactions) ─────
data class Wallet(
    val userId: Int,
    val totalCoins: Int,
    val transactions: List<WalletTransaction>
)

data class WalletTransaction(
    val id: Int,
    val type: String,                   // earned_referral_l1 / earned_referral_l2 / earned_bonus / redeemed_offer / admin_adjustment
    val coins: Int,                     // positive = credit, negative = debit
    val note: String?,                  // human-readable description
    val createdAt: String
)

// ─── Referral (from referral_chains table) ─────
data class Referral(
    val id: Int,
    val referrerId: Int,
    val refereeId: Int,
    val level: Int,                     // 1=direct, 2=one level up, 3=two levels up
    val rewardCoins: Int?,              // Snapshot of coins at registration
    val status: String,                 // pending / awarded / cancelled
    val rewardedat: String?,            // When coins were credited
    val createdAt: String
)

// ─── Offer (from offers table) ──────────────────
data class Offer(
    val id: Int,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val rewardType: String,             // movie_ticket / event_pass / coupon_code / merchandise / custom
    val rewardValue: String,            // The actual code/ticket reference
    val coinCost: Int = 0,              // Coins required to claim
    val targetReferrals: Int? = null,   // Number of referrals required (if milestone-based)
    val page: String,                   // referral / wallet / home / global
    val displayOrder: Int = 0,
    val active: Boolean = true,
    val maxClaims: Int? = null,
    val claimsCount: Int = 0,
    val validFrom: String? = null,
    val validUntil: String? = null,
    val claimed: Boolean = false        // For user context: if user has already claimed
)

// ─── Offer Redemption ────────────────────────────
data class OfferRedemption(
    val id: Int,
    val userId: Int,
    val offerId: Int,
    val coinsSpent: Int?,
    val rewardSent: String,             // The code/ticket sent to user
    val status: String,                 // claimed / fulfilled / expired / cancelled
    val claimedAt: String,
    val fulfilledAt: String? = null
)

// ─── Feed Post (from feed_posts table) ──────────
data class FeedPost(
    val id: Int,
    val layout: String,                 // hero / card / reel / banner / update / grid2
    val title: String,
    val subtitle: String? = null,
    val body: String? = null,
    val contentType: String,            // image / video / text / link
    val mediaUrl: String? = null,
    val thumbnailUrl: String? = null,   // For video posts
    val linkUrl: String? = null,
    val status: String,                 // draft / live / scheduled
    val createdAt: String
)

// ─── Point Rules (from point_rules table) ───────
data class PointRule(
    val id: Int,
    val ruleKey: String,                // snake_case identifier
    val label: String,                  // Display name
    val coins: Int,
    val ruleType: String,               // referral_level / registration_bonus / event_bonus / custom
    val levelNumber: Int? = null,       // For referral_level: 1, 2, 3, etc.
    val active: Boolean = true,
    val description: String? = null
)

// ─── App Content (from app_content table) ───────
data class AppContent(
    val id: Int,
    val contentKey: String,
    val contentType: String,            // text / richtext / image_url / number / url / boolean
    val value: String,
    val label: String,
    val section: String                 // splash / onboarding / legal / contact / wallet / branding / social / system
)

// ─── Referral Tree (for tree visualization) ─────
data class TreeNode(
    val id: Int,
    val name: String,
    val level: Int,
    val coinsEarned: Int,
    val children: List<TreeNode> = emptyList()
)

data class ReferralTreeStats(
    val totalMembers: Int,
    val totalEarnings: Int,
    val maxLevel: Int,
    val levelCounts: Map<Int, Int>      // Level -> count of members at that level
)
