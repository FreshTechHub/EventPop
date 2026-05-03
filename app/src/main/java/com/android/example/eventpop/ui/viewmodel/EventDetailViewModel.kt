package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.ui.mvc.EventDetailUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * **Controller** for a single event: loads from [EventRepository] (cache + remote refresh).
 */
class EventDetailViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun loadEvent(eventId: String) {
        observeJob?.cancel()
        if (eventId.isBlank()) {
            _uiState.value = EventDetailUiState()
            return
        }
        _uiState.update {
            it.copy(rsvpSuccess = false, event = null)
        }
        observeJob = viewModelScope.launch {
            eventRepository.observeEvent(eventId).collect { ev ->
                _uiState.update { it.copy(event = ev) }
            }
        }
        viewModelScope.launch {
            eventRepository.refreshEvent(eventId)
        }
    }

    fun toggleInterested() {
        _uiState.update { it.copy(isInterested = !it.isInterested) }
    }

    fun submitRsvp() {
        val currentEvent = _uiState.value.event ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(rsvpLoading = true) }
            val success = eventRepository.rsvpToEvent(currentEvent.id)
            _uiState.update { it.copy(rsvpLoading = false, rsvpSuccess = success) }
            if (success) {
                eventRepository.refreshEvent(currentEvent.id)
            }
        }
    }

    fun consumeRsvpSuccess() {
        _uiState.update { it.copy(rsvpSuccess = false) }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}
