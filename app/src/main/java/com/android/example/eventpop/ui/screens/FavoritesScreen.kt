@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.android.example.eventpop.ui.screens

import android.app.Activity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ripple
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.R
import com.android.example.eventpop.ui.components.EventPopCenteredTopBar
import com.android.example.eventpop.ui.mvc.FavoritesUiState
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OnAppBar
import com.android.example.eventpop.ui.theme.FreeGreen
import com.android.example.eventpop.ui.theme.HeartRed
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray

private val BodyBackground = Color(0xFFF4F6F9)
private val Shadow6 = Color(0x0F000000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onEventClick: (Event) -> Unit,
    onNavEvents: () -> Unit,
    onNavMap: () -> Unit,
    onNavDiscover: () -> Unit,
    onNavFavorites: () -> Unit,
    onNavProfile: () -> Unit,
    onSignIn: () -> Unit,
    onRemoveFavorite: (Event) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val oldLight = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = oldLight ?: true
        }
    }

    val favoritesTopBarState = rememberTopAppBarState()
    val favoritesScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(favoritesTopBarState)

    Scaffold(
        containerColor = BodyBackground,
        contentColor = AppBarNavy,
        topBar = {
            EventPopCenteredTopBar(
                title = stringResource(R.string.nav_favorites),
                scrollBehavior = favoritesScrollBehavior,
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = OnAppBar
                        )
                    }
                }
            )
        },
        bottomBar = {
            EventPopBottomBar(
                selectedEvents = false,
                selectedMap = false,
                selectedDiscover = false,
                selectedFavorites = true,
                selectedProfile = false,
                onNavEvents = onNavEvents,
                onNavMap = onNavMap,
                onNavDiscover = onNavDiscover,
                onNavFavorites = onNavFavorites,
                onNavProfile = onNavProfile
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                FavoritesLoadingShimmer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BodyBackground)
                        .padding(innerPadding)
                )
            }
            uiState.needsSignIn -> {
                FavoritesNeedsSignIn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BodyBackground)
                        .padding(innerPadding),
                    onSignIn = onSignIn
                )
            }
            uiState.favorites.isEmpty() -> {
                FavoritesEmpty(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BodyBackground)
                        .padding(innerPadding),
                    onExploreEvents = onNavEvents
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BodyBackground)
                        .padding(innerPadding)
                        .nestedScroll(favoritesScrollBehavior.nestedScrollConnection)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                ) {
                    item(key = "header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.favorites.size} Saved Events",
                                color = SubtitleGray,
                                fontSize = 13.sp
                            )
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = SubtitleGray
                                )
                            }
                        }
                    }
                    items(
                        items = uiState.favorites,
                        key = { it.id }
                    ) { event ->
                        SwipeFavoriteRow(
                            event = event,
                            onEventClick = { onEventClick(event) },
                            onRemoveFavorite = { onRemoveFavorite(event) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesLoadingShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "favShimmer")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "favShimmerT"
    )
    val c1 = Color(0xFFE0E0E0)
    val c2 = Color(0xFFF5F5F5)
    val brushColor = lerp(c1, c2, t)
    Column(
        modifier = modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Shadow6, spotColor = Shadow6)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .padding(16.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(brushColor)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(brushColor)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(brushColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesNeedsSignIn(modifier: Modifier = Modifier, onSignIn: () -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AppBarNavy.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = AppBarNavy.copy(alpha = 0.4f)
            )
        }
        Text(
            text = "Sign in to view favorites",
            color = AppBarNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = "Events you mark as interested sync here from your account.",
            color = SubtitleGray,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp)
        )
        Button(
            onClick = onSignIn,
            modifier = Modifier
                .padding(top = 24.dp)
                .width(160.dp)
                .height(48.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = OrangeAccent.copy(alpha = 0.3f), spotColor = OrangeAccent.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("Sign In", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FavoritesEmpty(modifier: Modifier = Modifier, onExploreEvents: () -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(OrangeAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = OrangeAccent
            )
        }
        Text(
            text = "No favorites yet",
            color = AppBarNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = "Tap the heart on any event to save it here",
            color = SubtitleGray,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 40.dp, end = 40.dp, top = 8.dp)
        )
        OutlinedButton(
            onClick = onExploreEvents,
            modifier = Modifier
                .padding(top = 24.dp)
                .width(160.dp)
                .height(44.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, OrangeAccent),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent)
        ) {
            Text("Explore Events", fontSize = 14.sp)
        }
    }
}

@Composable
private fun SwipeFavoriteRow(
    event: Event,
    onEventClick: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { target ->
            if (target == SwipeToDismissBoxValue.EndToStart) {
                onRemoveFavorite()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.fillMaxWidth(),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HeartRed),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    modifier = Modifier.padding(end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Remove",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        content = {
            FavoriteEventRowCard(
                event = event,
                onClick = onEventClick,
                onUnfavorite = onRemoveFavorite
            )
        }
    )
}

@Composable
private fun FavoriteEventRowCard(
    event: Event,
    onClick: () -> Unit,
    onUnfavorite: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "favCardScale"
    )
    val heartInteraction = remember { MutableInteractionSource() }
    val heartPressed by heartInteraction.collectIsPressedAsState()
    val heartScale by animateFloatAsState(
        targetValue = if (heartPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "heartScale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin.Center
            }
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Shadow6, spotColor = Shadow6)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppBarNavy.copy(alpha = 0.15f))
            ) {
                if (event.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(event.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = OrangeAccent.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = event.title,
                    color = AppBarNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = OrangeAccent
                    )
                    Text(
                        text = event.area ?: event.location,
                        color = SubtitleGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = SubtitleGray
                    )
                    Text(
                        text = event.date ?: event.timeInfo,
                        color = SubtitleGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (event.isFree) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(FreeGreen)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FREE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(OrangeAccent)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = event.priceInfo,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = heartScale
                            scaleY = heartScale
                            transformOrigin = TransformOrigin.Center
                        }
                        .clickable(
                            interactionSource = heartInteraction,
                            indication = null,
                            onClick = onUnfavorite
                        ),
                    tint = HeartRed
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(16.dp),
                    tint = SubtitleGray.copy(alpha = 0.5f)
                )
            }
        }
    }
}
