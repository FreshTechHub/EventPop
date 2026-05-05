package com.android.example.eventpop.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shape of a row from [public.event_ratings] for direct queries if needed later.
 */
@Serializable
data class RatingRemoteRow(
    @SerialName("event_id") val eventId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("score") val score: Int,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
