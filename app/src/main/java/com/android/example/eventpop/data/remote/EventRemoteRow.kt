package com.android.example.eventpop.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedName(
    val name: String
)

/**
 * PostgREST row for [public.events] with optional [areas] / [categories] embeds.
 */
@Serializable
data class EventRemoteRow(
    val id: String,
    val title: String,
    val location: String,
    @SerialName("is_free") val isFree: Boolean = false,
    val description: String = "",
    @SerialName("rsvp_count") val rsvpCount: Int = 0,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("avg_rating") val avgRating: Double? = null,
    val price: Double? = null,
    val date: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val area: EmbeddedName? = null,
    val category: EmbeddedName? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
