package com.android.example.eventpop.data

/** Lightweight coordinates from GPS or map pin before reverse geocode completes. */
data class GeoLatLon(val latitude: Double, val longitude: Double)

/**
 * Map-picked location for event creation (stored in UI; submitted as [CreateEventSubmission.location] + lat/lon).
 */
data class EventLocationData(
    val latitude: Double,
    val longitude: Double,
    val displayAddress: String,
    val placeName: String
)

data class NominatimResult(
    val lat: Double,
    val lon: Double,
    val name: String,
    val displayName: String
)
