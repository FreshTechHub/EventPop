package com.android.example.eventpop

import android.app.Application
import androidx.room.Room
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.data.local.AppDatabase

/**
 * Application entry: wires **Model** dependencies (Room + [EventRepository]) used by **Controllers** (ViewModels).
 */
class EventPopApp : Application() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "eventpop.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    val eventRepository: EventRepository by lazy {
        EventRepository(database.eventDao())
    }
}
