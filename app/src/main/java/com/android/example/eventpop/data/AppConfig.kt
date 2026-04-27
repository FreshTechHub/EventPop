package com.android.example.eventpop.data

import com.android.example.eventpop.BuildConfig

/**
 * Central configuration for the app.
 * Values are injected from build.gradle via BuildConfig.
 * Users should add these to local.properties.
 */
object AppConfig {
    val supabaseUrl: String = BuildConfig.SUPABASE_URL
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
    val mapsApiKey: String = BuildConfig.MAPS_API_KEY

    val isSupabaseConfigured: Boolean = supabaseUrl.isNotEmpty() && supabaseAnonKey.isNotEmpty()
}
