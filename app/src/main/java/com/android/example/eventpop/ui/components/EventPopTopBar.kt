package com.android.example.eventpop.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.AppBarNavyScrolled
import com.android.example.eventpop.ui.theme.OnAppBar
import com.android.example.eventpop.ui.theme.OnAppBarMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun eventPopTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = AppBarNavy,
    scrolledContainerColor = AppBarNavyScrolled,
    titleContentColor = OnAppBar,
    navigationIconContentColor = OnAppBar,
    actionIconContentColor = OnAppBar
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPopHomeLargeTopBar(
    greeting: String,
    displayName: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onProfileClick: () -> Unit,
    profileContentDescription: String,
    onSearchClick: () -> Unit,
    searchContentDescription: String,
    avatarUrl: String?,
    avatarLocalPath: String?,
    avatarDisplayName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LargeTopAppBar(
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.labelLarge,
                        color = OnAppBarMuted,
                        fontWeight = FontWeight.Medium
                    )
                    AnimatedContent(
                        targetState = displayName,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(160))
                        },
                        label = "homeDisplayName"
                    ) { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnAppBar,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.semantics { contentDescription = profileContentDescription }
                ) {
                    AvatarComposable(
                        avatarUrl = avatarUrl.orEmpty(),
                        avatarLocalPath = avatarLocalPath.orEmpty(),
                        displayName = avatarDisplayName,
                        size = 32.dp
                    )
                }
            },
            actions = {
                FilledTonalIconButton(
                    onClick = onSearchClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = OnAppBar.copy(alpha = 0.18f),
                        contentColor = OnAppBar
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = searchContentDescription
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            colors = eventPopTopAppBarColors()
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = OnAppBar.copy(alpha = 0.12f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPopCenteredTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    backContentDescription: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showDividerBelow: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backContentDescription
                        )
                    }
                }
            },
            actions = actions,
            scrollBehavior = scrollBehavior,
            colors = eventPopTopAppBarColors()
        )
        if (showDividerBelow) {
            HorizontalDivider(
                thickness = 1.dp,
                color = OnAppBar.copy(alpha = 0.12f)
            )
        }
    }
}
