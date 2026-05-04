package com.android.example.eventpop.ui.auth

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Design tokens for Login / Register (Kampala EventPop). */
object AuthUiTokens {
    val Primary = Color(0xFF1B2A4A)
    val Accent = Color(0xFFF5821F)
    val Background = Color(0xFFF4F6F9)
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFE53935)
    val TextPrimary = Color(0xFF1B2A4A)
    val TextSecondary = Color(0xFF6B7280)
    val BorderDefault = Color(0xFFE0E0E0)
    val Shadow = Color(0x14000000)

    val RadiusField = 12.dp
    val RadiusButton = 8.dp
    val HeaderHeight = 220.dp
    val CtaHeight = 52.dp
    val SocialHeight = 48.dp
    val ElevationCard = 2.dp

    val PasswordStrengthColors = listOf(
        Color(0xFFE53935),
        Color(0xFFFB8C00),
        Color(0xFFFDD835),
        Color(0xFF43A047)
    )
}
