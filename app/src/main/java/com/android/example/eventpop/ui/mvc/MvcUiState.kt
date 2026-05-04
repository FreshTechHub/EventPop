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
    val hotEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false
)

@Immutable
data class DiscoverUiState(
    val searchQuery: String = "",
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    /** Category display name (e.g. "Music") or null when not filtering. */
    val selectedCategory: String? = null,
    /** Selected calendar day (UTC epoch millis) or null when not filtering. */
    val selectedDateMillis: Long? = null
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

@Immutable
data class SearchUiState(
    val query: String = "",
    val results: List<Event> = emptyList(),
    val isLoading: Boolean = false
)

@Immutable
data class FavoritesUiState(
    val favorites: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val needsSignIn: Boolean = false
)

@Immutable
data class ProfileUiState(
    val email: String = "",
    val displayName: String = "Guest",
    val isLoggedIn: Boolean = false,
    val avatarUrl: String = "",
    val avatarLocalPath: String = "",
    val isUploadingAvatar: Boolean = false,
    val isUpdatingName: Boolean = false,
    val isUpdatingEmail: Boolean = false,
    val emailUpdatePending: Boolean = false,
    val avatarUploadProgress: Float = 0f,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val snackbarRetryable: Boolean = false
)
