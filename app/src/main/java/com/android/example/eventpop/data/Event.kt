package com.android.example.eventpop.data

/**
 * Category of an event, used for filtering and map marker color.
 */
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
data class Event(
    val id: String,
    val title: String,
    val location: String,
    val timeInfo: String,
    val priceInfo: String,
    val imageUrl: String?,
    val category: EventCategory,
    val rating: Float? = null,
    val rsvpCount: Int? = null
) {
    val subtitle: String get() = "$location · $timeInfo · $priceInfo"
}

object SampleEvents {
    val list: List<Event> = emptyList()
}
