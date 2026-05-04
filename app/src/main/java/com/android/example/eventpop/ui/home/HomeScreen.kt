package com.android.example.eventpop.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.android.example.eventpop.EventPopApp
import com.android.example.eventpop.R
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.data.LocalProfile
import com.android.example.eventpop.ui.components.EventPopHomeLargeTopBar
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventCategory
import com.android.example.eventpop.data.EventFilter
import com.android.example.eventpop.data.EventLocation
import com.android.example.eventpop.data.EventType
import com.android.example.eventpop.data.TimeRange
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import com.android.example.eventpop.ui.mvc.HomeUiState
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.lerp
import coil.compose.AsyncImagePainter

private val BodyBackground = Color(0xFFF4F6F9)
private val ChipBorderGray = Color(0xFFE0E0E0)
private val ShimmerLow = Color(0xFFE0E0E0)
private val ShimmerHigh = Color(0xFFF5F5F5)
private val ShadowBlack8 = Color(0x14000000)

private val HotCardWidth = 180.dp
private val HotCardHeight = 220.dp
private val ThumbnailSize = 88.dp
private val CardCorner = 16.dp
private val SectionSpacing = 20.dp

private fun EventFilter.applyTo(events: List<Event>): List<Event> {
    return events.filter { event ->
        val typeMatch = selectedTypes.isEmpty() || selectedTypes.any { type ->
            when (type) {
                EventType.MUSIC -> event.category == EventCategory.MUSIC
                EventType.FOOD -> event.category == EventCategory.FOOD
                EventType.COMEDY -> event.category == EventCategory.COMEDY
                EventType.ART -> event.category == EventCategory.ART
                EventType.SOOTHE -> event.category == EventCategory.WELLNESS
            }
        }
        val locationMatch = selectedLocation == EventLocation.ALL_AREAS ||
            event.location.equals(selectedLocation.label, ignoreCase = true)
        val timeMatch = when (selectedTime) {
            TimeRange.ANYTIME -> true
            TimeRange.TODAY -> event.timeInfo.contains("Today", ignoreCase = true)
            TimeRange.THIS_WEEKEND -> event.timeInfo.contains("Weekend", ignoreCase = true)
        }
        typeMatch && locationMatch && timeMatch
    }
}

private fun Event.matchesQuickType(type: EventType): Boolean = when (type) {
    EventType.MUSIC -> category == EventCategory.MUSIC
    EventType.FOOD -> category == EventCategory.FOOD
    EventType.COMEDY -> category == EventCategory.COMEDY
    EventType.ART -> category == EventCategory.ART
    EventType.SOOTHE -> category == EventCategory.WELLNESS
}

private fun timeOfDayGreetingRes(): Int {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..16 -> R.string.home_greeting_afternoon
        else -> R.string.home_greeting_evening
    }
}

private fun EventType.leadingIcon() = when (this) {
    EventType.MUSIC -> Icons.Filled.MusicNote
    EventType.FOOD -> Icons.Filled.Restaurant
    EventType.COMEDY -> Icons.Filled.TheaterComedy
    EventType.ART -> Icons.Filled.Palette
    EventType.SOOTHE -> Icons.Filled.Spa
}

@Composable
private fun ShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val shimmer = (sin((phase * 2 * PI).toDouble()).toFloat() * 0.5f + 0.5f)
    val c = lerp(ShimmerLow, ShimmerHigh, shimmer)
    return Brush.linearGradient(
        colors = listOf(ShimmerLow, c, ShimmerHigh, c, ShimmerLow),
        start = Offset(phase * 400f, 0f),
        end = Offset(phase * 400f + 200f, 120f)
    )
}

@Composable
private fun ShimmerBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ShimmerBrush())
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onFilterClick: (() -> Unit)? = null,
    currentFilter: EventFilter? = null,
    onSeeAllHotEvents: () -> Unit = {},
    onEventRsvp: (Event) -> Unit = {},
    onEventClick: (Event) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNavEvents: () -> Unit = {},
    onNavMap: () -> Unit = {},
    onNavDiscover: () -> Unit = {},
    onNavFavorites: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    selectedEvents: Boolean = true,
    selectedMap: Boolean = false,
    selectedDiscover: Boolean = false,
    selectedFavorites: Boolean = false,
    selectedProfile: Boolean = false
) {
    val ctx = LocalContext.current
    val app = remember(ctx.applicationContext) { ctx.applicationContext as EventPopApp }
    val localProfile by app.profileLocalDataStore.getProfile()
        .collectAsStateWithLifecycle(LocalProfile())
    val authSnap = AuthRepository.currentProfile()
    val displayName = localProfile.displayName.takeIf { it.isNotBlank() }
        ?: authSnap.displayName?.takeIf { it.isNotBlank() }
        ?: "Guest"

    var quickFilterOrdinal by rememberSaveable { mutableStateOf<Int?>(null) }
    val quickType: EventType? = quickFilterOrdinal?.let { EventType.entries.getOrNull(it) }

    val baseEvents =
        if (currentFilter != null) currentFilter.applyTo(uiState.events) else uiState.events
    val displayedEvents =
        if (quickType == null) baseEvents else baseEvents.filter { it.matchesQuickType(quickType) }

    val topAppBarScrollState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarScrollState)

    Scaffold(
        containerColor = BodyBackground,
        topBar = {
            EventPopHomeLargeTopBar(
                greeting = stringResource(timeOfDayGreetingRes()),
                displayName = displayName,
                scrollBehavior = scrollBehavior,
                onProfileClick = onNavProfile,
                profileContentDescription = stringResource(R.string.nav_profile),
                onSearchClick = onSearchClick,
                searchContentDescription = stringResource(R.string.search_title),
                avatarUrl = localProfile.avatarUrl,
                avatarLocalPath = localProfile.avatarLocalPath,
                avatarDisplayName = displayName
            )
        },
        bottomBar = {
            EventPopBottomBar(
                selectedEvents = selectedEvents,
                selectedMap = selectedMap,
                selectedDiscover = selectedDiscover,
                selectedFavorites = selectedFavorites,
                selectedProfile = selectedProfile,
                onNavEvents = onNavEvents,
                onNavMap = onNavMap,
                onNavDiscover = onNavDiscover,
                onNavFavorites = onNavFavorites,
                onNavProfile = onNavProfile
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(BodyBackground)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(SectionSpacing)
        ) {
            if (onFilterClick != null) {
                item(key = "filter_chips") {
                    FilterChipStrip(
                        quickFilterOrdinal = quickFilterOrdinal,
                        onQuickFilterChange = { quickFilterOrdinal = it },
                        onOpenFullFilters = onFilterClick
                    )
                }
            }

            if (uiState.hotEvents.isNotEmpty()) {
                item(key = "hot_header") {
                    SectionHeaderRow(
                        title = "🔥 ${stringResource(R.string.hot_events_near_you)}",
                        actionLabel = stringResource(R.string.see_all_arrow),
                        onAction = onSeeAllHotEvents
                    )
                }
                item(key = "hot_row") {
                    HotEventsRow(
                        hotEvents = uiState.hotEvents,
                        onEventClick = onEventClick
                    )
                }
            }

            if (uiState.isLoading) {
                items(
                    count = 3,
                    key = { idx -> "skeleton-$idx" }
                ) { _ ->
                    EventCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            }

            if (!uiState.isLoading && displayedEvents.isEmpty()) {
                item(key = "empty") {
                    HomeEmptyState(
                        onRetry = onFilterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 320.dp)
                    )
                }
            }

            items(
                items = displayedEvents,
                key = { it.id },
                contentType = { _ -> "event" }
            ) { event ->
                EventCard(
                    event = event,
                    onRsvp = { onEventRsvp(event) },
                    onClick = { onEventClick(event) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FilterChipStrip(
    quickFilterOrdinal: Int?,
    onQuickFilterChange: (Int?) -> Unit,
    onOpenFullFilters: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item(key = "all") {
            HomeFilterChip(
                label = stringResource(R.string.home_filter_all),
                selected = quickFilterOrdinal == null,
                onClick = { onQuickFilterChange(null) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
        items(EventType.entries.toList(), key = { it.name }) { type ->
            val ord = type.ordinal
            HomeFilterChip(
                label = type.label,
                selected = quickFilterOrdinal == ord,
                onClick = {
                    onQuickFilterChange(if (quickFilterOrdinal == ord) null else ord)
                },
                leadingIcon = {
                    Icon(
                        imageVector = type.leadingIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
        item(key = "more") {
            HomeFilterChip(
                label = stringResource(R.string.home_filter_more),
                selected = false,
                onClick = onOpenFullFilters,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) OrangeAccent else CardBackground,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) Color.White else SubtitleGray,
        animationSpec = tween(200),
        label = "chipFg"
    )
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = leadingIcon,
        modifier = Modifier
            .heightIn(min = 36.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        border = if (selected) null else BorderStroke(1.dp, ChipBorderGray),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            labelColor = labelColor,
            iconColor = labelColor,
            selectedContainerColor = OrangeAccent,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        )
    )
}

@Composable
private fun SectionHeaderRow(
    title: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AppBarNavy
        )
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelMedium,
            color = OrangeAccent,
            modifier = Modifier.clickable(onClick = onAction)
        )
    }
}

@Composable
private fun HotEventsRow(
    hotEvents: List<Event>,
    onEventClick: (Event) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(hotEvents, key = { it.id }) { event ->
            HotEventCard(
                event = event,
                onClick = { onEventClick(event) }
            )
        }
    }
}

@Composable
private fun HotEventCard(
    event: Event,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "hotScale"
    )
    Box(
        modifier = Modifier
            .width(HotCardWidth)
            .height(HotCardHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin.Center
            }
            .clip(RoundedCornerShape(CardCorner))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(event.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                else -> ShimmerBox(Modifier.fillMaxSize())
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
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
        Text(
            text = event.category.displayName.uppercase().take(5),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(OrangeAccent)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = event.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.subtitle,
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EventCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(CardCorner), ambientColor = ShadowBlack8, spotColor = ShadowBlack8)
            .clip(RoundedCornerShape(CardCorner))
            .background(CardBackground)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(
            modifier = Modifier
                .size(ThumbnailSize)
                .clip(RoundedCornerShape(12.dp))
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.72f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
        ShimmerBox(
            modifier = Modifier
                .width(72.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
private fun HomeEmptyState(
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_event_placeholder),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = SubtitleGray.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.home_no_events),
            style = MaterialTheme.typography.titleMedium,
            color = AppBarNavy,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_no_events_hint),
            style = MaterialTheme.typography.bodySmall,
            color = SubtitleGray,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onRetry,
                border = BorderStroke(1.dp, OrangeAccent),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangeAccent,
                    containerColor = CardBackground
                )
            ) {
                Text(stringResource(R.string.home_retry))
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    onRsvp: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin.Center
            }
            .shadow(4.dp, RoundedCornerShape(CardCorner), ambientColor = ShadowBlack8, spotColor = ShadowBlack8)
            .clip(RoundedCornerShape(CardCorner))
            .background(CardBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(ThumbnailSize)
                .clip(RoundedCornerShape(12.dp))
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                    else -> ShimmerBox(Modifier.fillMaxSize())
                }
            }
            if (event.rsvpCount != null) {
                Text(
                    text = stringResource(R.string.rsvp_count, event.rsvpCount),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(OrangeAccent)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = AppBarNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtitleGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (event.rating != null) {
                    StarRating(rating = event.rating)
                }
            }
            Button(
                onClick = onRsvp,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .defaultMinSize(minWidth = 72.dp)
                    .height(36.dp)
            ) {
                Text(
                    text = stringResource(R.string.rsvp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun StarRating(
    rating: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val fullStars = rating.toInt().coerceIn(0, 5)
        repeat(5) { index ->
            Icon(
                imageVector = if (index < fullStars) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (index < fullStars) OrangeAccent else SubtitleGray.copy(alpha = 0.35f)
            )
        }
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.labelSmall,
            color = SubtitleGray,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
