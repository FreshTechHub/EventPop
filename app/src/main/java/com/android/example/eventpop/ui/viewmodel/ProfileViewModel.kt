package com.android.example.eventpop.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.ui.mvc.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * **Controller** for profile header: reads session via [AuthRepository].
 */
class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun refresh() {
        val p = AuthRepository.currentProfile()
        _uiState.value = ProfileUiState(
            email = p.email.orEmpty(),
            displayName = (p.displayName?.takeIf { it.isNotBlank() }) ?: "Guest",
            isLoggedIn = p.isLoggedIn
        )
    }
}
