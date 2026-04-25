package com.android.example.eventpop.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.example.eventpop.R
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventCategory
import com.android.example.eventpop.data.EventFilter
import com.android.example.eventpop.data.EventLocation
import com.android.example.eventpop.data.EventType
import com.android.example.eventpop.data.SampleEvents
import com.android.example.eventpop.data.TimeRange
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.EventPopTheme
import com.android.example.eventpop.ui.theme.HotSectionNavy
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.StarFilled
import com.android.example.eventpop.ui.theme.StarUnfilled
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.theme.SubtitleGray


private val EventThumbnailHeight = 80.dp
private val EventThumbnailWidth = 80.dp
private val CardElevation = 2.dp
private val CardShape = RoundedCornerShape(12.dp)
private val HotSectionShape = RoundedCornerShape(12.dp)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    events: List<Event> = SampleEvents.list,
    onFilterClick: (() -> Unit)? = null,
    currentFilter: EventFilter? = null,
    onSeeAllHotEvents: () -> Unit = {},
    onEventRsvp: (Event) -> Unit = {},
    onEventClick: (Event) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNavEvents: () -> Unit = {},
    onNavMap: () -> Unit = {},
    onNavDiscover: () -> Unit = {},
    onNavProfile: () -> Unit = {},
    selectedEvents: Boolean = true,
    selectedMap: Boolean = false,
    selectedDiscover: Boolean = false,
    selectedProfile: Boolean = false
) {
    val displayedEvents = if (currentFilter != null) currentFilter.applyTo(events) else events
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_greeting),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search_title),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBarNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            EventPopBottomBar(
                selectedEvents = selectedEvents,
                selectedMap = selectedMap,
                selectedDiscover = selectedDiscover,
                selectedProfile = selectedProfile,
                onNavEvents = onNavEvents,
                onNavMap = onNavMap,
                onNavDiscover = onNavDiscover,
                onNavProfile = onNavProfile
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onFilterClick != null) {
                item {
                    OutlinedButton(
                        onClick = onFilterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangeAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.filter_events_title),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            item {
                HotEventsSection(
                    onSeeAll = onSeeAllHotEvents
                )
            }
            items(displayedEvents) { event ->
                EventCard(
                    event = event,
                    onRsvp = { onEventRsvp(event) },
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
}

@Composable
private fun HotEventsSection(onSeeAll: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = HotSectionShape,
        colors = CardDefaults.cardColors(containerColor = HotSectionNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\uD83D\uDD25 ${stringResource(R.string.hot_events_title)}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.see_all),
                color = OrangeAccent,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(onClick = onSeeAll)
            )
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    onRsvp: () -> Unit,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(EventThumbnailWidth, EventThumbnailHeight)
                    .clip(RoundedCornerShape(8.dp))
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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_event_placeholder),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                if (event.rsvpCount != null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.rsvp_count, event.rsvpCount),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = event.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = event.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtitleGray
                        )
                        if (event.rating != null) {
                            StarRating(
                                rating = event.rating,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                Button(
                    onClick = onRsvp,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.ContentPadding,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.rsvp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
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
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val fullStars = rating.toInt().coerceIn(0, 5)
        repeat(5) { index ->
            Icon(
                imageVector = if (index < fullStars) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (index < fullStars) StarFilled else StarUnfilled
            )
        }
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.labelSmall,
            color = SubtitleGray,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun HomeScreenPreview() {
    EventPopTheme {
        HomeScreen(events = SampleEvents.list, selectedEvents = true)
    }
}

