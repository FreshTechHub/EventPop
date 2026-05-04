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

sealed class SignUpUiState {
    data class Idle(
        val fullName: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val termsAccepted: Boolean = false,
        val fullNameError: UiText? = null,
        val emailError: UiText? = null,
        val passwordError: UiText? = null,
        val confirmPasswordError: UiText? = null,
        val termsError: UiText? = null,
        val bannerError: UiText? = null
    ) : SignUpUiState()

    data class Loading(val form: Idle) : SignUpUiState()
    data object Success : SignUpUiState()

    data class Error(val message: UiText, val recover: Idle) : SignUpUiState()
}

class SignUpViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.update { s ->
            if (s !is SignUpUiState.Idle) s
            else s.copy(fullName = value, fullNameError = null, bannerError = null)
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { s ->
            if (s !is SignUpUiState.Idle) s
            else s.copy(email = value, emailError = null, bannerError = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { s ->
            if (s !is SignUpUiState.Idle) s
            else s.copy(
                password = value,
                passwordError = null,
                confirmPasswordError = null,
                bannerError = null
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { s ->
            if (s !is SignUpUiState.Idle) s
            else s.copy(confirmPassword = value, confirmPasswordError = null, bannerError = null)
        }
    }

    fun onTermsAcceptedChange(value: Boolean) {
        _uiState.update { s ->
            if (s !is SignUpUiState.Idle) s
            else s.copy(termsAccepted = value, termsError = null, bannerError = null)
        }
    }

    fun dismissError() {
        _uiState.update { s ->
            if (s is SignUpUiState.Error) s.recover else s
        }
    }

    fun submit() {
        val idle = _uiState.value as? SignUpUiState.Idle ?: return
        val fullName = idle.fullName.trim()
        val email = idle.email.trim()
        val password = idle.password
        val confirm = idle.confirmPassword

        var nameErr: UiText? = null
        var emailErr: UiText? = null
        var passErr: UiText? = null
        var confirmErr: UiText? = null
        var termsErr: UiText? = null

        if (fullName.isEmpty()) nameErr = UiText.Resource(R.string.auth_error_name_required)
        if (email.isEmpty()) emailErr = UiText.Resource(R.string.auth_error_email_required)
        else if (!EMAIL_REGEX.matches(email)) emailErr = UiText.Resource(R.string.auth_error_email_invalid)
        if (password.length < MinPasswordLength) {
            passErr = UiText.Resource(R.string.auth_error_password_too_short)
        }
        if (password != confirm) {
            confirmErr = UiText.Resource(R.string.auth_error_password_mismatch)
        }
        if (!idle.termsAccepted) {
            termsErr = UiText.Resource(R.string.auth_error_terms_required)
        }

        if (nameErr != null || emailErr != null || passErr != null || confirmErr != null || termsErr != null) {
            _uiState.value = idle.copy(
                fullNameError = nameErr,
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmErr,
                termsError = termsErr
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading(idle)
            val result = withContext(Dispatchers.IO) {
                AuthRepository.signUp(email, password, fullName)
            }
            _uiState.value = if (result.isSuccess) {
                SignUpUiState.Success
            } else {
                val msg = result.exceptionOrNull()?.message.orEmpty()
                val text = when {
                    msg.contains("no user id", ignoreCase = true) ||
                        msg.contains("confirmation", ignoreCase = true) ->
                        UiText.Resource(R.string.auth_sign_up_check_email)

                    msg.contains("already registered", ignoreCase = true) ||
                        msg.contains("already been registered", ignoreCase = true) ||
                        msg.contains("User already registered", ignoreCase = true) ->
                        UiText.Resource(R.string.auth_error_email_in_use)

                    else -> UiText.Resource(R.string.auth_error_sign_up_failed)
                }
                SignUpUiState.Error(text, idle)
            }
        }
    }

    companion object {
        private val EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$".toRegex()
        const val MinPasswordLength = 8
    }
}
