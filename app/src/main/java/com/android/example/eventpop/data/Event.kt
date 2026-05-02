package com.android.example.eventpop.data

import kotlinx.serialization.Serializable

/**
 * Category of an event, used for filtering and map marker color.
 */
@Serializable
enum class EventCategory(val displayName: String, val markerColorHex: Long) {
    MUSIC("Music", 0xFF0D9488L),
    VENUE("Venue", 0xFFEA580CL),
    COMEDY("Comedy", 0xFF7C3AEDL),
    WELLNESS("Wellness", 0xFF059669L),
    ART("Art", 0xFFDC2626L),
    FOOD("Food", 0xFFEA580CL)
}

/**
 * Represents a single event in the feed.
 */
@Serializable
data class Event(
    val id: String,
    val title: String,
    val location: String,
    val area: String? = null,
    val timeInfo: String,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val priceInfo: String,
    val isFree: Boolean = false,
    val imageUrl: String? = null,
    val category: EventCategory,
    val rating: Float? = null,
    val rsvpCount: Int? = null,
    val description: String? = null,
    val organizerName: String? = null,
    val isInterested: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val subtitle: String get() = "$location · $timeInfo · $priceInfo"
}

object SampleEvents {
    val list: List<Event> = emptyList()
}
