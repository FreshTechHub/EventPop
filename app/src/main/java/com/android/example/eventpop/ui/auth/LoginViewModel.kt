package com.android.example.eventpop.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.eventpop.R
import com.android.example.eventpop.data.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class LoginUiState {
    data class Idle(
        val email: String = "",
        val password: String = "",
        val emailError: UiText? = null,
        val passwordError: UiText? = null,
        val bannerError: UiText? = null
    ) : LoginUiState()

    data class Loading(val form: Idle) : LoginUiState()
    data object Success : LoginUiState()

    data class Error(val message: UiText, val recover: Idle) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _resetSuccessTick = MutableStateFlow(0)
    val resetSuccessTick: StateFlow<Int> = _resetSuccessTick.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { s ->
            if (s !is LoginUiState.Idle) s
            else s.copy(email = value, emailError = null, bannerError = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { s ->
            if (s !is LoginUiState.Idle) s
            else s.copy(password = value, passwordError = null, bannerError = null)
        }
    }

    fun dismissError() {
        _uiState.update { s ->
            if (s is LoginUiState.Error) s.recover else s
        }
    }

    fun submit() {
        val idle = _uiState.value as? LoginUiState.Idle ?: return
        val email = idle.email.trim()
        val password = idle.password
        var next = idle
        if (email.isEmpty()) {
            next = next.copy(emailError = UiText.Resource(R.string.auth_error_email_required))
        }
        if (password.isEmpty()) {
            next = next.copy(passwordError = UiText.Resource(R.string.auth_error_password_required))
        }
        if (next.emailError != null || next.passwordError != null) {
            _uiState.value = next
            return
        }
        if (!emailLooksValid(email)) {
            _uiState.value = idle.copy(emailError = UiText.Resource(R.string.auth_error_email_invalid))
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading(idle)
            val result = withContext(Dispatchers.IO) {
                AuthRepository.signIn(email, password)
            }
            _uiState.value = if (result.isSuccess) {
                LoginUiState.Success
            } else {
                val msg = result.exceptionOrNull()?.message.orEmpty()
                val text = if (msg.contains("confirmation", ignoreCase = true)) {
                    UiText.Resource(R.string.auth_error_confirm_email)
                } else {
                    UiText.Resource(R.string.auth_error_sign_in_failed)
                }
                LoginUiState.Error(text, idle)
            }
        }
    }

    fun sendPasswordReset() {
        val idle = _uiState.value as? LoginUiState.Idle ?: return
        val email = idle.email.trim()
        if (email.isEmpty()) {
            _uiState.value = idle.copy(emailError = UiText.Resource(R.string.auth_error_email_required))
            return
        }
        if (!emailLooksValid(email)) {
            _uiState.value = idle.copy(emailError = UiText.Resource(R.string.auth_error_email_invalid))
            return
        }
        viewModelScope.launch {
            _uiState.value = idle.copy(bannerError = null)
            val result = withContext(Dispatchers.IO) {
                AuthRepository.sendPasswordReset(email)
            }
            if (result.isSuccess) {
                _resetSuccessTick.update { it + 1 }
            } else {
                _uiState.value = idle.copy(
                    bannerError = UiText.Resource(R.string.auth_error_reset_email_failed)
                )
            }
        }
    }

    private fun emailLooksValid(email: String): Boolean =
        EMAIL_REGEX.matches(email)

    companion object {
        private val EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$".toRegex()
    }
}
