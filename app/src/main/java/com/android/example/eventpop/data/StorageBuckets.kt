package com.android.example.eventpop.data

/**
 * Supabase Storage bucket ids. Create matching buckets in the Supabase dashboard
 * (Storage → New bucket). For public event images, mark the bucket as public or use signed URLs.
 */
object StorageBuckets {
    const val EVENT_IMAGES = "event-images"
    const val AVATARS = "avatars"
}
