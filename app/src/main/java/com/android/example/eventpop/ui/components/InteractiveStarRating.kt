package com.android.example.eventpop.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray

@Composable
fun InteractiveStarRating(
    currentRating: Int?,
    @Suppress("UNUSED_PARAMETER") displayRating: Float,
    isSubmitting: Boolean,
    onRatingSelected: (Int) -> Unit,
    onRemoveRating: () -> Unit,
    modifier: Modifier = Modifier,
    tapToRateLabel: String,
    removeLabel: String,
    yourRatedSuffix: String,
) {
    var pulseIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(pulseIndex) {
        if (pulseIndex >= 0) {
            kotlinx.coroutines.delay(280)
            pulseIndex = -1
        }
    }
    val bounceSpec = spring<Float>(
        dampingRatio = 0.4f,
        stiffness = 600f
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val starScore = index + 1
                    val active = index < (currentRating ?: 0)
                    val targetOrange = if (active) OrangeAccent else SubtitleGray.copy(alpha = 0.4f)
                    val staggerMs = index * 30
                    val color by animateColorAsState(
                        targetValue = targetOrange,
                        animationSpec = tween(durationMillis = 150, delayMillis = staggerMs),
                        label = "starTint$index"
                    )
                    val pulseScale by animateFloatAsState(
                        targetValue = if (pulseIndex == index) 1.25f else 1f,
                        animationSpec = bounceSpec,
                        label = "starScale$index"
                    )
                    IconButton(
                        onClick = {
                            if (!isSubmitting) {
                                pulseIndex = index
                                onRatingSelected(starScore)
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (active) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                    transformOrigin = TransformOrigin.Center
                                }
                        )
                    }
                }
            }
            if (isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = OrangeAccent,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (currentRating == null) {
            Text(
                text = tapToRateLabel,
                color = SubtitleGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.StarRate,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = yourRatedSuffix,
                    color = SubtitleGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
                TextButton(
                    onClick = onRemoveRating,
                    enabled = !isSubmitting,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = removeLabel,
                        color = OrangeAccent,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
