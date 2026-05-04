package com.android.example.eventpop.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.example.eventpop.R
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.ui.components.AvatarComposable
import com.android.example.eventpop.ui.mvc.EventDetailUiState
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.ContentGray
import com.android.example.eventpop.ui.theme.DetailBackground
import com.android.example.eventpop.ui.theme.FreeGreen
import com.android.example.eventpop.ui.theme.HeartRed
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import kotlinx.coroutines.delay

private val HeroHeight = 300.dp
private val ToolbarScrollThreshold = 240.dp

private fun lightenColor(base: Color, amount: Float): Color = Color(
    red = base.red + (1f - base.red) * amount,
    green = base.green + (1f - base.green) * amount,
    blue = base.blue + (1f - base.blue) * amount,
    alpha = base.alpha
)

private val MetaPillBackground = lightenColor(DetailBackground, 0.08f)

private fun heroScrimBrush(): Brush = Brush.verticalGradient(
    colorStops = arrayOf(
        0f to Color.Transparent,
        0.5f to Color.Transparent,
        1f to DetailBackground
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    uiState: EventDetailUiState,
    onToggleInterested: () -> Unit,
    onSubmitRsvp: () -> Unit,
    onConsumeRsvpSuccess: () -> Unit,
    onEditEvent: () -> Unit = {},
    onDeleteEvent: () -> Unit = {}
) {
    val event = uiState.event
    val isOwner = uiState.isOwner
    val isInterested = uiState.isInterested
    val rsvpSuccess = uiState.rsvpSuccess
    val rsvpLoading = uiState.rsvpLoading
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val view = LocalView.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val heartColor by animateColorAsState(
        targetValue = if (isInterested) HeartRed else Color.White,
        animationSpec = tween(200),
        label = "heart"
    )

    LaunchedEffect(rsvpSuccess) {
        if (rsvpSuccess) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.rsvp_success_message),
                withDismissAction = true
            )
            onConsumeRsvpSuccess()
        }
    }

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val oldLight = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = oldLight ?: true
        }
    }

    if (showDeleteConfirm && event != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.event_detail_delete_title)) },
            text = { Text(stringResource(R.string.event_detail_delete_message, event.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteEvent()
                    }
                ) {
                    Text(stringResource(R.string.event_detail_delete_confirm), color = HeartRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.create_event_dialog_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.background(DetailBackground),
        containerColor = DetailBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            event?.let { ev ->
                EventDetailBottomBar(
                    event = ev,
                    rsvpSuccess = rsvpSuccess,
                    rsvpLoading = rsvpLoading,
                    onRsvpClick = onSubmitRsvp
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DetailBackground)
                .padding(innerPadding)
        ) {
            if (event != null) {
                val listState = rememberLazyListState()
                val density = LocalDensity.current
                val thresholdPx = remember(density) {
                    with(density) { ToolbarScrollThreshold.toPx() }
                }
                val rawBarAlpha by remember(listState, thresholdPx) {
                    derivedStateOf {
                        if (listState.firstVisibleItemIndex > 0) {
                            1f
                        } else {
                            (listState.firstVisibleItemScrollOffset / thresholdPx).coerceIn(0f, 1f)
                        }
                    }
                }
                val barBgAlpha by animateFloatAsState(
                    targetValue = rawBarAlpha,
                    animationSpec = tween(200),
                    label = "toolbarBg"
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item(key = "hero") {
                        HeroSection(event = event)
                    }
                    item(key = "title") {
                        TitleAndMetaBlock(
                            event = event,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    item(key = "vibe") {
                        VibeCheckBlock(
                            event = event,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)
                        )
                    }
                    item(key = "about") {
                        AboutBlock(
                            event = event,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)
                        )
                    }
                    item(key = "organizer") {
                        OrganizerBlock(
                            event = event,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)
                        )
                    }
                    item(key = "map") {
                        MapBlock(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp)
                        )
                    }
                }

                EventDetailFloatingToolbar(
                    barBgAlpha = barBgAlpha,
                    heartColor = heartColor,
                    isInterested = isInterested,
                    isOwner = isOwner,
                    onBack = { navController.popBackStack() },
                    onToggleInterested = onToggleInterested,
                    onShare = { /* share */ },
                    onEditEvent = onEditEvent,
                    onDeleteRequest = { showDeleteConfirm = true }
                )
            } else {
                EventDetailLoadingShimmer()
            }
        }
    }
}

@Composable
private fun EventDetailFloatingToolbar(
    barBgAlpha: Float,
    heartColor: Color,
    isInterested: Boolean,
    isOwner: Boolean,
    onBack: () -> Unit,
    onToggleInterested: () -> Unit,
    onShare: () -> Unit,
    onEditEvent: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(AppBarNavy.copy(alpha = barBgAlpha))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularToolbarIcon(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOwner) {
                CircularToolbarIcon(onClick = onEditEvent) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.event_detail_edit),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                CircularToolbarIcon(onClick = onDeleteRequest) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.event_detail_delete),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            CircularToolbarIcon(onClick = onShare) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.share),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            CircularToolbarIcon(onClick = onToggleInterested) {
                Icon(
                    imageVector = if (isInterested) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(R.string.content_desc_favorite),
                    tint = heartColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CircularToolbarIcon(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.4f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun EventDetailLoadingShimmer() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerShift"
    )
    val shimmerLow = lightenColor(AppBarNavy, 0.12f)
    val shimmerHigh = ContentGray.copy(alpha = 0.2f)
    val brush = Brush.linearGradient(
        colors = listOf(shimmerLow, shimmerHigh, shimmerLow),
        start = Offset(shift * 400f, 0f),
        end = Offset(shift * 400f + 200f, 200f)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeroHeight)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(20.dp))
        repeat(3) { i ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = when (i) {
                        0 -> 0.92f
                        1 -> 0.72f
                        else -> 0.55f
                    })
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(brush)
                )
            }
        }
    }
}

@Composable
private fun HeroSection(event: Event) {
    var scaleTarget by remember { mutableFloatStateOf(0.97f) }
    LaunchedEffect(event) {
        scaleTarget = 1f
    }
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(400),
        label = "heroScale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeroHeight)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = scale; scaleY = scale }
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
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBarNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(heroScrimBrush())
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(OrangeAccent)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = event.category.displayName.uppercase(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(bottom = 4.dp),
        fontSize = 11.sp,
        color = SubtitleGray,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun TitleAndMetaBlock(event: Event, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(event) {
        delay(0L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 12 },
        modifier = modifier
    ) {
        Column {
            Text(
                text = event.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 34.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            val locationText = event.area ?: event.location
            val dateText = event.date ?: event.timeInfo
            val timeText =
                if (event.startTime != null && event.endTime != null) {
                    "${event.startTime} - ${event.endTime}"
                } else {
                    event.timeInfo
                }
            val headcount = stringResource(R.string.people_going, event.rsvpCount ?: 0)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    MetaPill(
                        icon = Icons.Filled.LocationOn,
                        label = locationText
                    )
                }
                item {
                    MetaPill(
                        icon = Icons.Filled.CalendarToday,
                        label = dateText
                    )
                }
                item {
                    MetaPill(
                        icon = Icons.Filled.AccessTime,
                        label = timeText
                    )
                }
                item {
                    MetaPill(
                        icon = Icons.Filled.Group,
                        label = headcount
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (event.isFree) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(FreeGreen)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "FREE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(OrangeAccent)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = event.priceInfo,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaPill(
    icon: ImageVector,
    label: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MetaPillBackground)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = OrangeAccent
        )
        Text(
            text = label,
            color = ContentGray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VibeCheckBlock(event: Event, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(event) {
        delay(100L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 12 },
        modifier = modifier
    ) {
        val rating = event.rating ?: 0f
        val filled = rating.toInt().coerceIn(0, 5)
        Column {
            SectionHeader(stringResource(R.string.vibe_check).uppercase())
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "%.1f".format(rating),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < filled) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (index < filled) {
                                    OrangeAccent
                                } else {
                                    ContentGray.copy(alpha = 0.3f)
                                }
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.rating_out_of_five, rating),
                        color = ContentGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutBlock(event: Event, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(event.id) {
        expanded = false
    }
    LaunchedEffect(event) {
        delay(200L)
        visible = true
    }
    val description = event.description ?: "No description provided."
    val interactionSource = remember { MutableInteractionSource() }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 12 },
        modifier = modifier
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            SectionHeader(stringResource(R.string.detail_section_about).uppercase())
            Text(
                text = description,
                color = ContentGray,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (expanded) {
                    stringResource(R.string.detail_show_less)
                } else {
                    stringResource(R.string.detail_read_more)
                },
                color = OrangeAccent,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { expanded = !expanded }
                    )
            )
        }
    }
}

@Composable
private fun OrganizerBlock(event: Event, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(event) {
        delay(300L)
        visible = true
    }
    val name = event.organizerName ?: "Unknown Organizer"
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 12 },
        modifier = modifier
    ) {
        Column {
            SectionHeader(stringResource(R.string.detail_section_organizer).uppercase())
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarComposable(
                    avatarUrl = "",
                    avatarLocalPath = "",
                    displayName = name,
                    size = 44.dp,
                    initialsFontSize = 14.sp,
                    modifier = Modifier.clip(CircleShape)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.detail_organizer_role),
                        color = ContentGray,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent.copy(alpha = 0.1f))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = stringResource(R.string.contact_organizer),
                        tint = OrangeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapBlock(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 12 },
        modifier = modifier
    ) {
        Column {
            SectionHeader(stringResource(R.string.location_title).uppercase())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppBarNavy)
                    .drawBehind {
                        val stroke = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                            cap = StrokeCap.Round
                        )
                        drawRoundRect(
                            color = ContentGray.copy(alpha = 0.2f),
                            style = stroke,
                            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = ContentGray.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.map_coming_soon),
                        color = ContentGray.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailBottomBar(
    event: Event,
    rsvpSuccess: Boolean,
    rsvpLoading: Boolean,
    onRsvpClick: () -> Unit
) {
    val buttonBg by animateColorAsState(
        targetValue = if (rsvpSuccess) FreeGreen else OrangeAccent,
        animationSpec = tween(300),
        label = "rsvpButtonBg"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBarNavy)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.08f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (event.isFree) {
                    Text(
                        text = "FREE",
                        color = FreeGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Text(
                        text = event.priceInfo,
                        color = OrangeAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = stringResource(R.string.detail_per_person),
                    color = ContentGray,
                    fontSize = 11.sp
                )
            }
            Button(
                onClick = onRsvpClick,
                enabled = !rsvpSuccess,
                modifier = Modifier
                    .width(140.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeAccent,
                    disabledContainerColor = buttonBg,
                    contentColor = Color.White,
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                when {
                    rsvpLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    rsvpSuccess -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "RSVP'd",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.rsvp_now),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
