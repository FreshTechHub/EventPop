package com.android.example.eventpop.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector
import java.io.Serializable

enum class EventType(val label: String, val icon: ImageVector) : Serializable {
    MUSIC("Music", Icons.Filled.MusicNote),
    FOOD("Food", Icons.Filled.Fastfood),
    COMEDY("Comedy", Icons.Filled.TheaterComedy),
    ART("Art", Icons.Filled.Palette),
    SOOTHE("Soothe", Icons.Filled.SelfImprovement)
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
