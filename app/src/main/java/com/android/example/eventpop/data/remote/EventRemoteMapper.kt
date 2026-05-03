package com.android.example.eventpop.data.remote

import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventCategory

private fun mapCategoryName(name: String?): EventCategory {
    if (name.isNullOrBlank()) return EventCategory.VENUE
    val n = name.trim()
    return EventCategory.entries.find { e ->
        e.name.equals(n, ignoreCase = true) ||
            e.displayName.equals(n, ignoreCase = true)
    } ?: EventCategory.VENUE
}

private fun buildTimeInfo(date: String?, start: String?, end: String?): String {
    val timeRange = when {
        start.isNullOrBlank() -> null
        end.isNullOrBlank() -> start
        else -> "$start–$end"
    }
    val parts = listOfNotNull(date?.takeIf { it.isNotBlank() }, timeRange)
    return if (parts.isEmpty()) "TBD" else parts.joinToString(" · ")
}

private fun buildPriceInfo(isFree: Boolean, price: Double?): String {
    if (isFree) return "Free"
    return price?.let { p -> "UGX ${p.toLong()}" } ?: "Paid"
}

fun EventRemoteRow.toEvent(): Event {
    val categoryEnum = mapCategoryName(category?.name)
    return Event(
        id = id,
        title = title,
        location = location,
        area = area?.name,
        timeInfo = buildTimeInfo(date, startTime, endTime),
        date = date,
        startTime = startTime,
        endTime = endTime,
        priceInfo = buildPriceInfo(isFree, price),
        isFree = isFree,
        imageUrl = imageUrl?.takeIf { it.isNotBlank() },
        category = categoryEnum,
        rating = avgRating?.toFloat(),
        rsvpCount = rsvpCount,
        description = description,
        organizerName = null,
        isInterested = false,
        latitude = latitude,
        longitude = longitude
    )
}
