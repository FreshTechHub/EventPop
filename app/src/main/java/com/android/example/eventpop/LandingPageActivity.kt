package com.android.example.eventpop

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.eventpop.ui.theme.EventPopTheme

// Color constants
private val LandingBackground = Color(0xFF0D1117)
private val GradientOrange = Color(0xFFFF6B00)
private val GradientPurple = Color(0xFF7B2FBE)
private val LandingNavy = Color(0xFF1B2A4A)
private val LandingDarkCard = Color(0xFF1A1A2E)
private val TextGray = Color(0xFFAAAAAA)
private val TextLightGray = Color(0xFFCCCCCC)

// Data models
data class Feature(
    val icon: String,
    val title: String,
    val description: String
)

data class EventPreview(
    val title: String,
    val location: String,
    val priceLabel: String,
    val isFree: Boolean
)

class LandingPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventPopTheme {
                LandingPage(onGetStarted = {
                    startActivity(Intent(this, MainActivity::class.java))
                })
            }
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandingPage(onGetStarted: () -> Unit) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    BackHandler {
        context.findActivity()?.finish()
    }

    Scaffold(
        containerColor = LandingBackground
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(LandingBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item(key = "hero") {
                HeroSection(
                    listState = listState,
                    index = 0,
                    onGetStarted = onGetStarted
                )
            }
            item(key = "stats") {
                StatsRow(listState = listState, index = 1)
            }
            item(key = "features") {
                FeaturesSection(listState = listState, index = 2)
            }
            item(key = "steps") {
                HowItWorksSection(listState = listState, index = 3)
            }
            item(key = "events") {
                EventPreviewSection(listState = listState, index = 4)
            }
            item(key = "cta") {
                CtaSection(listState = listState, index = 5, onGetStarted = onGetStarted)
            }
            item(key = "footer") {
                FooterSection(listState = listState, index = 6)
            }
        }
    }
}

@Composable
private fun sectionVisible(listState: LazyListState, index: Int): Boolean {
    val visible by remember(listState) {
        derivedStateOf {
            val first = listState.firstVisibleItemIndex
            val last = (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: first)
            index in first - 1..last + 1
        }
    }
    return visible
}

@Composable
private fun HeroSection(
    listState: LazyListState,
    index: Int,
    onGetStarted: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val show = sectionVisible(listState, index)

    LaunchedEffect(show) {
        if (show) visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) +
            slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(600))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            FloatingIconsBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val headline = buildAnnotatedString {
                        append("Discover What's Happening ")
                        withStyle(
                            SpanStyle(
                                brush = Brush.verticalGradient(
                                    listOf(GradientOrange, GradientPurple)
                                )
                            )
                        ) {
                            append("Around You")
                        }
                    }
                    Text(
                        text = headline,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Text(
                        text = "Find pop-ups, food fests, concerts & hidden events in Kampala",
                        fontSize = 15.sp,
                        color = TextGray
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GradientButton(
                        text = "Get Started",
                        onClick = onGetStarted
                    )
                    OutlinedLandingButton(
                        text = "Learn More",
                        onClick = {
                            // Scroll to features roughly
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingIconsBackground() {
    val icons = listOf("📍", "🎵", "🍔", "🎨", "🎭")
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    icons.forEachIndexed { index, icon ->
        val amplitude = 10 + index * 4
        val duration = 4000 + index * 600
        val phase = index * 40

        val offsetY by infiniteTransition.animateFloat(
            initialValue = -amplitude.toFloat(),
            targetValue = amplitude.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = LinearEasing, delayMillis = phase),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetY$index"
        )

        val offsetX by infiniteTransition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration * 2, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetX$index"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier
                    .align(
                        when (index) {
                            0 -> Alignment.TopStart
                            1 -> Alignment.TopEnd
                            2 -> Alignment.CenterStart
                            3 -> Alignment.CenterEnd
                            else -> Alignment.BottomCenter
                        }
                    )
                    .offset(
                        x = offsetX.dp,
                        y = offsetY.dp
                    ),
                color = TextLightGray.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun StatsRow(listState: LazyListState, index: Int) {
    var visible by remember { mutableStateOf(false) }
    val show = sectionVisible(listState, index)
    LaunchedEffect(show) {
        if (show) visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = LandingNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(target = 500, label = "Events +")
                StatItem(target = 10_000, label = "Users +")
                StatItem(target = 20, label = "Areas +")
            }
        }
    }
}

@Composable
private fun StatItem(target: Int, label: String) {
    var started by remember { mutableStateOf(false) }
    val animated by animateIntAsState(
        targetValue = if (started) target else 0,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "stat"
    )

    LaunchedEffect(Unit) {
        started = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "%,d".format(animated),
            color = GradientOrange,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            color = TextGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun FeaturesSection(listState: LazyListState, index: Int) {
    var visible by remember { mutableStateOf(false) }
    val show = sectionVisible(listState, index)
    LaunchedEffect(show) {
        if (show) visible = true
    }

    val features = listOf(
        Feature("🗺️", "Live Map", "See events pinned across Kampala in real time."),
        Feature("🔔", "Instant Alerts", "Get notified when new pop-ups go live."),
        Feature("🎟️", "Quick RSVP", "Join events with a single tap."),
        Feature("🎨", "Event Types", "Filter by music, food, art & more."),
        Feature("⭐", "Vibe Checks", "See ratings before you commit."),
        Feature("📍", "Add Pop-Ups", "Host your own micro-events easily.")
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) +
            slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(500))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Everything You Need",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                gridItems(features) { feature ->
                    FeatureCard(feature)
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: Feature) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LandingDarkCard),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GradientOrange.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientOrange, GradientPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = feature.icon, fontSize = 18.sp)
            }
            Text(
                text = feature.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = feature.description,
                color = TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun HowItWorksSection(listState: LazyListState, index: Int) {
    var visible by remember { mutableStateOf(false) }
    val show = sectionVisible(listState, index)
    LaunchedEffect(show) {
        if (show) visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "3 Simple Steps",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            val steps = listOf(
                "Download App" to "Install PopUp Kampala from the Play Store.",
                "Allow Location" to "Let us find events closest to you.",
                "Discover & RSVP" to "Browse, vibe check and tap to join."
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                steps.forEachIndexed { idx, (title, desc) ->
                    StepItem(
                        index = idx + 1,
                        title = title,
                        description = desc,
                        showConnector = idx < steps.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    index: Int,
    title: String,
    description: String,
    showConnector: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientOrange, GradientPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            if (showConnector) {
                Canvas(
                    modifier = Modifier
                        .height(40.dp)
                        .width(2.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    drawLine(
                        color = GradientOrange.copy(alpha = 0.6f),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 4f,
                        pathEffect = pathEffect
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = description,
                color = TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EventPreviewSection(listState: LazyListState, index: Int) {
    var visible by remember { mutableStateOf(false) }
    val show = sectionVisible(listState, index)
    LaunchedEffect(show) {
        if (show) visible = true
    }

    val events = listOf(
        EventPreview("Street Food Fest", "Bugolobi", "Free", true),
        EventPreview("Live Band Night", "Kololo", "UGX 10K", false),
        EventPreview("Friday Comedy Night", "Wandegeya", "Free", true)
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Hot Events Right Now 🔥",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    EventCard(event = event)
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: EventPreview) {
    Card(
        modifier = Modifier
            .width(220.dp),
        colors = CardDefaults.cardColors(containerColor = LandingDarkCard),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientOrange, GradientPurple),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    )
            )
            Text(
                text = event.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = event.location,
                color = TextGray,
                fontSize = 12.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.priceLabel,
                    color = GradientOrange,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Badge(
                    containerColor = if (event.isFree) GradientOrange.copy(alpha = 0.2f) else LandingNavy,
                    contentColor = GradientOrange,
                    modifier = Modifier
                ) {
                    Text(
                        text = if (event.isFree) "FREE" else "PAID",
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CtaSection(
    listState: LazyListState,
    index: Int,
    onGetStarted: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val show = sectionVisible(listState, index)
    LaunchedEffect(show) {
        if (show) visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) +
            slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(GradientOrange, GradientPurple)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ready to Explore Kampala?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Pop-ups, hidden gems and live events — all in one place.",
                    color = TextLightGray,
                    fontSize = 13.sp
                )

                GradientButton(
                    text = "Get Started",
                    backgroundBrush = Brush.horizontalGradient(
                        colors = listOf(Color.White, TextLightGray)
                    ),
                    contentColor = LandingBackground,
                    modifier = Modifier
                        .fillMaxWidth(),
                    indicationRadius = 300.dp,
                    onClick = onGetStarted
                )
            }
        }
    }
}

@Composable
private fun FooterSection(listState: LazyListState, index: Int) {
    var visible by remember { mutableStateOf(false) }
    val show = sectionVisible(listState, index)
    LaunchedEffect(show) {
        if (show) visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = LandingDarkCard),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "📍", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PopUp Kampala",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = "Your city. Your events. Your vibe.",
                    color = TextGray,
                    fontSize = 12.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialCircle("IG")
                    SocialCircle("X")
                    SocialCircle("WA")
                }
            }
        }
    }
}

@Composable
private fun SocialCircle(label: String) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(GradientOrange, GradientPurple)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundBrush: Brush = Brush.horizontalGradient(colors = listOf(GradientOrange, GradientPurple)),
    contentColor: Color = Color.White,
    indicationRadius: Dp = 200.dp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        border = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush, shape = RoundedCornerShape(50))
                .clip(RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun OutlinedLandingButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
