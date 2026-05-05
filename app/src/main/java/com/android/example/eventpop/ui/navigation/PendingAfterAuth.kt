package com.android.example.eventpop.ui.navigation

/**
 * Holds optional post-login navigation (e.g. open an event detail after sign-in).
 * Cleared when consumed on successful auth.
 */
object PendingAfterAuth {

    @Volatile
    private var pendingEventDetailId: String? = null

    fun clear() {
        pendingEventDetailId = null
    }

    fun setPendingEventDetail(eventId: String) {
        pendingEventDetailId = eventId
    }

    fun takePendingEventDetail(): String? {
        val id = pendingEventDetailId
        pendingEventDetailId = null
        return id
    }
}
