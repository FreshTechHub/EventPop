package com.android.example.eventpop.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Model-layer façade for auth operations. UI must not call [SupabaseService] directly.
 */
object AuthRepository {

    fun isLoggedIn(): Boolean = SupabaseService.isUserLoggedIn()

    fun currentUserId(): String? = SupabaseService.currentUserId()

    fun currentProfile(): UserProfileSnapshot = SupabaseService.currentProfileSnapshot()

    suspend fun signOut() {
        SupabaseService.signOut()
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        SupabaseService.signInWithEmailPassword(email, password)
    }

    suspend fun signUp(email: String, password: String, fullName: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val identity = SupabaseService.signUpWithEmailPassword(email, password, fullName)
                .getOrElse { return@withContext Result.failure(it) }
            val baseUsername = usernameFromEmail(email)
            repeat(MaxUsernameAttempts) { attempt ->
                val suffix = if (attempt == 0) "" else "_${Random.nextInt(1000, 999_999)}"
                val candidate = (baseUsername + suffix).take(MaxUsernameLength)
                val inserted =
                    SupabaseService.insertProfileRow(identity.userId, fullName.trim(), candidate)
                if (inserted.isSuccess) return@withContext Result.success(Unit)
                val err = inserted.exceptionOrNull() ?: return@withContext inserted
                val msg = err.message.orEmpty()
                if (usernameUniqueViolation(msg)) {
                    if (attempt == MaxUsernameAttempts - 1) return@withContext Result.failure(err)
                } else {
                    return@withContext Result.failure(err)
                }
            }
            Result.failure(IllegalStateException("Could not allocate a unique username."))
        }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        SupabaseService.sendPasswordResetEmail(email)
    }

    private fun usernameFromEmail(email: String): String {
        val local = email.substringBefore("@").lowercase()
        val filtered = local.filter { it.isLetterOrDigit() || it == '_' }.take(24)
        return filtered.ifEmpty { "user" }
    }

    private fun usernameUniqueViolation(message: String): Boolean =
        message.contains("profiles_username", ignoreCase = true)

    private const val MaxUsernameAttempts = 12
    private const val MaxUsernameLength = 30
}
