package com.android.example.eventpop.ui.util

import android.Manifest
import android.os.Build

object MediaPickPermissions {

    /** Permissions required before picking images from gallery (scoped storage aware). */
    fun galleryPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    fun cameraPermission(): Array<String> = arrayOf(Manifest.permission.CAMERA)
}
