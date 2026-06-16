// ─── AuthModels.kt ────────────────────────────────────────────────
package com.rs3films.app.data.api.models

import com.google.gson.annotations.SerializedName

// ── Request bodies ──
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    @SerializedName("referralCode") val referralCode: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class VerifyOtpRequest(
    val phone: String,
    val code: String
)

// ── Responses ──
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class AuthData(
    val user: UserData,
    val accessToken: String,
    val refreshToken: String,
    val referredBy: String? = null
)

data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val referralCode: String,
    val isVerified: Boolean,
    val isAmbassador: Boolean = false,
    val coins: Int = 0
)

// ─── EventModels.kt ───────────────────────────────────────────────
data class EventData(
    val id: String,
    val title: String,
    val type: String,
    val description: String?,
    val bannerUrl: String?,
    val venue: String,
    val city: String,
    val eventDate: String,
    val priorityDeadline: String,
    val coinsPerReferral: Int,
    val attendanceBonus: Int,
    val organizer: OrganizerData,
    @SerializedName("_count") val count: CountData?
)

data class OrganizerData(val name: String)
data class CountData(val registrations: Int)

data class ShowtimeData(
    val id: String,
    val startTime: String,
    val format: String,
    val price: Double,
    val availableSeats: Int,
    val prioritySeats: Int
)

data class RegisterEventRequest(val eventId: String)
data class RegisterEventResponse(
    val registration: RegistrationData,
    val hasPriority: Boolean,
    val message: String
)
data class RegistrationData(val id: String, val hasPriority: Boolean)

// ─── TicketModels.kt ──────────────────────────────────────────────
data class BookTicketRequest(
    val showtimeId: String,
    val seats: List<String>,
    val coinsToUse: Int = 0,
    val paymentId: String? = null
)

data class TicketData(
    val id: String,
    val seats: List<String>,
    val totalPrice: Double,
    val coinsUsed: Int,
    val discount: Double,
    val finalPrice: Double,
    val qrCode: String,
    val status: String,
    val event: String,
    val showtime: String,
    val format: String,
    val venue: String
)

data class BookTicketResponse(
    val ticket: TicketData,
    val pricing: PricingData
)

data class PricingData(
    val originalPrice: Double,
    val coinDiscount: Double,
    val priorityDiscount: Double,
    val totalDiscount: Double,
    val finalPrice: Double,
    val coinsSaved: Int
)

data class MyTicketsResponse(
    val upcoming: List<TicketWithShowtime>,
    val past: List<TicketWithShowtime>,
    val total: Int
)

data class TicketWithShowtime(
    val id: String,
    val seats: List<String>,
    val finalPrice: Double,
    val qrCode: String,
    val status: String,
    val showtime: ShowtimeWithEvent
)

data class ShowtimeWithEvent(
    val startTime: String,
    val format: String,
    val event: EventSummary
)

data class EventSummary(
    val title: String,
    val venue: String,
    val city: String
)

// ─── WalletModels.kt ──────────────────────────────────────────────
data class WalletData(
    val coins: Int,
    val rupeeValue: String,
    val totalEarned: Int,
    val totalSpent: Int,
    val totalReferrals: Int
)

data class TransactionData(
    val id: String,
    val type: String,
    val coins: Int,
    val description: String,
    val createdAt: String
)

data class PaginatedResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val pagination: PaginationData
)

data class PaginationData(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class RedeemRequest(
    val coins: Int,
    val upiId: String
)
