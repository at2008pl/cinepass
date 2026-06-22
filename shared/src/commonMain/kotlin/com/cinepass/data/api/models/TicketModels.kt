package com.cinepass.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class BookTicketRequest(
    val showtimeId: String,
    val seats: List<String>,
    val coinsToUse: Int = 0,
    val paymentId: String? = null
)

@Serializable
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

@Serializable
data class BookTicketResponse(
    val ticket: TicketData,
    val pricing: PricingData
)

@Serializable
data class PricingData(
    val originalPrice: Double,
    val coinDiscount: Double,
    val priorityDiscount: Double,
    val totalDiscount: Double,
    val finalPrice: Double,
    val coinsSaved: Int
)

@Serializable
data class MyTicketsResponse(
    val upcoming: List<TicketWithShowtime>,
    val past: List<TicketWithShowtime>,
    val total: Int
)

@Serializable
data class TicketWithShowtime(
    val id: String,
    val seats: List<String>,
    val finalPrice: Double,
    val qrCode: String,
    val status: String,
    val showtime: ShowtimeWithEvent
)

@Serializable
data class ShowtimeWithEvent(
    val startTime: String,
    val format: String,
    val event: EventSummary
)

@Serializable
data class EventSummary(
    val title: String,
    val venue: String,
    val city: String
)

