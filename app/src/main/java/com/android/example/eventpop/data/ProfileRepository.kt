package com.android.example.eventpop.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

data class AvatarUploadResult(
    val remoteUrl: String?,
    val localPath: String,
    val syncedToCloud: Boolean
)

class ProfileRepository(
    private val supabase: SupabaseClient?,
    private val localDataStore: ProfileLocalDataStore,
    private val context: Context
) {

    fun observeProfile(): Flow<LocalProfile> = localDataStore.getProfile()

    companion object {
        fun tempCaptureFile(ctx: Context): File = File(ctx.filesDir, "temp_capture.jpg")

        fun cleanupTempCaptureIfExists(ctx: Context) {
            runCatching { tempCaptureFile(ctx).takeIf { it.exists() }?.delete() }
        }
    }

    private fun userId(): String? = SupabaseService.currentUserId()

    private fun avatarObjectPath(uid: String): String = "public/$uid/avatar.jpg"

    private fun localAvatarFile(uid: String): File {
        val dir = File(context.filesDir, "avatars").apply { mkdirs() }
        return File(dir, "$uid.jpg")
    }

    private fun Throwable.isConnectivityLikely(): Boolean =
        this is java.net.UnknownHostException ||
            this is java.net.SocketTimeoutException ||
            this is java.io.IOException ||
            message.orEmpty().contains("Unable to resolve", ignoreCase = true) ||
            message.orEmpty().contains("failed to connect", ignoreCase = true)

    suspend fun syncFromSupabase(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = userId()
        if (uid == null) {
            localDataStore.clearProfile()
            return@withContext Result.success(Unit)
        }
        runCatching {
            val role = SupabaseService.fetchCurrentUserRoleRemote()
            AuthRepository.publishRole(role)
            val snap = SupabaseService.currentProfileSnapshot()
            val current = localDataStore.getProfile().first()
            val merged = current.copy(
                displayName = snap.displayName.orEmpty().ifBlank { current.displayName },
                email = snap.email.orEmpty().ifBlank { current.email },
                avatarUrl = snap.avatarUrl.ifBlank { current.avatarUrl },
                avatarLocalPath = current.avatarLocalPath,
                lastSyncedEpochMillis = System.currentTimeMillis(),
                pendingSync = current.pendingSync,
                role = role
            )
            localDataStore.saveProfile(merged)
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun retryPendingSync(): Result<Unit> = withContext(Dispatchers.IO) {
        val profile = localDataStore.getProfile().first()
        if (!profile.pendingSync) return@withContext Result.success(Unit)
        val uid = userId() ?: return@withContext Result.failure(IllegalStateException("Not signed in"))
        runCatching {
            remoteUpdateUserMetadata(
                profile.displayName.takeIf { it.isNotBlank() },
                profile.avatarUrl.takeIf { it.isNotBlank() }
            ).getOrThrow()
            var url = profile.avatarUrl
            if (url.isBlank() && profile.avatarLocalPath.isNotBlank()) {
                val f = File(profile.avatarLocalPath)
                if (f.exists()) {
                    val bytes = compressImage(Uri.fromFile(f))
                    url = uploadAvatarBytes(uid, bytes).getOrThrow()
                    localDataStore.patchProfile(avatarUrl = url)
                    remoteUpdateUserMetadata(null, url).getOrThrow()
                }
            }
            localDataStore.setPendingSync(false)
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { e ->
                if (e.isConnectivityLikely()) Result.success(Unit)
                else Result.failure(e)
            }
        )
    }

    suspend fun updateDisplayName(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return@withContext Result.failure(IllegalArgumentException("Empty name"))
        userId() ?: return@withContext Result.failure(IllegalStateException("Not signed in"))
        val before = localDataStore.getProfile().first()
        localDataStore.patchProfile(displayName = trimmed)
        remoteUpdateUserMetadata(displayName = trimmed, avatarUrl = null).fold(
            onSuccess = {
                localDataStore.setPendingSync(false)
                Result.success(Unit)
            },
            onFailure = { e ->
                if (e.isConnectivityLikely()) {
                    localDataStore.setPendingSync(true)
                    Result.success(Unit)
                } else {
                    localDataStore.patchProfile(displayName = before.displayName)
                    Result.failure(e)
                }
            }
        )
    }

    suspend fun updateEmail(newEmail: String): Result<Unit> = withContext(Dispatchers.IO) {
        val email = newEmail.trim()
        if (email.isBlank()) return@withContext Result.failure(IllegalArgumentException("Empty email"))
        val client = supabase ?: return@withContext Result.failure(IllegalStateException("Supabase not configured"))
        runCatching {
            client.auth.updateUser { this.email = email }
            localDataStore.patchProfile(email = email)
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun updateAvatar(uri: Uri): Result<AvatarUploadResult> = withContext(Dispatchers.IO) {
        val uid = userId() ?: return@withContext Result.failure(IllegalStateException("Not signed in"))
        runCatching {
            val bytes = compressImage(uri)
            val previous = localDataStore.getProfile().first()
            previous.avatarLocalPath.takeIf { it.isNotBlank() }?.let { p ->
                if (p != localAvatarFile(uid).absolutePath) runCatching { File(p).delete() }
            }
            val outFile = localAvatarFile(uid)
            outFile.outputStream().use { it.write(bytes) }
            val path = outFile.absolutePath
            localDataStore.patchProfile(avatarLocalPath = path, pendingSync = true)
            uploadAvatarBytes(uid, bytes).fold(
                onSuccess = { url ->
                    localDataStore.patchProfile(avatarUrl = url, pendingSync = false)
                    remoteUpdateUserMetadata(displayName = null, avatarUrl = url)
                        .onFailure { err ->
                            if (err.isConnectivityLikely()) localDataStore.setPendingSync(true)
                        }
                    AvatarUploadResult(remoteUrl = url, localPath = path, syncedToCloud = true)
                },
                onFailure = { err ->
                    if (err.isConnectivityLikely()) {
                        localDataStore.setPendingSync(true)
                    }
                    AvatarUploadResult(remoteUrl = null, localPath = path, syncedToCloud = false)
                }
            )
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    private suspend fun uploadAvatarBytes(userId: String, bytes: ByteArray): Result<String> {
        val path = avatarObjectPath(userId)
        return SupabaseService.uploadPublicObject(
            bucketId = StorageBuckets.AVATARS,
            objectPath = path,
            bytes = bytes,
            contentType = "image/jpeg"
        )
    }

    suspend fun removeAvatar(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = userId() ?: return@withContext Result.failure(IllegalStateException("Not signed in"))
        runCatching {
            SupabaseService.deletePublicStorageObject(StorageBuckets.AVATARS, avatarObjectPath(uid))
            val profile = localDataStore.getProfile().first()
            profile.avatarLocalPath.takeIf { it.isNotBlank() }?.let { runCatching { File(it).delete() } }
            localDataStore.patchProfile(avatarUrl = "", avatarLocalPath = "")
            remoteUpdateUserMetadata(displayName = null, avatarUrl = "")
            tempCaptureFile(context).takeIf { it.exists() }?.delete()
            Unit
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun clearLocalCache() {
        localDataStore.clearProfile()
    }

    private suspend fun remoteUpdateUserMetadata(
        displayName: String?,
        avatarUrl: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val client = supabase ?: return@withContext Result.failure(IllegalStateException("Supabase not configured"))
        if (displayName == null && avatarUrl == null) return@withContext Result.success(Unit)
        runCatching {
            client.auth.updateUser {
                data = buildJsonObject {
                    displayName?.let {
                        put("display_name", JsonPrimitive(it))
                        put("full_name", JsonPrimitive(it))
                    }
                    avatarUrl?.let { put("avatar_url", JsonPrimitive(it)) }
                }
            }
            Unit
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }

    fun compressImage(uri: Uri): ByteArray {
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open image")
        stream.use { input ->
            val bmp = BitmapFactory.decodeStream(input)
                ?: throw IllegalArgumentException("Unsupported image")
            val scaled = scaleToMaxSide(bmp, 800)
            if (scaled != bmp) bmp.recycle()
            ByteArrayOutputStream().use { baos ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                scaled.recycle()
                return baos.toByteArray()
            }
        }
    }

    private fun scaleToMaxSide(src: Bitmap, maxSide: Int): Bitmap {
        val w = src.width
        val h = src.height
        val longest = max(w, h)
        if (longest <= maxSide) return src
        val scale = maxSide.toFloat() / longest.toFloat()
        val nw = (w * scale).roundToInt().coerceAtLeast(1)
        val nh = (h * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, nw, nh, true)
    }
}
