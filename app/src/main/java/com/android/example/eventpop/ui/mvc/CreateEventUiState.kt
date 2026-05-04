package com.android.example.eventpop.ui.mvc

import com.android.example.eventpop.data.EventLocationData
import com.android.example.eventpop.data.NamedLookupRow

data class CreateEventFieldErrors(
    val title: String? = null,
    val location: String? = null,
    val description: String? = null,
    val area: String? = null,
    val category: String? = null,
    val date: String? = null,
    val time: String? = null,
    val price: String? = null
)

/**
 * UI state for the host "Create event" flow (catalog pickers, free-tier gate, form, publish).
 */
data class CreateEventUiState(
    val isLoadingMeta: Boolean = true,
    val metaError: String? = null,
    /** When true, user has 2 events and no subscription — show upgrade panel instead of the form. */
    val subscribeGate: Boolean = false,
    val hostedCount: Int = 0,
    val subscriptionActive: Boolean = false,
    val areas: List<NamedLookupRow> = emptyList(),
    val categories: List<NamedLookupRow> = emptyList(),
    val selectedAreaId: String? = null,
    val selectedCategoryId: String? = null,
    val title: String = "",
    val description: String = "",
    val isFree: Boolean = true,
    val priceText: String = "",
    val dateText: String = "",
    val startTimeText: String = "",
    val endTimeText: String = "",
    val coverImageLabel: String? = null,
    val coverStoragePath: String? = null,
    val isUploadingCover: Boolean = false,
    val isPublishing: Boolean = false,
    val publishError: String? = null,
    val fieldErrors: CreateEventFieldErrors = CreateEventFieldErrors(),
    /** When non-null, navigate to event detail; clear after handling navigation. */
    val navigateToEventId: String? = null,
    /** Confirmed map-picked location for submission. */
    val locationData: EventLocationData? = null,
    /** Reverse geocode in progress (location picker sheet). */
    val locationLoading: Boolean = false,
    val locationError: String? = null
)
