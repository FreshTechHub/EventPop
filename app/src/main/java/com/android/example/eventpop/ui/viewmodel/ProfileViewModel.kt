package com.android.example.eventpop.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.data.ProfileRepository
import com.android.example.eventpop.data.SupabaseService
import com.android.example.eventpop.ui.mvc.ProfileUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * **Controller** for profile: DataStore-backed identity + Supabase sync + RSVP stats.
 */
class ProfileViewModel(
    application: Application,
    private val profileRepository: ProfileRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _rsvpCount = MutableStateFlow(0)
    val rsvpCount: StateFlow<Int> = _rsvpCount.asStateFlow()

    private var avatarProgressJob: Job? = null
    private var pendingRetryPickUri: Uri? = null

    private val foregroundObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            viewModelScope.launch {
                profileRepository.retryPendingSync()
            }
        }
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)
        viewModelScope.launch {
            profileRepository.observeProfile().collect { local ->
                val loggedIn = AuthRepository.isLoggedIn()
                val snap = AuthRepository.currentProfile()
                _uiState.update { st ->
                    st.copy(
                        isLoggedIn = loggedIn,
                        email = if (loggedIn) firstNonBlank(local.email, snap.email.orEmpty()) else "",
                        displayName = if (loggedIn) {
                            firstNonBlank(local.displayName, snap.displayName.orEmpty(), "Guest")
                        } else {
                            "Guest"
                        },
                        avatarUrl = if (loggedIn) firstNonBlank(local.avatarUrl, snap.avatarUrl) else "",
                        avatarLocalPath = if (loggedIn) local.avatarLocalPath else ""
                    )
                }
            }
        }
        viewModelScope.launch {
            refreshRsvpCountInternal()
        }
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(foregroundObserver)
        super.onCleared()
    }

    fun refresh() {
        viewModelScope.launch {
            profileRepository.syncFromSupabase()
            refreshRsvpCountInternal()
        }
    }

    fun dismissMessages() {
        pendingRetryPickUri = null
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null,
                snackbarRetryable = false
            )
        }
    }

    fun retrySnackbarAction() {
        val uri = pendingRetryPickUri ?: return
        onAvatarSelected(uri)
    }

    fun onAvatarSelected(uri: Uri) {
        pendingRetryPickUri = uri
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploadingAvatar = true,
                    avatarUploadProgress = 0f,
                    successMessage = null,
                    errorMessage = null,
                    snackbarRetryable = false
                )
            }
            avatarProgressJob?.cancel()
            avatarProgressJob = launch {
                while (_uiState.value.avatarUploadProgress < 0.92f && _uiState.value.isUploadingAvatar) {
                    delay(120)
                    _uiState.update { s ->
                        s.copy(avatarUploadProgress = (s.avatarUploadProgress + 0.08f).coerceAtMost(0.92f))
                    }
                }
            }
            profileRepository.updateAvatar(uri).fold(
                onSuccess = { result ->
                    avatarProgressJob?.cancel()
                    val msg = if (result.syncedToCloud && !result.remoteUrl.isNullOrBlank()) {
                        getApplication<Application>().getString(com.android.example.eventpop.R.string.profile_photo_updated)
                    } else {
                        getApplication<Application>().getString(com.android.example.eventpop.R.string.profile_photo_saved_local)
                    }
                    pendingRetryPickUri = null
                    _uiState.update {
                        it.copy(
                            isUploadingAvatar = false,
                            avatarUploadProgress = 1f,
                            successMessage = msg,
                            snackbarRetryable = false
                        )
                    }
                },
                onFailure = { e ->
                    avatarProgressJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isUploadingAvatar = false,
                            avatarUploadProgress = 0f,
                            errorMessage = e.message ?: getApplication<Application>().getString(
                                com.android.example.eventpop.R.string.profile_photo_upload_failed
                            ),
                            snackbarRetryable = true
                        )
                    }
                }
            )
        }
    }

    fun onRemoveAvatar() {
        viewModelScope.launch {
            profileRepository.removeAvatar().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(successMessage = getApplication<Application>().getString(
                            com.android.example.eventpop.R.string.profile_photo_removed
                        ))
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: getApplication<Application>().getString(
                                com.android.example.eventpop.R.string.profile_photo_remove_failed
                            ),
                            snackbarRetryable = false
                        )
                    }
                }
            )
        }
    }

    fun onUpdateDisplayName(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingName = true, errorMessage = null, successMessage = null) }
            profileRepository.updateDisplayName(name).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isUpdatingName = false,
                            successMessage = getApplication<Application>().getString(
                                com.android.example.eventpop.R.string.profile_display_name_updated
                            )
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isUpdatingName = false,
                            errorMessage = e.message ?: getApplication<Application>().getString(
                                com.android.example.eventpop.R.string.profile_display_name_failed
                            ),
                            snackbarRetryable = false
                        )
                    }
                }
            )
        }
    }

    suspend fun refreshRsvpCountInternal() {
        _rsvpCount.value = SupabaseService.countCurrentUserEventInterests()
    }

    suspend fun clearLocalProfileCache() {
        profileRepository.clearLocalCache()
    }

    private fun firstNonBlank(vararg parts: String): String =
        parts.firstOrNull { it.isNotBlank() }.orEmpty()
}
