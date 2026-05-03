package com.android.example.eventpop.data

/**
 * Model-layer façade for auth operations. UI must not call [SupabaseService] directly.
 */
object AuthRepository {
    suspend fun signOut() {
        SupabaseService.signOut()
    }
}
