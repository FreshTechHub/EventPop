package com.android.example.eventpop.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profilePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "eventpop_profile_prefs"
)

object ProfileKeys {
    val DISPLAY_NAME = stringPreferencesKey("profile_display_name")
    val EMAIL = stringPreferencesKey("profile_email")
    val AVATAR_URL = stringPreferencesKey("profile_avatar_url")
    val AVATAR_LOCAL = stringPreferencesKey("profile_avatar_local_path")
    val LAST_SYNCED = longPreferencesKey("profile_last_synced")
    val PENDING_SYNC = booleanPreferencesKey("profile_pending_sync")
    val ROLE = stringPreferencesKey("profile_role")
}

data class LocalProfile(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val avatarLocalPath: String = "",
    val lastSyncedEpochMillis: Long = 0L,
    val pendingSync: Boolean = false,
    val role: UserRole = UserRole.USER
)

class ProfileLocalDataStore(context: Context) {

    private val dataStore = context.applicationContext.profilePreferencesDataStore

    fun getProfile(): Flow<LocalProfile> = dataStore.data.map { prefs ->
        LocalProfile(
            displayName = prefs[ProfileKeys.DISPLAY_NAME].orEmpty(),
            email = prefs[ProfileKeys.EMAIL].orEmpty(),
            avatarUrl = prefs[ProfileKeys.AVATAR_URL].orEmpty(),
            avatarLocalPath = prefs[ProfileKeys.AVATAR_LOCAL].orEmpty(),
            lastSyncedEpochMillis = prefs[ProfileKeys.LAST_SYNCED] ?: 0L,
            pendingSync = prefs[ProfileKeys.PENDING_SYNC] == true,
            role = UserRole.fromWire(prefs[ProfileKeys.ROLE])
        )
    }

    fun getAvatarLocalPath(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[ProfileKeys.AVATAR_LOCAL]?.takeIf { it.isNotBlank() }
    }

    suspend fun saveProfile(profile: LocalProfile) {
        dataStore.edit { prefs ->
            prefs[ProfileKeys.DISPLAY_NAME] = profile.displayName
            prefs[ProfileKeys.EMAIL] = profile.email
            prefs[ProfileKeys.AVATAR_URL] = profile.avatarUrl
            prefs[ProfileKeys.AVATAR_LOCAL] = profile.avatarLocalPath
            prefs[ProfileKeys.LAST_SYNCED] = profile.lastSyncedEpochMillis
            prefs[ProfileKeys.PENDING_SYNC] = profile.pendingSync
            prefs[ProfileKeys.ROLE] = profile.role.wire
        }
    }

    suspend fun patchProfile(
        displayName: String? = null,
        email: String? = null,
        avatarUrl: String? = null,
        avatarLocalPath: String? = null,
        lastSyncedEpochMillis: Long? = null,
        pendingSync: Boolean? = null,
        role: UserRole? = null
    ) {
        dataStore.edit { prefs ->
            displayName?.let { prefs[ProfileKeys.DISPLAY_NAME] = it }
            email?.let { prefs[ProfileKeys.EMAIL] = it }
            avatarUrl?.let { prefs[ProfileKeys.AVATAR_URL] = it }
            avatarLocalPath?.let { prefs[ProfileKeys.AVATAR_LOCAL] = it }
            lastSyncedEpochMillis?.let { prefs[ProfileKeys.LAST_SYNCED] = it }
            pendingSync?.let { prefs[ProfileKeys.PENDING_SYNC] = it }
            role?.let { prefs[ProfileKeys.ROLE] = it.wire }
        }
    }

    suspend fun saveAvatarLocalPath(path: String) {
        dataStore.edit { it[ProfileKeys.AVATAR_LOCAL] = path }
    }

    suspend fun clearProfile() {
        dataStore.edit { it.clear() }
    }

    suspend fun setPendingSync(value: Boolean) {
        dataStore.edit { it[ProfileKeys.PENDING_SYNC] = value }
    }
}
