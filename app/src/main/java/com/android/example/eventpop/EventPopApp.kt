package com.android.example.eventpop

import android.app.Application
import androidx.room.Room
import com.android.example.eventpop.data.EventRepository
import com.android.example.eventpop.data.ProfileLocalDataStore
import com.android.example.eventpop.data.ProfileRepository
import com.android.example.eventpop.data.SupabaseService
import com.android.example.eventpop.data.repository.RatingRepository
import com.android.example.eventpop.data.repository.RatingRepositoryImpl
import com.android.example.eventpop.data.local.AppDatabase

/**
 * Application entry: wires **Model** dependencies (Room + [EventRepository]) used by **Controllers** (ViewModels).
 */
class EventPopApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ProfileRepository.cleanupTempCaptureIfExists(applicationContext)
    }

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

    val ratingRepository: RatingRepository by lazy {
        RatingRepositoryImpl(SupabaseService.supabaseClientOrNull())
    }

    val profileLocalDataStore: ProfileLocalDataStore by lazy {
        ProfileLocalDataStore(applicationContext)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(
            supabase = SupabaseService.supabaseClientOrNull(),
            localDataStore = profileLocalDataStore,
            context = applicationContext
        )
    }
}
