package com.android.example.eventpop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.example.eventpop.ui.theme.AppBarNavy
import java.io.File

@Composable
fun AvatarComposable(
    avatarUrl: String,
    avatarLocalPath: String,
    displayName: String,
    size: Dp,
    modifier: Modifier = Modifier,
    initialsFontSize: TextUnit? = null
) {
    val ctx = LocalContext.current
    val initials = remember(displayName) {
        displayName.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "?" }
    }
    val resolvedFont = initialsFontSize ?: when {
        size >= 96.dp -> 30.sp
        size >= 44.dp -> 14.sp
        else -> 12.sp
    }
    val localFile = remember(avatarLocalPath) {
        avatarLocalPath.takeIf { it.isNotBlank() }?.let { File(it) }?.takeIf { it.exists() }
    }
    val model: Any? = when {
        localFile != null -> localFile
        avatarUrl.isNotBlank() -> avatarUrl
        else -> null
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(AppBarNavy),
        contentAlignment = Alignment.Center
    ) {
        if (model != null) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(model)
                    .crossfade(300)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontSize = resolvedFont,
                fontWeight = FontWeight.Black
            )
        }
    }
}
