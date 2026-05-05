package com.android.example.eventpop.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface RatingRepository {
    suspend fun getMyRating(eventId: String): Result<Int?>
    suspend fun upsertRating(eventId: String, score: Int): Result<Unit>
    suspend fun deleteRating(eventId: String): Result<Unit>
}

@Serializable
private data class MyRatingRpcRow(val score: Int)

class RatingRepositoryImpl(
    private val supabase: SupabaseClient?
) : RatingRepository {

    override suspend fun getMyRating(eventId: String): Result<Int?> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (supabase == null) return@runCatching null
                val rows = supabase.postgrest.rpc(
                    function = "get_my_rating",
                    parameters = rpcEventIdParams(eventId)
                ).decodeList<MyRatingRpcRow>()
                rows.firstOrNull()?.score
            }
        }

    override suspend fun upsertRating(eventId: String, score: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(score in 1..5)
                val sb = supabase ?: error("Supabase not configured")
                sb.postgrest.rpc(
                    function = "upsert_rating",
                    parameters = buildJsonObject {
                        put("p_event_id", JsonPrimitive(eventId))
                        put("p_score", JsonPrimitive(score))
                    }
                )
                Unit
            }
        }

    override suspend fun deleteRating(eventId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sb = supabase ?: error("Supabase not configured")
                sb.postgrest.rpc(
                    function = "delete_my_rating",
                    parameters = rpcEventIdParams(eventId)
                )
                Unit
            }
        }

    private fun rpcEventIdParams(eventId: String) = buildJsonObject {
        put("p_event_id", JsonPrimitive(eventId))
    }
}
