package com.android.example.eventpop.ui.auth

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class Plain(val value: String) : UiText()

    data class Resource(@param:StringRes val resId: Int, val formatArgs: List<Any> = emptyList()) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is Plain -> value
        is Resource -> stringResource(resId, *formatArgs.toTypedArray())
    }
}
