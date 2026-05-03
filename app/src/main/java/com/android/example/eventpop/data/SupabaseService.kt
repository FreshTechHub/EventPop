package com.android.example.eventpop.data

import android.util.Log
import com.android.example.eventpop.data.remote.EventRemoteRow
import com.android.example.eventpop.data.remote.toEvent
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Service for interacting with Supabase (PostgREST + Storage + Auth).
 */
object SupabaseService {

    private val eventsSelectColumns = Columns.raw(
        "*,area:areas(name),category:categories(name)"
    )

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

    fun handleDeeplinks(intent: android.content.Intent) {
        client?.handleDeeplinks(intent)
    }

    suspend fun signUp(email: String, name: String) {
        val auth = auth ?: return
        try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = "TemporaryPassword123!"
                data = buildJsonObject {
                    put("full_name", name)
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Sign up error: ${e.message}", e)
            throw e
        }
    }

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

    fun isUserLoggedIn(): Boolean {
        return auth?.currentSessionOrNull() != null
    }

    fun currentProfileSnapshot(): UserProfileSnapshot {
        val user = auth?.currentUserOrNull()
            ?: return UserProfileSnapshot(email = null, displayName = null, isLoggedIn = false)
        val meta = user.userMetadata
        val fullName = meta?.get("full_name")?.let { el ->
            (el as? JsonPrimitive)?.content
                ?: el.toString().trim().removeSurrounding("\"")
        }?.takeIf { it.isNotBlank() }
        return UserProfileSnapshot(
            email = user.email,
            displayName = fullName ?: user.email?.substringBefore("@"),
            isLoggedIn = true
        )
    }

    suspend fun signOut() {
        auth?.signOut()
    }

    private suspend fun loadEventRows(
        columns: Columns,
        filter: (io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder.() -> Unit)? = null
    ): List<EventRemoteRow> {
        val pg = postgrest ?: return emptyList()
        return try {
            val request = pg["events"].select(columns = columns) {
                filter?.let { filter(it) }
            }
            request.decodeList<EventRemoteRow>()
        } catch (e: Exception) {
            Log.w("SupabaseService", "events select failed for columns=$columns: ${e.message}")
            if (columns != Columns.ALL) {
                loadEventRows(Columns.ALL, filter)
            } else {
                emptyList()
            }
        }
    }

    /**
     * Loads events from the network. Returns null on transport/parse failure (local cache should be kept).
     */
    suspend fun fetchEventsRemote(): List<Event>? = withContext(Dispatchers.IO) {
        try {
            val rows = loadEventRows(eventsSelectColumns)
            if (postgrest == null) return@withContext null
            rows.map { it.toEvent().withResolvedStorageImage(StorageBuckets.EVENT_IMAGES) }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching events", e)
            null
        }
    }

    suspend fun fetchEventByIdRemote(eventId: String): Event? = withContext(Dispatchers.IO) {
        val pg = postgrest ?: return@withContext null
        try {
            val row = pg["events"].select(columns = eventsSelectColumns) {
                filter { eq("id", eventId) }
            }.decodeList<EventRemoteRow>().singleOrNull()
                ?: pg["events"].select(columns = Columns.ALL) {
                    filter { eq("id", eventId) }
                }.decodeList<EventRemoteRow>().singleOrNull()
                ?: return@withContext null
            row.toEvent().withResolvedStorageImage(StorageBuckets.EVENT_IMAGES)
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching event by id", e)
            null
        }
    }

    suspend fun searchEventsRemote(query: String): List<Event> = withContext(Dispatchers.IO) {
        val pg = postgrest ?: return@withContext emptyList()
        try {
            val rows = pg["events"].select(columns = eventsSelectColumns) {
                filter {
                    or {
                        ilike("title", "%$query%")
                        ilike("description", "%$query%")
                    }
                }
            }.decodeList<EventRemoteRow>()
            rows.map { it.toEvent().withResolvedStorageImage(StorageBuckets.EVENT_IMAGES) }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error searching events", e)
            try {
                val rows = pg["events"].select(columns = Columns.ALL) {
                    filter {
                        or {
                            ilike("title", "%$query%")
                            ilike("description", "%$query%")
                        }
                    }
                }.decodeList<EventRemoteRow>()
                rows.map { it.toEvent().withResolvedStorageImage(StorageBuckets.EVENT_IMAGES) }
            } catch (e2: Exception) {
                Log.e("SupabaseService", "Error searching events (fallback)", e2)
                emptyList()
            }
        }
    }

    /**
     * Public URL for an object in a **public** Supabase Storage bucket.
     */
    fun publicStorageUrl(bucketId: String, objectPath: String): String? {
        val c = client ?: return null
        return try {
            c.storage.from(bucketId).publicUrl(objectPath.trimStart('/'))
        } catch (e: Exception) {
            Log.e("SupabaseService", "publicUrl error: ${e.message}", e)
            null
        }
    }

    /**
     * Upload bytes to Storage and return the public URL (bucket must allow public read).
     */
    suspend fun uploadPublicObject(
        bucketId: String,
        objectPath: String,
        bytes: ByteArray,
        contentType: String = "application/octet-stream"
    ): Result<String> = withContext(Dispatchers.IO) {
        val s = storage ?: return@withContext Result.failure(IllegalStateException("Supabase not configured"))
        runCatching {
            s.from(bucketId).upload(objectPath.trimStart('/'), bytes) {
                upsert = true
                this.contentType = contentType
            }
            s.from(bucketId).publicUrl(objectPath.trimStart('/'))
        }
    }

    private fun Event.withResolvedStorageImage(bucketId: String): Event {
        val url = imageUrl ?: return this
        if (url.startsWith("http", ignoreCase = true)) return this
        val resolved = publicStorageUrl(bucketId, url) ?: return this
        return copy(imageUrl = resolved)
    }

    suspend fun rsvpToEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
        delay(500)
        true
    }

    @Serializable
    private data class EventInterestIdRow(
        @SerialName("event_id") val eventId: String
    )

    @Serializable
    private data class EventInterestInsert(
        @SerialName("event_id") val eventId: String,
        @SerialName("user_id") val userId: String
    )

    suspend fun isEventInterested(eventId: String): Boolean = withContext(Dispatchers.IO) {
        val pg = postgrest ?: return@withContext false
        val uid = auth?.currentUserOrNull()?.id ?: return@withContext false
        return@withContext try {
            pg["event_interests"]
                .select(columns = Columns.raw("event_id")) {
                    filter {
                        eq("event_id", eventId)
                        eq("user_id", uid)
                    }
                }
                .decodeList<EventInterestIdRow>()
                .isNotEmpty()
        } catch (e: Exception) {
            Log.w("SupabaseService", "isEventInterested: ${e.message}")
            false
        }
    }

    suspend fun setEventInterested(eventId: String, interested: Boolean): Boolean = withContext(Dispatchers.IO) {
        val pg = postgrest ?: return@withContext false
        val uid = auth?.currentUserOrNull()?.id ?: return@withContext false
        return@withContext try {
            if (interested) {
                pg["event_interests"].insert(EventInterestInsert(eventId = eventId, userId = uid))
            } else {
                pg["event_interests"].delete {
                    filter {
                        eq("event_id", eventId)
                        eq("user_id", uid)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseService", "setEventInterested: ${e.message}", e)
            false
        }
    }

    /**
     * Event ids the signed-in user marked in [public.event_interests].
     */
    suspend fun fetchFavoriteEventIdsForCurrentUser(): List<String> = withContext(Dispatchers.IO) {
        val pg = postgrest ?: return@withContext emptyList()
        val uid = auth?.currentUserOrNull()?.id ?: return@withContext emptyList()
        try {
            pg["event_interests"]
                .select(columns = Columns.raw("event_id")) {
                    filter { eq("user_id", uid) }
                }
                .decodeList<EventInterestIdRow>()
                .map { it.eventId }
                .distinct()
        } catch (e: Exception) {
            Log.e("SupabaseService", "fetchFavoriteEventIds: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchEventsByIds(eventIds: List<String>): List<Event> = withContext(Dispatchers.IO) {
        if (eventIds.isEmpty()) return@withContext emptyList()
        eventIds.distinct().mapNotNull { id ->
            fetchEventByIdRemote(id)
        }
    }
}
