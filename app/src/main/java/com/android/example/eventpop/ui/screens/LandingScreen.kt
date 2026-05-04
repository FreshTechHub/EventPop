package com.android.example.eventpop.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.example.eventpop.EventPopApp
import com.android.example.eventpop.MainActivity
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.data.LocalProfile
import com.android.example.eventpop.ui.components.AvatarComposable
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.example.eventpop.R
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.FreeGreen
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import com.android.example.eventpop.ui.viewmodel.LandingViewModel

private val PageBackground = Color(0xFFF4F6F9)
private val LiveRed = Color(0xFFE53935)
private val ChipBorder = Color(0xFFE0E0E0)
private val AuthDivider = Color(0xFFF0F0F0)
private val Shadow6 = Color(0x0F000000)
private val Black5 = Color(0x0D000000)

private data class LandingCategory(
    val label: String,
    val icon: ImageVector
)

private val landingCategories = listOf(
    LandingCategory("Music", Icons.Filled.MusicNote),
    LandingCategory("Food", Icons.Filled.Fastfood),
    LandingCategory("Comedy", Icons.Filled.TheaterComedy),
    LandingCategory("Art", Icons.Filled.Palette),
    LandingCategory("Sports", Icons.Filled.SportsSoccer),
    LandingCategory("Soothe", Icons.Filled.SelfImprovement)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LandingScreen(
    viewModel: LandingViewModel,
    onSignIn: () -> Unit,
    onRegister: () -> Unit,
    onSeeAllLive: () -> Unit,
    onViewAllFeatured: () -> Unit,
    onGetTickets: (eventId: String) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val app = remember(context.applicationContext) { context.applicationContext as EventPopApp }
    val cachedProfile by app.profileLocalDataStore.getProfile()
        .collectAsStateWithLifecycle(LocalProfile())
    val sessionLoggedIn = AuthRepository.isLoggedIn()

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val oldLight = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = oldLight ?: true
        }
    }

    BackHandler {
        (context as? Activity)?.finish()
    }

    val listState = rememberLazyListState()
    val heroImageUrl = remember(uiState) {
        uiState.featuredEvents.firstOrNull { !it.imageUrl.isNullOrBlank() }?.imageUrl
            ?: uiState.allEvents.firstOrNull { !it.imageUrl.isNullOrBlank() }?.imageUrl
    }

    Box(modifier = Modifier.fillMaxSize().background(PageBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item(key = "hero") {
                Column(Modifier.animateItem()) {
                    LandingHero(
                        heroImageUrl = heroImageUrl,
                        isLoading = uiState.isLoading,
                        onSignIn = onSignIn,
                        profile = cachedProfile,
                        isLoggedIn = sessionLoggedIn,
                        onOpenProfile = {
                            context.startActivity(
                                android.content.Intent(context, MainActivity::class.java).apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                            )
                        }
                    )
                }
            }
            item(key = "live") {
                Column(Modifier.animateItem()) {
                    LandingLiveSection(
                        isLoading = uiState.isLoading,
                        liveEvents = uiState.liveEvents,
                        onSeeAll = onSeeAllLive
                    )
                }
            }
            item(key = "categories") {
                Column(Modifier.animateItem()) {
                    LandingCategoryRow()
                }
            }
            item(key = "featured") {
                Column(Modifier.animateItem()) {
                    LandingFeaturedSection(
                        isLoading = uiState.isLoading,
                        featured = uiState.featuredEvents,
                        onViewAll = onViewAllFeatured,
                        onGetTickets = onGetTickets
                    )
                }
            }
            item(key = "social") {
                Column(Modifier.animateItem()) {
                    LandingSocialProof()
                }
            }
            item(key = "bottomPad") {
                Spacer(Modifier.height(120.dp))
            }
        }

        LandingAuthBar(
            onRegister = onRegister,
            onSignIn = onSignIn
        )
    }
}

@Composable
private fun LandingHero(
    heroImageUrl: String?,
    isLoading: Boolean,
    onSignIn: () -> Unit,
    profile: LocalProfile,
    isLoggedIn: Boolean,
    onOpenProfile: () -> Unit
) {
    var heroAnimStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        heroAnimStarted = true
    }
    val heroScale by animateFloatAsState(
        targetValue = if (heroAnimStarted) 1f else 0.97f,
        animationSpec = tween(500),
        label = "heroScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShape(0.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = heroScale
                    scaleY = heroScale
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
        ) {
            when {
                !heroImageUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(heroImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppBarNavy)
                    ) {
                        LandingShimmerOverlay(Modifier.fillMaxSize())
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppBarNavy)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.45f to Color.Transparent,
                            1f to AppBarNavy.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
            if (isLoggedIn) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenProfile),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarComposable(
                        avatarUrl = profile.avatarUrl,
                        avatarLocalPath = profile.avatarLocalPath,
                        displayName = profile.displayName.ifBlank { "User" },
                        size = 28.dp
                    )
                }
            } else {
                TextButton(onClick = onSignIn) {
                    Text(
                        text = stringResource(R.string.landing_sign_in),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.landing_eyebrow),
                color = OrangeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.landing_hero_title),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 38.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.landing_hero_subtitle),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(16.dp))
            HeroStatsRow()
        }
    }
}

@Composable
private fun HeroStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HeroStat("200+", stringResource(R.string.landing_stat_events_listed))
        Box(
            Modifier
                .width(1.dp)
                .height(28.dp)
                .background(Color.White.copy(alpha = 0.2f))
        )
        HeroStat("18", stringResource(R.string.landing_stat_this_weekend))
        Box(
            Modifier
                .width(1.dp)
                .height(28.dp)
                .background(Color.White.copy(alpha = 0.2f))
        )
        HeroStat(stringResource(R.string.landing_stat_free_value), stringResource(R.string.landing_stat_free_label))
    }
}

@Composable
private fun HeroStat(value: String, label: String) {
    Column {
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun LandingLiveSection(
    isLoading: Boolean,
    liveEvents: List<Event>,
    onSeeAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBarNavy)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.landing_live_title),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.landing_live_subtitle),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .border(BorderStroke(1.dp, Color.White), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = stringResource(R.string.landing_see_all),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        val showShimmer = isLoading || liveEvents.isEmpty()
        if (showShimmer) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(4) {
                    LiveCardShimmer()
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(liveEvents, key = { it.id }) { event ->
                    LandingLiveCard(event = event)
                }
            }
        }
    }
}

@Composable
private fun LandingLiveCard(event: Event) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "liveCardPress"
    )
    val ctx = LocalContext.current
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(140.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .clip(RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = {})
    ) {
        if (!event.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(event.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(AppBarNavy))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.5f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(LiveRed)
            )
            Text(
                text = stringResource(R.string.landing_live_badge),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = event.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = event.location,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LandingCategoryRow() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PageBackground)
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.landing_browse_category),
            color = AppBarNavy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(landingCategories, key = { it.label }) { cat ->
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Black5, spotColor = Black5)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBackground)
                        .border(BorderStroke(1.dp, ChipBorder), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = cat.icon,
                        contentDescription = null,
                        tint = AppBarNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = cat.label,
                        color = AppBarNavy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LandingFeaturedSection(
    isLoading: Boolean,
    featured: List<Event>,
    onViewAll: () -> Unit,
    onGetTickets: (eventId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PageBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.landing_featured_title),
                color = AppBarNavy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.landing_view_all),
                color = OrangeAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onViewAll)
            )
        }
        Spacer(Modifier.height(14.dp))
        val showShimmer = isLoading || featured.isEmpty()
        if (showShimmer) {
            for (i in 0 until 4) {
                FeaturedCardShimmer()
                if (i < 3) Spacer(Modifier.height(14.dp))
            }
        } else {
            featured.forEachIndexed { index, event ->
                if (index > 0) Spacer(Modifier.height(14.dp))
                LandingFeaturedCard(event = event, onGetTickets = onGetTickets)
            }
        }
    }
}

@Composable
private fun LandingFeaturedCard(
    event: Event,
    onGetTickets: (eventId: String) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(),
        label = "featuredPress"
    )
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Shadow6, spotColor = Shadow6)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { onGetTickets(event.id) }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            if (!event.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(event.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBarNavy.copy(alpha = 0.15f))
                )
            }
            Text(
                text = event.category.displayName.uppercase(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(OrangeAccent)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        Column(Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                color = AppBarNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = event.location,
                        color = SubtitleGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = event.date ?: event.timeInfo,
                        color = SubtitleGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pillBg = if (event.isFree) FreeGreen else OrangeAccent
                val pillText = if (event.isFree) {
                    stringResource(R.string.landing_free_badge)
                } else {
                    event.priceInfo.uppercase()
                }
                Text(
                    text = pillText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(pillBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.landing_get_tickets),
                    color = OrangeAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun LandingSocialProof() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBarNavy)
            .padding(horizontal = 28.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.landing_social_headline),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.landing_social_body),
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SocialStat(stringResource(R.string.landing_social_users_value), stringResource(R.string.landing_social_users_label))
            SocialStat(stringResource(R.string.landing_social_hosted_value), stringResource(R.string.landing_social_hosted_label))
            SocialStat(stringResource(R.string.landing_social_rating_value), stringResource(R.string.landing_social_rating_label))
        }
    }
}

@Composable
private fun SocialStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = OrangeAccent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LandingAuthBar(
    onRegister: () -> Unit,
    onSignIn: () -> Unit
) {
    val context = LocalContext.current
    val termsUrl = stringResource(R.string.auth_terms_url)
    val privacyUrl = stringResource(R.string.auth_privacy_url)
    val primaryInteraction = remember { MutableInteractionSource() }
    val primaryPressed by primaryInteraction.collectIsPressedAsState()
    val primaryScale by animateFloatAsState(
        targetValue = if (primaryPressed) 0.97f else 1f,
        animationSpec = spring(),
        label = "primaryAuth"
    )
    val secondaryInteraction = remember { MutableInteractionSource() }
    val secondaryPressed by secondaryInteraction.collectIsPressedAsState()
    val secondaryScale by animateFloatAsState(
        targetValue = if (secondaryPressed) 0.97f else 1f,
        animationSpec = spring(),
        label = "secondaryAuth"
    )

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.08f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
            ) {
                HorizontalDivider(thickness = 1.dp, color = AuthDivider)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = primaryScale
                                scaleY = primaryScale
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                            }
                            .height(52.dp)
                            .shadow(
                                4.dp,
                                RoundedCornerShape(14.dp),
                                spotColor = OrangeAccent.copy(alpha = 0.35f),
                                ambientColor = OrangeAccent.copy(alpha = 0.35f)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(OrangeAccent)
                            .clickable(
                                interactionSource = primaryInteraction,
                                indication = null,
                                onClick = onRegister
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.landing_get_started),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = secondaryScale
                                scaleY = secondaryScale
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                            }
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(BorderStroke(1.5.dp, AppBarNavy), RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = secondaryInteraction,
                                indication = null,
                                onClick = onSignIn
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.landing_have_account),
                            color = AppBarNavy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    LandingLegalFooter(
                        onTerms = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl)))
                        },
                        onPrivacy = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LandingLegalFooter(
    onTerms: () -> Unit,
    onPrivacy: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.landing_legal_prefix),
            color = SubtitleGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.auth_terms_service),
                color = OrangeAccent,
                fontSize = 11.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onTerms)
            )
            Text(
                text = stringResource(R.string.auth_terms_and),
                color = SubtitleGray,
                fontSize = 11.sp
            )
            Text(
                text = stringResource(R.string.auth_terms_privacy),
                color = OrangeAccent,
                fontSize = 11.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onPrivacy)
            )
        }
    }
}

@Composable
private fun shimmerBrush(): Brush {
    val t = rememberInfiniteTransition(label = "landingShimmer")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "landingShimmerPhase"
    )
    val c1 = Color(0xFFE0E0E0)
    val c2 = Color(0xFFF5F5F5)
    val color = lerp(c1, c2, phase)
    return Brush.linearGradient(colors = listOf(color, color))
}

@Composable
private fun LandingShimmerOverlay(modifier: Modifier) {
    Box(
        modifier = modifier.background(shimmerBrush())
    )
}

@Composable
private fun LiveCardShimmer() {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(shimmerBrush())
    )
}

@Composable
private fun FeaturedCardShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Shadow6, spotColor = Shadow6)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(shimmerBrush())
        )
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(0.65f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush())
            )
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.45f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush())
            )
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush())
            )
        }
    }
}
