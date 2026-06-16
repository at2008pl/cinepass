package com.cinepass.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinepass.data.models.Event
import com.cinepass.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventUiState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val selectedEvent: Event? = null,
    val error: String? = null,
    val registrationDone: Boolean = false,
)

class EventViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState(isLoading = true))
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { eventRepository.getEvents() }
                .onSuccess { events ->
                    _uiState.update { it.copy(isLoading = false, events = events, error = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unable to load events") }
                }
        }
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedEvent = null, registrationDone = false) }
            runCatching { eventRepository.getEvent(eventId) }
                .onSuccess { event ->
                    _uiState.update { it.copy(isLoading = false, selectedEvent = event) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unable to load event") }
                }
        }
    }

    fun registerForEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, registrationDone = false) }
            runCatching { eventRepository.registerForEvent(eventId) }
                .onSuccess { ok ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registrationDone = ok,
                            error = if (ok) null else "Registration failed",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Registration failed") }
                }
        }
    }
}

