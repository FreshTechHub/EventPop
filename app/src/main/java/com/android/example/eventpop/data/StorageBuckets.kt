package com.android.example.eventpop.data

/**
 * Supabase Storage bucket ids. `event-images` is created by migration
 * `20260506120200_storage_event_images_bucket.sql` (and re-checked when a profile row is inserted).
 * Upload object keys must be `{currentUserId}/...` so storage RLS allows the write.
 */
object StorageBuckets {
    const val EVENT_IMAGES = "event-images"
    const val AVATARS = "avatars"
}
