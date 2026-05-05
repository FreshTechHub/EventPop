package com.android.example.eventpop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_events")
data class EventEntity(
    @PrimaryKey val id: String,
    /** JSON-encoded [com.android.example.eventpop.data.Event] */
    val payloadJson: String,
    val updatedAtMillis: Long
)
