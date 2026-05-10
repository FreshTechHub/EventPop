package com.android.example.eventpop.data

/**
 * Lookup row for area / category pickers when creating an event.
 */
data class NamedLookupRow(
    val id: String,
    val name: String
)

/**
 * Hosting quota from [public.profiles] + count of [public.events] for [auth.uid].
 *
 * [role] gates **whether** the caller is allowed to create events at all
 * (RBAC), while [subscriptionActive] / [hostedEventCount] control **how many**
 * events an organizer may create. Both must be satisfied for [canCreateEvent].
 */
data class HostEventQuota(
    val subscriptionActive: Boolean,
    val hostedEventCount: Int,
    val role: UserRole = UserRole.USER
) {
    val canCreateEvent: Boolean
        get() = role.canCreateEvents &&
            (subscriptionActive || hostedEventCount < 2)
}

/**
 * Payload assembled in the UI and sent to PostgREST (excluding [created_by], set by DB trigger).
 */
data class CreateEventSubmission(
    val title: String,
    val location: String,
    val description: String,
    val isFree: Boolean,
    val price: Double?,
    val date: String?,
    val startTime: String?,
    val endTime: String?,
    val areaId: String?,
    val categoryId: String?,
    val latitude: Double?,
    val longitude: Double?,
    /** Storage object path or https URL; null if no image. */
    val imagePathOrUrl: String?,
    /** Preserved when updating an event (insert ignores). */
    val rsvpCount: Int? = null
)
