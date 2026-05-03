package com.android.example.eventpop.data

import com.android.example.eventpop.data.local.EventDao
import com.android.example.eventpop.data.local.EventEntity
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
}
