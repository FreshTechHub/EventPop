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
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onNavigateToLogin: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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

    Scaffold { padding ->
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
                text = stringResource(R.string.auth_sign_up_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.auth_sign_up_subtitle),
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
                    (uiState as? SignUpUiState.Error)?.let { errState ->
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
                    var confirmVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = idle?.fullName.orEmpty(),
                        onValueChange = viewModel::onFullNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = loading,
                        label = { Text(stringResource(R.string.auth_full_name_label)) },
                        singleLine = true,
                        isError = idle?.fullNameError != null,
                        supportingText = idle?.fullNameError?.let { err ->
                            { Text(err.asString(), color = MaterialTheme.colorScheme.error) }
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )

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
                        isError = idle?.passwordError != null ||
                            ((idle?.password?.length ?: 0) in 1 until SignUpViewModel.MinPasswordLength),
                        supportingText = idle?.passwordError?.let { err ->
                            {
                                Text(err.asString(), color = MaterialTheme.colorScheme.error)
                            }
                        } ?: if ((idle?.password?.length ?: 0) in 1 until SignUpViewModel.MinPasswordLength) {
                            {
                                Text(
                                    stringResource(R.string.auth_password_min_hint),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            null
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
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

                    OutlinedTextField(
                        value = idle?.confirmPassword.orEmpty(),
                        onValueChange = viewModel::onConfirmPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = loading,
                        label = { Text(stringResource(R.string.auth_confirm_password_label)) },
                        singleLine = true,
                        isError = idle?.confirmPasswordError != null ||
                            (idle != null &&
                                idle.confirmPassword.isNotEmpty() &&
                                idle.password != idle.confirmPassword),
                        supportingText = idle?.confirmPasswordError?.let { err ->
                            {
                                Text(err.asString(), color = MaterialTheme.colorScheme.error)
                            }
                        } ?: if (idle != null &&
                            idle.confirmPassword.isNotEmpty() &&
                            idle.password != idle.confirmPassword
                        ) {
                            {
                                Text(
                                    stringResource(R.string.auth_password_mismatch_hint),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            null
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
                        ),
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
                        }
                    )

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
                            Text(stringResource(R.string.auth_create_account))
                        }
                    }

                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        enabled = !loading
                    ) {
                        Text(stringResource(R.string.auth_go_to_login))
                    }
                }
            }
        }
    }
}
