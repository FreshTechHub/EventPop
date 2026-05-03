package com.android.example.eventpop.data

/**
 * Model-layer façade for auth operations. UI must not call [SupabaseService] directly.
 */
object AuthRepository {

    fun isLoggedIn(): Boolean = SupabaseService.isUserLoggedIn()

    fun currentProfile(): UserProfileSnapshot = SupabaseService.currentProfileSnapshot()

    suspend fun signOut() {
        SupabaseService.signOut()
    }
}

