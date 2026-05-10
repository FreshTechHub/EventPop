package com.android.example.eventpop.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Model-layer façade for auth operations. UI must not call [SupabaseService] directly.
 */
object AuthRepository {

    private val _roleFlow = MutableStateFlow(SupabaseService.cachedUserRole())
    val roleFlow: StateFlow<UserRole> = _roleFlow.asStateFlow()

    /** Keeps [roleFlow] aligned when [SupabaseService] resolves role outside [refreshRole]. */
    fun publishRole(role: UserRole) {
        _roleFlow.value = role
    }

    fun isLoggedIn(): Boolean = SupabaseService.isUserLoggedIn()

    fun currentUserId(): String? = SupabaseService.currentUserId()

    fun currentProfile(): UserProfileSnapshot = SupabaseService.currentProfileSnapshot()

    /**
     * RBAC predicate: only callers cached as organizer/admin may navigate to event-creation
     * screens. Server-side RLS is the authority — this is a UX-only guard.
     */
    fun isOrganizer(): Boolean = _roleFlow.value.canCreateEvents

    fun cachedRole(): UserRole = _roleFlow.value

    /** Refresh role cache from the server; safe to call after sign-in or on resume. */
    suspend fun refreshRole(): UserRole = withContext(Dispatchers.IO) {
        val r = SupabaseService.fetchCurrentUserRoleRemote()
        _roleFlow.value = r
        r
    }

    suspend fun signOut() {
        SupabaseService.signOut()
        SupabaseService.clearCachedUserRole()
        _roleFlow.value = UserRole.USER
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
