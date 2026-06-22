package com.cinepass.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinepass.data.models.Ticket
import com.cinepass.data.repository.EventRepository
import com.cinepass.data.repository.TicketRepository
import com.cinepass.data.repository.WalletRepository
import com.cinepass.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShowtimeUi(
    val id: String,
    val label: String,
    val price: Int,
)

data class TicketUiState(
    val isLoading: Boolean = true,
    val isBooking: Boolean = false,
    val eventTitle: String = Constants.MOVIE_TITLE,
    val eventMeta: String = "Priority booking available",
    val showtimes: List<ShowtimeUi> = emptyList(),
    val selectedShowtimeId: String = "",
    val useCoins: Boolean = true,
    val availableCoins: Int = 0,
    val error: String? = null,
    val bookedTicketId: String? = null,
)

class TicketViewModel(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    init {
        loadTicketContext()
    }

    fun loadTicketContext() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val event = eventRepository.getEventData(Constants.MOVIE_EVENT_ID)
                val wallet = walletRepository.getMyWallet()
                val shows = event?.showtimes.orEmpty().map { s ->
                    ShowtimeUi(
                        id = s.id,
                        label = "${s.startTime.take(16).replace('T', ' ')} ${s.format}",
                        price = s.price.toInt(),
                    )
                }
                Triple(event, wallet.totalCoins, shows)
            }.onSuccess { (event, coins, shows) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        eventTitle = event?.title ?: Constants.MOVIE_TITLE,
                        eventMeta = "${event?.city ?: ""} • ${event?.venue ?: ""}".trim().trim('•', ' '),
                        availableCoins = coins,
                        showtimes = shows,
                        selectedShowtimeId = shows.firstOrNull()?.id.orEmpty(),
                        error = null,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load booking details",
                    )
                }
            }
        }
    }

    fun selectShowtime(showtimeId: String) {
        _uiState.update { it.copy(selectedShowtimeId = showtimeId) }
    }

    fun setUseCoins(useCoins: Boolean) {
        _uiState.update { it.copy(useCoins = useCoins) }
    }

    fun clearBookedTicket() {
        _uiState.update { it.copy(bookedTicketId = null) }
    }

    fun bookNow() {
        val current = _uiState.value
        if (current.selectedShowtimeId.isBlank()) {
            _uiState.update { it.copy(error = "Select a showtime first") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBooking = true, error = null) }
            val coinsToUse = if (current.useCoins) minOf(200, current.availableCoins) else 0
            runCatching {
                ticketRepository.bookTicket(
                    showtimeId = current.selectedShowtimeId,
                    seats = listOf("B-12", "B-13"),
                    coinsToUse = coinsToUse,
                )
            }.onSuccess { ticket ->
                _uiState.update {
                    it.copy(
                        isBooking = false,
                        bookedTicketId = ticket?.id,
                        error = if (ticket == null) "Booking failed" else null,
                    )
                }
                if (ticket != null) {
                    loadTicketContext()
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isBooking = false,
                        error = e.message ?: "Booking failed",
                    )
                }
            }
        }
    }

    fun getTicket(ticketId: String, onLoaded: (Ticket?) -> Unit) {
        viewModelScope.launch {
            val ticket = runCatching { ticketRepository.getTicketById(ticketId) }.getOrNull()
            onLoaded(ticket)
        }
    }

    fun getTicket(ticketId: String): Flow<Ticket?> = flow {
        emit(runCatching { ticketRepository.getTicketById(ticketId) }.getOrNull())
    }
}
