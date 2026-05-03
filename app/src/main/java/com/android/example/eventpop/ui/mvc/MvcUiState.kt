package com.android.example.eventpop.ui.mvc

import androidx.compose.runtime.Immutable
import com.android.example.eventpop.data.Event

/**
 * Immutable presentation models for **View** layers (Compose).
 * **Controller** (ViewModels) produce these; screens render state + forward user intent only.
 */
@Immutable
data class HomeUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false
)

@Immutable
data class DiscoverUiState(
    val searchQuery: String = "",
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false
)

@Immutable
data class EventDetailUiState(
    val event: Event? = null,
    val isInterested: Boolean = false,
    val rsvpSuccess: Boolean = false,
    val rsvpLoading: Boolean = false
)

@Immutable
data class MapUiState(
    val eventPins: List<Event> = emptyList(),
    val isLoading: Boolean = false
)
