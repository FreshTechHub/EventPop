package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.android.example.eventpop.data.EventType
import com.android.example.eventpop.data.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val mockEvents = emptyList<Event>()

class EventDetailViewModel : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isInterested = MutableStateFlow(false)
    val isInterested: StateFlow<Boolean> = _isInterested.asStateFlow()

    private val _rsvpSuccess = MutableStateFlow(false)
    val rsvpSuccess: StateFlow<Boolean> = _rsvpSuccess.asStateFlow()

    private val _rsvpLoading = MutableStateFlow(false)
    val rsvpLoading: StateFlow<Boolean> = _rsvpLoading.asStateFlow()

    fun loadEvent(eventId: String) {
        _event.value = mockEvents.find { it.id == eventId }
        _isInterested.value = _event.value?.isInterested ?: false
        _rsvpSuccess.value = false
    }

    fun toggleInterested() {
        _isInterested.value = !_isInterested.value
    }

    fun submitRsvp() {
        _rsvpLoading.value = true
        _rsvpSuccess.value = true
        _rsvpLoading.value = false
    }
}
