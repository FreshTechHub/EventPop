package com.android.example.eventpop.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.eventpop.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val termsUrl = stringResource(R.string.auth_terms_url)
    val privacyUrl = stringResource(R.string.auth_privacy_url)

    LaunchedEffect(uiState) {
        if (uiState is SignUpUiState.Success) {
            onAuthenticated()
        }
    }

    val idle: SignUpUiState.Idle? = when (val s = uiState) {
        is SignUpUiState.Idle -> s
        is SignUpUiState.Loading -> s.form
        is SignUpUiState.Error -> s.recover
        else -> null
    }
    val loading = uiState is SignUpUiState.Loading

    Scaffold(
        containerColor = AuthUiTokens.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AuthUiTokens.Background)
        ) {
            AuthBrandingHeader(
                title = stringResource(R.string.auth_sign_up_title),
                subtitle = stringResource(R.string.auth_header_signup_subtitle),
                showBack = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = AuthUiTokens.ElevationCard,
                            shape = RoundedCornerShape(AuthUiTokens.RadiusField),
                            spotColor = AuthUiTokens.Shadow,
                            ambientColor = AuthUiTokens.Shadow
                        )
                        .background(
                            AuthUiTokens.Surface,
                            RoundedCornerShape(AuthUiTokens.RadiusField)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        (uiState as? SignUpUiState.Error)?.let { errState ->
                            AuthInlineError(text = errState.message.asString())
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text(
                                    stringResource(R.string.auth_dismiss_error),
                                    color = AuthUiTokens.Accent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        idle?.bannerError?.let { banner ->
                            AuthInlineError(text = banner.asString())
                        }

                        var passwordVisible by remember { mutableStateOf(false) }
                        var confirmVisible by remember { mutableStateOf(false) }

                        AuthOutlinedField(
                            value = idle?.fullName.orEmpty(),
                            onValueChange = viewModel::onFullNameChange,
                            label = { Text(stringResource(R.string.auth_full_name_label)) },
                            readOnly = loading,
                            isError = idle?.fullNameError != null,
                            supportingText = idle?.fullNameError?.let { err ->
                                { AuthInlineError(text = err.asString()) }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        AuthOutlinedField(
                            value = idle?.email.orEmpty(),
                            onValueChange = viewModel::onEmailChange,
                            label = { Text(stringResource(R.string.auth_email_label)) },
                            readOnly = loading,
                            isError = idle?.emailError != null,
                            supportingText = idle?.emailError?.let { err ->
                                { AuthInlineError(text = err.asString()) }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.MailOutline,
                                    contentDescription = null
                                )
                            },
                            placeholder = { Text(stringResource(R.string.auth_email_hint)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        AuthOutlinedField(
                            value = idle?.password.orEmpty(),
                            onValueChange = viewModel::onPasswordChange,
                            readOnly = loading,
                            label = { Text(stringResource(R.string.auth_password_label)) },
                            isError = idle?.passwordError != null,
                            supportingText = idle?.passwordError?.let { err ->
                                { AuthInlineError(text = err.asString()) }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = stringResource(
                                            if (passwordVisible) {
                                                R.string.auth_password_hide
                                            } else {
                                                R.string.auth_password_show
                                            }
                                        )
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        PasswordStrengthBar(password = idle?.password.orEmpty())
                        Spacer(modifier = Modifier.height(4.dp))

                        AuthOutlinedField(
                            value = idle?.confirmPassword.orEmpty(),
                            onValueChange = viewModel::onConfirmPasswordChange,
                            readOnly = loading,
                            label = { Text(stringResource(R.string.auth_confirm_password_label)) },
                            isError = idle?.confirmPasswordError != null,
                            supportingText = idle?.confirmPasswordError?.let { err ->
                                { AuthInlineError(text = err.asString()) }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                    Icon(
                                        imageVector = if (confirmVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = stringResource(
                                            if (confirmVisible) {
                                                R.string.auth_password_hide
                                            } else {
                                                R.string.auth_password_show
                                            }
                                        )
                                    )
                                }
                            },
                            visualTransformation = if (confirmVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.submit() }
                            )
                        )

                        AuthTermsRow(
                            checked = idle?.termsAccepted == true,
                            onCheckedChange = viewModel::onTermsAcceptedChange,
                            onTermsClick = { uriHandler.openUri(termsUrl) },
                            onPrivacyClick = { uriHandler.openUri(privacyUrl) },
                            checkboxError = idle?.termsError != null
                        )
                        idle?.termsError?.let { err ->
                            AuthInlineError(
                                text = err.asString(),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        AuthPrimaryCta(
                            text = stringResource(R.string.auth_create_account),
                            onClick = { viewModel.submit() },
                            loading = loading,
                            enabled = idle != null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.auth_footer_has_account),
                        color = AuthUiTokens.TextSecondary,
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        enabled = !loading,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_footer_log_in),
                            color = AuthUiTokens.Accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
