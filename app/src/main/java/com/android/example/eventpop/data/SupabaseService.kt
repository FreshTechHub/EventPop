package com.android.example.eventpop.data

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.providers.builtin.Email
import com.android.example.eventpop.data.Event
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Service for interacting with Supabase.
 */
object SupabaseService {

    private val client = if (AppConfig.isSupabaseConfigured) {
        createSupabaseClient(
            supabaseUrl = AppConfig.supabaseUrl,
            supabaseKey = AppConfig.supabaseAnonKey
        ) {
            install(Auth) {
                scheme = "eventpop"
                host = "login"
            }
            install(Postgrest)
            install(Storage)
        }
    } else {
        null
    }

    val auth get() = client?.auth
    val postgrest get() = client?.postgrest
    val storage get() = client?.storage

    /**
     * Handle deep links for authentication.
     */
    fun handleDeeplinks(intent: android.content.Intent) {
        client?.handleDeeplinks(intent)
    }

    /**
     * Sign up a new user with email and password.
     */
    suspend fun signUp(email: String, name: String) {
        val auth = auth ?: return
        try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = "TemporaryPassword123!"
                // this.redirectTo = "eventpop://login"
                data = buildJsonObject {
                    put("full_name", name)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Sign up error: ${e.message}", e)
            throw e
        }
    }

    /**
     * Sign in a user with email and password.
     */
    suspend fun signIn(email: String) {
        val auth = auth ?: return
        try {
            auth.signInWith(Email) {
                this.email = email
                this.password = "TemporaryPassword123!"
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Sign in error: ${e.message}", e)
            throw e
        }
    }

    /**
     * Check if a user is currently logged in.
     */
    fun isUserLoggedIn(): Boolean {
        return auth?.currentSessionOrNull() != null
    }

    /**
     * Sign out the current user.
     */
    suspend fun signOut() {
        auth?.signOut()
    }

    /**
     * Fetch events from Supabase.
     */
    suspend fun fetchEvents(): List<Event> = withContext(Dispatchers.IO) {
        val pg = postgrest ?: return@withContext emptyList()
        try {
            val results = pg["events"].select(columns = Columns.ALL).decodeList<Event>()
            return@withContext results
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching events", e)
            return@withContext emptyList()
        }
    }

    suspend fun fetchEventById(eventId: String): Event? = withContext(Dispatchers.IO) {
        val pg = postgrest ?: return@withContext null
        try {
            val result = pg["events"].select(columns = Columns.ALL) {
                filter {
                    eq("id", eventId)
                }
            }.decodeSingle<Event>()
            return@withContext result
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching event by id", e)
            return@withContext null
        }
    }

    /**
     * Search events by title or description.
     */
    suspend fun searchEvents(query: String): List<Event> = withContext(Dispatchers.IO) {
        val pg = postgrest ?: return@withContext emptyList()
        try {
            val results = pg["events"].select {
                filter {
                    or {
                        ilike("title", "%$query%")
                        ilike("description", "%$query%")
                    }
                }
            }.decodeList<Event>()
            return@withContext results
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error searching events", e)
            return@withContext emptyList()
        }
    }

    /**
     * Placeholder for RSVP functionality.
     */
    suspend fun rsvpToEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
        // In a production app, this would be a database insert or update
        // We'll simulate a successful RSVP for now
        delay(500)
        true
    }
}
