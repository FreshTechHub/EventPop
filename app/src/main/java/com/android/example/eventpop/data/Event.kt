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
    val list: List<Event> = listOf(
        Event(
            id = "1",
            title = "Street Food Fest",
            location = "Bugolobi",
            timeInfo = "Today 5:00 PM",
            priceInfo = "Free",
            imageUrl = null,
            category = EventCategory.FOOD
        ),
        Event(
            id = "2",
            title = "Zumba in the Park",
            location = "Kyadondo",
            timeInfo = "In 30 mins",
            priceInfo = "UGX 5K",
            imageUrl = null,
            category = EventCategory.WELLNESS
        ),
        Event(
            id = "3",
            title = "DJ Party",
            location = "Ntinda",
            timeInfo = "Starts at 9:30 PM",
            priceInfo = "Free Entry",
            imageUrl = null,
            category = EventCategory.MUSIC,
            rating = 3.5f,
            rsvpCount = 62
        )
    )
}
