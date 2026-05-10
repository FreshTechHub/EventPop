package com.android.example.eventpop.data

/**
 * Mirror of the Postgres `public.user_role` enum. Source of truth for RBAC on the client.
 * Defaults to [USER] for any unknown / missing value to keep the app safe-by-default.
 */
enum class UserRole(val wire: String) {
    USER("user"),
    ORGANIZER("organizer"),
    ADMIN("admin");

    val canCreateEvents: Boolean
        get() = this == ORGANIZER || this == ADMIN

    companion object {
        fun fromWire(value: String?): UserRole {
            if (value.isNullOrBlank()) return USER
            return entries.firstOrNull { it.wire.equals(value, ignoreCase = true) } ?: USER
        }
    }
}
