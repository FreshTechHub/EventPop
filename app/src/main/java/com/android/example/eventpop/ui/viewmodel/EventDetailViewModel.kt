package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            _event.value = SupabaseService.fetchEventById(eventId)
            _rsvpSuccess.value = false
        }
    }

    fun toggleInterested() {
        _isInterested.value = !_isInterested.value
    }

    fun submitRsvp() {
        val currentEvent = _event.value ?: return
        viewModelScope.launch {
            _rsvpLoading.value = true
            val success = SupabaseService.rsvpToEvent(currentEvent.id)
            _rsvpSuccess.value = success
            _rsvpLoading.value = false
        }
    }
}
