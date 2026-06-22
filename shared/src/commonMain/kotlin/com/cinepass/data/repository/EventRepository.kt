package com.cinepass.data.repository

import com.cinepass.data.api.ApiService
import com.cinepass.data.api.models.EventData
import com.cinepass.data.models.Event
import com.cinepass.data.models.EventStatus
import com.cinepass.data.models.EventType
import com.cinepass.data.prefs.UserPrefs

class EventRepository(
    private val apiService: ApiService,
    private val userPrefs: UserPrefs,
) {
    suspend fun getEvents(city: String? = null): List<Event> {
        val response = apiService.listEvents(city = city)
        return response.body()?.data.orEmpty().map { it.toDomainEvent() }
    }

    suspend fun getEvent(eventId: String): Event? {
        return getEventData(eventId)?.toDomainEvent()
    }

    suspend fun getEventData(eventId: String): EventData? {
        val response = apiService.getEvent(eventId)
        return response.body()?.data
    }

    suspend fun registerForEvent(eventId: String): Boolean {
        val response = apiService.registerForEvent(eventId, userPrefs.bearerToken)
        return response.isSuccessful && response.body()?.success == true
    }

    private fun EventData.toDomainEvent(): Event {
        val banner = showtimes?.firstOrNull()?.let { "" } ?: ""
        return Event(
            id = id,
            title = title,
            description = description ?: "",
            type = EventType.MOVIE,
            bannerUrl = banner,
            teaserUrl = null,
            date = showtimes?.firstOrNull()?.startTime ?: "",
            venue = venue,
            city = city,
            organizerId = organizer.name,
            isInviteOnly = false,
            ticketPrice = showtimes?.firstOrNull()?.price?.toInt() ?: 0,
            discountForRegistered = 5,
            coinsPerReferral = coinsPerReferral,
            status = EventStatus.UPCOMING,
        )
    }
}
