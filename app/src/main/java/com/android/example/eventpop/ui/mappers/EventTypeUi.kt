package com.android.example.eventpop.ui.mappers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.example.eventpop.data.EventType

@Composable
fun EventType.icon(): ImageVector {
    return when (this) {
        EventType.MUSIC -> Icons.Filled.MusicNote
        EventType.FOOD -> Icons.Filled.Fastfood
        EventType.COMEDY -> Icons.Filled.TheaterComedy
        EventType.ART -> Icons.Filled.Palette
        EventType.SOOTHE -> Icons.Filled.SelfImprovement
    }
}
