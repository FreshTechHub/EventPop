package com.android.example.eventpop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray

@Composable
fun ReadOnlyStarRating(
    rating: Float,
    ratingCount: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp,
    showRatingCountLabel: Boolean = true,
) {
    val outlineTint = SubtitleGray.copy(alpha = 0.4f)
    val noRatings = ratingCount <= 0

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(5) { index ->
                val remainder = rating - index
                val fill = remainder.coerceIn(0f, 1f)
                StarHalfCell(
                    fillFraction = fill,
                    starSize = starSize,
                    outlineTint = outlineTint,
                    accent = OrangeAccent
                )
            }
        }

        Spacer(modifier = Modifier.padding(start = 4.dp))

        if (noRatings) {
            Text(
                text = "No ratings yet",
                color = SubtitleGray,
                fontSize = 12.sp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "%.1f".format(rating),
                    color = AppBarNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (showRatingCountLabel) {
                    Text(
                        text = "($ratingCount)",
                        color = SubtitleGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StarHalfCell(
    fillFraction: Float,
    starSize: Dp,
    outlineTint: Color,
    accent: Color
) {
    Box(
        modifier = Modifier.size(starSize),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = null,
            tint = outlineTint,
            modifier = Modifier.fillMaxSize()
        )
        if (fillFraction > 0.001f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(fillFraction.coerceIn(0.05f, 1f))
                    .clip(RoundedCornerShape(0.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}
