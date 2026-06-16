package com.cinepass.data.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class EventData(
    val id: String,
    val title: String,
    val venue: String,
    val city: String,
    val description: String?,
    val showtimes: List<ShowtimeData>?,
    val coinsPerReferral: Int,
    val attendanceBonus: Int,
    val organizer: OrganizerData,
    @SerialName("_count") val count: CountData?
)

@Serializable
data class OrganizerData(val name: String)

@Serializable
data class CountData(val registrations: Int)

@Serializable
data class ShowtimeData(
    val id: String,
    val startTime: String,
    val format: String,
    val price: Double,
    val availableSeats: Int,
    val prioritySeats: Int
)

@Serializable
data class RegisterEventRequest(val eventId: String)

@Serializable
data class RegisterEventResponse(
    val registration: RegistrationData,
    val hasPriority: Boolean,
    val message: String
)

@Serializable
data class RegistrationData(val id: String, val hasPriority: Boolean)

