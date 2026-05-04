package com.android.example.eventpop.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.example.eventpop.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToSignUp: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val resetTick by viewModel.resetSuccessTick.collectAsState()
    val resetSentMessage = stringResource(R.string.auth_reset_email_sent)

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onAuthenticated()
        }
    }

    LaunchedEffect(resetTick) {
        if (resetTick > 0) {
            snackbarHostState.showSnackbar(resetSentMessage)
        }
    }

    val idle: LoginUiState.Idle? = when (val s = uiState) {
        is LoginUiState.Idle -> s
        is LoginUiState.Loading -> s.form
        is LoginUiState.Error -> s.recover
        else -> null
    }
    val loading = uiState is LoginUiState.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.auth_login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    (uiState as? LoginUiState.Error)?.let { errState ->
                        Text(
                            text = errState.message.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text(
                                stringResource(R.string.auth_dismiss_error),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    idle?.bannerError?.let { banner ->
                        Text(
                            text = banner.asString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    var passwordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = idle?.email.orEmpty(),
                        onValueChange = viewModel::onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = loading,
                        label = { Text(stringResource(R.string.auth_email_label)) },
                        placeholder = { Text(stringResource(R.string.auth_email_hint)) },
                        singleLine = true,
                        isError = idle?.emailError != null,
                        supportingText = idle?.emailError?.let { err ->
                            { Text(err.asString(), color = MaterialTheme.colorScheme.error) }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = idle?.password.orEmpty(),
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = loading,
                        label = { Text(stringResource(R.string.auth_password_label)) },
                        singleLine = true,
                        isError = idle?.passwordError != null,
                        supportingText = idle?.passwordError?.let { err ->
                            { Text(err.asString(), color = MaterialTheme.colorScheme.error) }
                        },
                        visualTransformation = if (passwordVisible) {
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
                        ),
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
                        }
                    )

                    TextButton(
                        onClick = { viewModel.sendPasswordReset() },
                        enabled = !loading
                    ) {
                        Text(stringResource(R.string.auth_forgot_password))
                    }

                    Button(
                        onClick = { viewModel.submit() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = idle != null && !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.auth_sign_in))
                        }
                    }

                    TextButton(
                        onClick = onNavigateToSignUp,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        enabled = !loading
                    ) {
                        Text(stringResource(R.string.auth_go_to_sign_up))
                    }
                }
            }
        }
    }
}
