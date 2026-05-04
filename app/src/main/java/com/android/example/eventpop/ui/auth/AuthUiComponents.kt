@file:Suppress("DEPRECATION")

package com.android.example.eventpop.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.eventpop.R

@Composable
fun AuthBrandingHeader(
    title: String,
    subtitle: String,
    showBack: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(AuthUiTokens.HeaderHeight)
            .background(AuthUiTokens.Primary)
    ) {
        if (showBack) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 4.dp, start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = AuthUiTokens.Surface
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                colorFilter = ColorFilter.tint(AuthUiTokens.Surface)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = AuthUiTokens.Surface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = AuthUiTokens.Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AuthOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions =
        androidx.compose.foundation.text.KeyboardActions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = supportingText,
        singleLine = singleLine,
        shape = RoundedCornerShape(AuthUiTokens.RadiusField),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuthUiTokens.Accent,
            unfocusedBorderColor = AuthUiTokens.BorderDefault,
            errorBorderColor = AuthUiTokens.Error,
            cursorColor = AuthUiTokens.Accent,
            focusedTextColor = AuthUiTokens.TextPrimary,
            unfocusedTextColor = AuthUiTokens.TextPrimary,
            errorTextColor = AuthUiTokens.TextPrimary,
            focusedLabelColor = AuthUiTokens.TextSecondary,
            unfocusedLabelColor = AuthUiTokens.TextSecondary,
            errorLabelColor = AuthUiTokens.Error,
            errorSupportingTextColor = AuthUiTokens.Error,
            focusedLeadingIconColor = AuthUiTokens.TextSecondary,
            unfocusedLeadingIconColor = AuthUiTokens.TextSecondary,
            errorLeadingIconColor = AuthUiTokens.TextSecondary,
            focusedTrailingIconColor = AuthUiTokens.TextSecondary,
            unfocusedTrailingIconColor = AuthUiTokens.TextSecondary
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation
    )
}

@Composable
fun AuthPrimaryCta(
    text: String,
    onClick: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(AuthUiTokens.CtaHeight),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(AuthUiTokens.RadiusButton),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthUiTokens.Accent,
            contentColor = AuthUiTokens.Surface,
            disabledContainerColor = AuthUiTokens.Accent.copy(alpha = 0.5f),
            disabledContentColor = AuthUiTokens.Surface.copy(alpha = 0.8f)
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = AuthUiTokens.Surface
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun AuthSocialOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(AuthUiTokens.SocialHeight),
        shape = RoundedCornerShape(AuthUiTokens.RadiusButton),
        border = BorderStroke(1.dp, AuthUiTokens.BorderDefault),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AuthUiTokens.Surface,
            contentColor = AuthUiTokens.TextPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leading()
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AuthOrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = AuthUiTokens.BorderDefault
        )
        Text(
            text = stringResource(R.string.auth_or_continue_with),
            modifier = Modifier.padding(horizontal = 12.dp),
            color = AuthUiTokens.TextSecondary,
            fontSize = 13.sp
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = AuthUiTokens.BorderDefault
        )
    }
}

@Composable
fun PasswordStrengthBar(password: String, modifier: Modifier = Modifier) {
    val strength = passwordStrengthSegments(password)
    val shape = RoundedCornerShape(2.dp)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val colors = AuthUiTokens.PasswordStrengthColors
        val inactive = AuthUiTokens.BorderDefault.copy(alpha = 0.45f)
        for (i in 0 until 4) {
            val filled = strength > i
            val c = if (filled) colors[i] else inactive
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(c, shape)
            )
        }
    }
}

/** 0 = empty, 1–4 = increasing strength (drives four segment colours). */
private fun passwordStrengthSegments(password: String): Int {
    if (password.isEmpty()) return 0
    if (password.length < 4) return 1
    if (password.length < 8) return 2
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    val hasOther = password.any { !it.isLetterOrDigit() }
    val hasLower = password.any { it.isLowerCase() }
    val hasUpper = password.any { it.isUpperCase() }
    if (!hasLetter || !hasDigit) return 2
    if ((hasLower && hasUpper) || hasOther || password.length >= 12) return 4
    return 3
}

@Composable
fun AuthTermsRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    checkboxError: Boolean,
    modifier: Modifier = Modifier
) {
    val termsTag = "terms"
    val privacyTag = "privacy"
    val annotated = buildAnnotatedString {
        append(stringResource(R.string.auth_terms_prefix))
        pushStringAnnotation(termsTag, termsTag)
        withStyle(
            SpanStyle(
                color = AuthUiTokens.Accent,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium
            )
        ) {
            append(stringResource(R.string.auth_terms_service))
        }
        pop()
        append(stringResource(R.string.auth_terms_and))
        pushStringAnnotation(privacyTag, privacyTag)
        withStyle(
            SpanStyle(
                color = AuthUiTokens.Accent,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium
            )
        ) {
            append(stringResource(R.string.auth_terms_privacy))
        }
        pop()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AuthUiTokens.Accent,
                uncheckedColor = if (checkboxError) AuthUiTokens.Error else AuthUiTokens.TextSecondary
            )
        )
        ClickableText(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AuthUiTokens.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(top = 12.dp, end = 8.dp),
            onClick = { offset ->
                annotated.getStringAnnotations(start = offset, end = offset)
                    .firstOrNull()?.let { ann ->
                        when (ann.tag) {
                            termsTag -> onTermsClick()
                            privacyTag -> onPrivacyClick()
                        }
                    }
            }
        )
    }
}

@Composable
fun AuthInlineError(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = AuthUiTokens.Error,
        fontSize = 12.sp,
        modifier = modifier.padding(start = 4.dp, top = 2.dp)
    )
}
