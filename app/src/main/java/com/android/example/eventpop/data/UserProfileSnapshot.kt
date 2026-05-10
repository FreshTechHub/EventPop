package com.android.example.eventpop.data

data class UserProfileSnapshot(
    val email: String?,
    val displayName: String?,
    val avatarUrl: String = "",
    val isLoggedIn: Boolean,
    val role: UserRole = UserRole.USER
)
