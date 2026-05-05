package com.android.example.eventpop.data

import java.io.Serializable

enum class EventType(val label: String) : Serializable {
    MUSIC("Music"),
    FOOD("Food"),
    COMEDY("Comedy"),
    ART("Art"),
    SOOTHE("Soothe")
}

enum class EventLocation(val label: String) : Serializable {
    ALL_AREAS("All Areas"),
    NTINDA("Ntinda"),
    KOLOLO("Kololo"),
    BUGOLOBI("Bugolobi"),
    WANDEGEYA("Wandegeya")
}

enum class TimeRange(val label: String) : Serializable {
    ANYTIME("Anytime"),
    TODAY("Today"),
    THIS_WEEKEND("This Weekend")
}

data class EventFilter(
    val selectedTypes: Set<EventType> = emptySet(),
    val selectedLocation: EventLocation = EventLocation.ALL_AREAS,
    val selectedTime: TimeRange = TimeRange.ANYTIME
) : Serializable
