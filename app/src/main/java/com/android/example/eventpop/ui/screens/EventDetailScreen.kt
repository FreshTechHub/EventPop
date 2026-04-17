package com.android.example.eventpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.example.eventpop.R
import com.android.example.eventpop.data.model.Event
import com.android.example.eventpop.ui.theme.ContentGray
import com.android.example.eventpop.ui.theme.DetailBackground
import com.android.example.eventpop.ui.theme.FreeGreen
import com.android.example.eventpop.ui.theme.HeartRed
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.RsvpBarNavy
import com.android.example.eventpop.ui.theme.RsvpSuccessGreen
import com.android.example.eventpop.ui.theme.GradientPurple
import com.android.example.eventpop.ui.viewmodel.EventDetailViewModel
import kotlinx.coroutines.delay

private val HeroHeight = 280.dp
private val HeroGradient = Brush.verticalGradient(
    colors = listOf(OrangeAccent, GradientPurple)
)
private val HeroScrim = Brush.verticalGradient(
    colors = listOf(Color.Transparent, DetailBackground)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    viewModel: EventDetailViewModel,
    navBackStackEntry: NavBackStackEntry
) {
    val eventId = navBackStackEntry.arguments?.getString("eventId")
    val event by viewModel.event.collectAsState()
    val isInterested by viewModel.isInterested.collectAsState()
    val rsvpSuccess by viewModel.rsvpSuccess.collectAsState()
    val rsvpLoading by viewModel.rsvpLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val heartColor by animateColorAsState(
        targetValue = if (isInterested) HeartRed else Color.White,
        animationSpec = tween(200),
        label = "heart"
    )

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId ?: "")
    }

    LaunchedEffect(rsvpSuccess) {
        if (rsvpSuccess) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.rsvp_success_message),
                withDismissAction = true
            )
        }
    }

    Scaffold(
        modifier = Modifier.background(DetailBackground),
        containerColor = DetailBackground,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleInterested() }) {
                        Icon(
                            imageVector = if (isInterested) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(R.string.content_desc_favorite),
                            tint = heartColor
                        )
                    }
                    IconButton(onClick = { /* share */ }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.share),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            event?.let { ev ->
                EventDetailBottomBar(
                    event = ev,
                    rsvpSuccess = rsvpSuccess,
                    rsvpLoading = rsvpLoading,
                    onRsvpClick = { viewModel.submitRsvp() }
                )
            }
        }
    ) { innerPadding ->
        event?.let { ev ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HeroSection(event = ev)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    TitleAndMetaBlock(ev)
                    HorizontalDivider(color = Color(0xFF1B2A4A), modifier = Modifier.padding(vertical = 12.dp))
                    VibeCheckBlock(ev)
                    HorizontalDivider(color = Color(0xFF1B2A4A), modifier = Modifier.padding(vertical = 12.dp))
                    AboutBlock(ev)
                    HorizontalDivider(color = Color(0xFF1B2A4A), modifier = Modifier.padding(vertical = 12.dp))
                    OrganizerBlock(ev)
                    HorizontalDivider(color = Color(0xFF1B2A4A), modifier = Modifier.padding(vertical = 12.dp))
                    MapBlock()
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading…",
                    color = ContentGray
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
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
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
                        .background(HeroGradient)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HeroScrim),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White.copy(alpha = 0.2f)
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(OrangeAccent)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = event.category.label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TitleAndMetaBlock(event: Event) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(event) {
        delay(0L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = event.title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.area_kampala, event.area),
                    color = ContentGray,
                    fontSize = 14.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(text = event.date, color = ContentGray, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccessTime, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.time_range, event.startTime, event.endTime),
                    color = ContentGray,
                    fontSize = 14.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Group, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.people_going, event.rsvpCount),
                    color = ContentGray,
                    fontSize = 14.sp
                )
            }
            if (event.isFree) {
                Text(
                    text = stringResource(R.string.free_entry),
                    color = FreeGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = stringResource(R.string.price_ugx, event.price),
                    color = OrangeAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VibeCheckBlock(event: Event) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(event) {
        delay(80L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.StarRate, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.vibe_check),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < event.rating.toInt().coerceIn(0, 5)) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (index < event.rating.toInt().coerceIn(0, 5)) OrangeAccent else ContentGray
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.rating_out_of_five, event.rating),
                    color = ContentGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AboutBlock(event: Event) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(event) {
        delay(160L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.about_this_event),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = event.description,
                color = ContentGray,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun OrganizerBlock(event: Event) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(event) {
        delay(240L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.organized_by),
                    color = ContentGray,
                    fontSize = 14.sp
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = event.organizerName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            OutlinedButton(
                onClick = { },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
            ) {
                Text(stringResource(R.string.contact_organizer))
            }
        }
    }
}

@Composable
private fun MapBlock() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(320L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Map, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.location_title),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(RsvpBarNavy, RsvpBarNavy.copy(alpha = 0.8f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.map_coming_soon),
                        color = ContentGray,
                        fontSize = 14.sp
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
    val rsvpButtonColor by animateColorAsState(
        targetValue = if (rsvpSuccess) RsvpSuccessGreen else Color.Transparent,
        animationSpec = tween(300),
        label = "rsvpButton"
    )
    BottomAppBar(
        containerColor = RsvpBarNavy,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (event.isFree) {
                Text(
                    text = stringResource(R.string.free),
                    color = FreeGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = stringResource(R.string.price_ugx, event.price),
                    color = OrangeAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Button(
            onClick = onRsvpClick,
            enabled = !rsvpSuccess,
            modifier = Modifier
                .height(48.dp)
                .padding(end = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = rsvpButtonColor,
                contentColor = Color.White,
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp),
            contentPadding = ButtonDefaults.ContentPadding,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            if (rsvpLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else if (rsvpSuccess) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.rsvpd),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(OrangeAccent, GradientPurple)),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.rsvp_now),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
