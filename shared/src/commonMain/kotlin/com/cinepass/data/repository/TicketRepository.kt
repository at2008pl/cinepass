package com.cinepass.data.repository

import com.cinepass.data.api.ApiService
import com.cinepass.data.api.models.BookTicketRequest
import com.cinepass.data.models.Ticket
import com.cinepass.data.prefs.UserPrefs

class TicketRepository(
    private val apiService: ApiService,
    private val userPrefs: UserPrefs,
) {
    suspend fun bookTicket(
        showtimeId: String,
        seats: List<String>,
        coinsToUse: Int = 0,
    ): Ticket? {
        val response = apiService.bookTicket(
            token = userPrefs.bearerToken,
            request = BookTicketRequest(
                showtimeId = showtimeId,
                seats = seats,
                coinsToUse = coinsToUse,
            ),
        )
        val ticket = response.body()?.data?.ticket ?: return null
        return ticket.toDomain(userPrefs.userId ?: "")
    }

    suspend fun getMyTickets(): List<Ticket> {
        val response = apiService.getMyTickets(userPrefs.bearerToken)
        val data = response.body()?.data ?: return emptyList()
        return (data.upcoming + data.past).map { item ->
            Ticket(
                id = item.id,
                eventId = item.showtime.event.title,
                userId = userPrefs.userId ?: "",
                eventTitle = item.showtime.event.title,
                eventDate = item.showtime.startTime.toReadableDate(),
                venue = item.showtime.event.venue,
                seatInfo = item.seats.joinToString(", "),
                qrData = item.qrCode,
                isUsed = item.status.equals("USED", true),
                isPriority = true,
            )
        }
    }

    suspend fun getTicketById(ticketId: String): Ticket? {
        val response = apiService.getTicketById(userPrefs.bearerToken, ticketId)
        val ticket = response.body()?.data ?: return null
        return ticket.toDomain(userPrefs.userId ?: "")
    }

    suspend fun cancelTicket(ticketId: String): Boolean {
        val response = apiService.cancelTicket(userPrefs.bearerToken, ticketId)
        return response.isSuccessful && response.body()?.success == true
    }

    private fun com.cinepass.data.api.models.TicketData.toDomain(userId: String): Ticket {
        return Ticket(
            id = id,
            eventId = event,
            userId = userId,
            eventTitle = event,
            eventDate = showtime.toReadableDate(),
            venue = venue,
            seatInfo = seats.joinToString(", "),
            qrData = qrCode,
            isUsed = status.equals("USED", true),
            isPriority = true,
        )
    }

    private fun String.toReadableDate(): String {
        return try {
            val tIndex = this.indexOf('T')
            if (tIndex == -1) return this
            val datePart = this.substring(0, tIndex)
            val parts = datePart.split("-")
            if (parts.size != 3) return this
            val year = parts[0]
            val monthNum = parts[1]
            val day = parts[2].toInt().toString()
            
            val months = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            val monthIdx = monthNum.toIntOrNull()?.minus(1) ?: 0
            val month = if (monthIdx in 0..11) months[monthIdx] else "Jan"
            
            val timePart = this.substring(tIndex + 1)
            val timeParts = timePart.split(":")
            if (timeParts.size >= 2) {
                val hour24 = timeParts[0].toIntOrNull() ?: 0
                val minute = timeParts[1]
                val ampm = if (hour24 >= 12) "PM" else "AM"
                val hour12 = when {
                    hour24 == 0 -> 12
                    hour24 > 12 -> hour24 - 12
                    else -> hour24
                }
                "$day $month $year, $hour12:$minute $ampm"
            } else {
                "$day $month $year"
            }
        } catch (_: Exception) {
            this
        }
    }
}

