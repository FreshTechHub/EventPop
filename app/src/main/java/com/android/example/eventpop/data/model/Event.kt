package com.android.example.eventpop.data.model

import com.android.example.eventpop.data.EventType

/**
 * Full event model for the detail screen (id, title, location, area, date, times, price, category, description, organizer, rsvp, rating, interested, image).
 */
data class Event(
    val id: String,
    val title: String,
    val location: String,
    val area: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val price: String,
    val isFree: Boolean,
    val category: EventType,
    val description: String,
    val organizerName: String,
    val rsvpCount: Int,
    val rating: Float,
    val isInterested: Boolean = false,
    val imageUrl: String? = null
)
