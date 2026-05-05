package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.ui.mvc.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * **Controller** for the home feed: reads/writes through [EventRepository] (Model), exposes [HomeUiState] (View).
 */
class HomeViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)

    private fun computeHotEvents(events: List<Event>): List<Event> =
        events.asSequence()
            .sortedWith(
                compareByDescending<Event> { it.rating ?: 0f }
                    .thenByDescending { it.rsvpCount ?: 0 }
            )
            .take(6)
            .toList()

    val uiState: StateFlow<HomeUiState> = combine(
        eventRepository.observeEvents(),
        isRefreshing
    ) { events, loading ->
        HomeUiState(
            events = events,
            hotEvents = computeHotEvents(events),
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                eventRepository.refreshEvents()
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun rsvpEvent(eventId: String) {
        viewModelScope.launch {
            if (eventRepository.rsvpToEvent(eventId)) {
                refresh()
            }
        }
    }
}
