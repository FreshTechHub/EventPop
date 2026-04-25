package com.android.example.eventpop.data

import android.util.Log

/**
 * Example service for interacting with Supabase.
 * Uses AppConfig to avoid hard-coded URLs and keys.
 */
class SupabaseService {

    init {
        if (!AppConfig.isSupabaseConfigured) {
            Log.w("SupabaseService", "Supabase is not configured. Please add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties.")
        } else {
            Log.i("SupabaseService", "Supabase initialized with URL: ${AppConfig.supabaseUrl}")
        }
    }

    /**
     * Placeholder for fetching events from Supabase.
     */
    fun fetchEvents() {
        if (!AppConfig.isSupabaseConfigured) return
        
        // In a real implementation, you would use a Supabase client here:
        // val client = createSupabaseClient(AppConfig.supabaseUrl, AppConfig.supabaseAnonKey)
        // ...
    }
}
