package com.cinepass.data.models

// ─── User ───────────────────────────────────────────────
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val referralCode: String,       // Unique code to share
    val referredBy: String? = null, // Who referred this user
    val walletBalance: Int = 0,     // Coins in wallet
    val badges: List<String> = emptyList(),
    val profileImage: String? = null
)

// ─── Event ──────────────────────────────────────────────
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

// ─── Ticket ─────────────────────────────────────────────
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

// ─── Wallet ─────────────────────────────────────────────
data class Wallet(
    val userId: String,
    val totalCoins: Int,
    val transactions: List<WalletTransaction>
)

data class WalletTransaction(
    val id: String,
    val type: TransactionType,
    val coins: Int,
    val description: String,
    val date: String
)

enum class TransactionType {
    EARNED_REFERRAL,
    EARNED_ATTENDANCE,
    REDEEMED_TICKET,
    REDEEMED_CASH
}

// ─── Referral ────────────────────────────────────────────
data class Referral(
    val id: String,
    val referrerId: String,
    val referredUserId: String,
    val eventId: String,
    val coinsEarned: Int,
    val status: ReferralStatus,
    val date: String
)

enum class ReferralStatus {
    PENDING,    // Referred user registered but not attended
    CONFIRMED,  // Referred user attended — coins released
    EXPIRED
}

// ─── API Response Wrapper ────────────────────────────────
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)
