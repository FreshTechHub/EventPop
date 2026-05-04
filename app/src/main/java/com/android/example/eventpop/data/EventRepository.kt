package com.android.example.eventpop.data

import android.content.Context
import android.net.Uri
import com.android.example.eventpop.data.local.EventDao
import com.android.example.eventpop.data.local.EventEntity
import com.android.example.eventpop.data.EventCategory
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventRepository(
    private val eventDao: EventDao
) {

    fun observeEvents(): Flow<List<Event>> =
        eventDao.observeAll().map { rows ->
            rows.mapNotNull { row ->
                runCatching { EventJson.decode(row.payloadJson) }.getOrNull()
            }
        }

    fun observeEvent(id: String): Flow<Event?> =
        eventDao.observeById(id).map { entity ->
            entity?.let { runCatching { EventJson.decode(it.payloadJson) }.getOrNull() }
        }

    /**
     * Public events list for pre-auth UI (e.g. landing). Does not require a session.
     */
    suspend fun fetchPublicEventsSnapshot(): List<Event>? =
        SupabaseService.fetchEventsRemote()

    suspend fun refreshEvents(): Boolean {
        val remote = SupabaseService.fetchEventsRemote() ?: return false
        val now = System.currentTimeMillis()
        val entities = remote.map { e ->
            EventEntity(id = e.id, payloadJson = EventJson.encode(e), updatedAtMillis = now)
        }
        eventDao.replaceAll(entities)
        return true
    }

    suspend fun rsvpToEvent(eventId: String): Boolean =
        SupabaseService.rsvpToEvent(eventId)

    suspend fun refreshEvent(id: String): Boolean {
        val remote = SupabaseService.fetchEventByIdRemote(id) ?: return false
        val entity = EventEntity(
            id = remote.id,
            payloadJson = EventJson.encode(remote),
            updatedAtMillis = System.currentTimeMillis()
        )
        eventDao.upsert(entity)
        return true
    }

    suspend fun searchEventsRemote(query: String): List<Event> =
        SupabaseService.searchEventsRemote(query)

    suspend fun loadFavoriteEvents(): List<Event> {
        val ids = SupabaseService.fetchFavoriteEventIdsForCurrentUser()
        if (ids.isEmpty()) return emptyList()
        return SupabaseService.fetchEventsByIds(ids)
    }

    suspend fun isEventInterested(eventId: String): Boolean =
        SupabaseService.isEventInterested(eventId)

    suspend fun setEventInterested(eventId: String, interested: Boolean): Boolean =
        SupabaseService.setEventInterested(eventId, interested)

    /**
     * Categories shown when hosting an event: app [EventCategory] list (synced to Supabase on publish).
     */
    fun createEventCategoryOptions(): List<NamedLookupRow> =
        EventCategory.entries.map { NamedLookupRow(id = it.name, name = it.displayName) }

    suspend fun fetchCreateEventLookups(): Pair<List<NamedLookupRow>, List<NamedLookupRow>> {
        val categories = createEventCategoryOptions()
        return emptyList<NamedLookupRow>() to categories
    }

    suspend fun fetchHostQuota(): HostEventQuota? = SupabaseService.fetchHostQuotaRemote()

    suspend fun createEvent(submission: CreateEventSubmission): Result<Event> {
        val result = SupabaseService.insertEventRemote(submission)
        result.onSuccess { event ->
            val entity = EventEntity(
                id = event.id,
                payloadJson = EventJson.encode(event),
                updatedAtMillis = System.currentTimeMillis()
            )
            eventDao.upsert(entity)
        }
        return result
    }

    suspend fun updateEvent(eventId: String, submission: CreateEventSubmission): Result<Event> {
        val result = SupabaseService.updateEventRemote(eventId, submission)
        result.onSuccess { event ->
            val entity = EventEntity(
                id = event.id,
                payloadJson = EventJson.encode(event),
                updatedAtMillis = System.currentTimeMillis()
            )
            eventDao.upsert(entity)
        }
        return result
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        val result = SupabaseService.deleteEventRemote(eventId)
        if (result.isSuccess) {
            eventDao.deleteById(eventId)
        }
        return result
    }

    suspend fun fetchEventSnapshotRemote(eventId: String): Event? =
        SupabaseService.fetchEventByIdRemote(eventId)

    suspend fun fetchEventImagePathRemote(eventId: String): String? =
        SupabaseService.fetchEventImagePathRemote(eventId)

    suspend fun resolveAreaIdForSubmission(rawName: String): Result<String?> =
        SupabaseService.resolveOrInsertAreaByName(rawName)

    suspend fun resolveCategoryIdForSubmission(displayName: String): Result<String> =
        SupabaseService.resolveOrInsertCategoryByDisplayName(displayName)

    /**
     * Uploads to `event-images` under `{userId}/{uuid}.ext`. Returns the **storage object path**
     * to store in [public.events.image_url] (resolved to a public URL when loading events).
     */
    suspend fun uploadEventCover(context: Context, imageUri: Uri): Result<String> {
        val uid = SupabaseService.currentUserId()
            ?: return Result.failure(IllegalStateException("Not signed in"))
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: return Result.failure(IllegalStateException("Could not read image"))
        if (bytes.size > 5_242_880) {
            return Result.failure(IllegalStateException("Image too large (max 5 MB)"))
        }
        val mime = resolver.getType(imageUri) ?: "image/jpeg"
        val ext = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "jpg"
        }
        val path = "$uid/${UUID.randomUUID()}.$ext"
        return SupabaseService.uploadPublicObject(StorageBuckets.EVENT_IMAGES, path, bytes, mime)
            .map { path }
    }
}
