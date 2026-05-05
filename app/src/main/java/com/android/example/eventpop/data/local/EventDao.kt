package com.android.example.eventpop.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM cached_events ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM cached_events WHERE id = :id")
    fun observeById(id: String): Flow<EventEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: EventEntity)

    @Query("DELETE FROM cached_events")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM cached_events WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Transaction
    suspend fun replaceAll(entities: List<EventEntity>) {
        deleteAll()
        upsertAll(entities)
    }
}
