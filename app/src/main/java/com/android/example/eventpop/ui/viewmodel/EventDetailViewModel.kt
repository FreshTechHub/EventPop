package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.android.example.eventpop.data.EventType
import com.android.example.eventpop.data.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val mockEvents = listOf(
    Event(
        id = "1",
        title = "Street Food Fest",
        location = "Bugolobi Market",
        area = "Bugolobi",
        date = "Today",
        startTime = "5:00 PM",
        endTime = "9:00 PM",
        price = "Free",
        isFree = true,
        category = EventType.FOOD,
        description = "Join us for an evening of the best street food Kampala has to offer. From grilled meats to local delicacies, live music and a vibrant atmosphere. Bring your friends and appetite!",
        organizerName = "Kampala Street Eats",
        rsvpCount = 124,
        rating = 4.6f,
        isInterested = false,
        imageUrl = null
    ),
    Event(
        id = "2",
        title = "Live Band Night",
        location = "Kololo Lounge",
        area = "Kololo",
        date = "Friday, June 14",
        startTime = "8:00 PM",
        endTime = "11:00 PM",
        price = "UGX 10K",
        isFree = false,
        category = EventType.MUSIC,
        description = "An unforgettable night of live music featuring local bands. Great sound, cold drinks, and good vibes. Doors open at 7:30 PM.",
        organizerName = "Kololo Lounge",
        rsvpCount = 89,
        rating = 4.4f,
        isInterested = false,
        imageUrl = null
    ),
    Event(
        id = "3",
        title = "DJ Party",
        location = "Ntinda Hub",
        area = "Ntinda",
        date = "Saturday, June 15",
        startTime = "9:30 PM",
        endTime = "2:00 AM",
        price = "Free Entry",
        isFree = true,
        category = EventType.MUSIC,
        description = "The biggest DJ night in Ntinda. Top local DJs, premium sound system, and a crowd that knows how to party. Free entry before 10:30 PM.",
        organizerName = "Ntinda Events",
        rsvpCount = 62,
        rating = 3.5f,
        isInterested = false,
        imageUrl = null
    )
)

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
