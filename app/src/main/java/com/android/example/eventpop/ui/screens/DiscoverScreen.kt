package com.android.example.eventpop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.ui.components.EventPopCenteredTopBar
import com.android.example.eventpop.ui.components.EventSummaryRow
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.mvc.DiscoverUiState
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.eventpop.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class CategoryItem(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private val allCategories = listOf(
    CategoryItem("Music", Icons.Filled.MusicNote, Color(0xFF0D9488)),
    CategoryItem("Food", Icons.Filled.Fastfood, Color(0xFFEA580C)),
    CategoryItem("Comedy", Icons.Filled.TheaterComedy, Color(0xFF7C3AED)),
    CategoryItem("Art", Icons.Filled.Palette, Color(0xFFDC2626)),
    CategoryItem("Wellness", Icons.Filled.SelfImprovement, Color(0xFF059669))
)

private val popularSearches = listOf(
    "Street Food", "DJ Party", "Comedy Night", "Rooftop",
    "Free Events", "This Weekend", "Live Music", "Art Gallery"
)

private val BodyBackground = Color(0xFFF4F6F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onNavEvents: () -> Unit,
    onNavMap: () -> Unit,
    onNavDiscover: () -> Unit,
    onNavFavorites: () -> Unit,
    onNavProfile: () -> Unit,
    onEventClick: (Event) -> Unit,
    uiState: DiscoverUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectedCategoryChange: (String?) -> Unit,
    onSelectedDateMillisChange: (Long?) -> Unit,
    onClearSearchAndFilters: () -> Unit,
    onRefresh: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.selectedDateMillis ?: System.currentTimeMillis()
    )

    val dateChipLabel = remember(uiState.selectedDateMillis) {
        uiState.selectedDateMillis?.let { millis ->
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onSelectedDateMillisChange(it) }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.discover_date_ok), color = OrangeAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.discover_date_cancel), color = SubtitleGray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val events = uiState.events
    val isLoading = uiState.isLoading
    val hasActiveFilters = uiState.searchQuery.isNotBlank() ||
        uiState.selectedCategory != null ||
        uiState.selectedDateMillis != null

    val discoverTopBarState = rememberTopAppBarState()
    val discoverScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(discoverTopBarState)

    Scaffold(
        containerColor = BodyBackground,
        topBar = {
            EventPopCenteredTopBar(
                title = stringResource(R.string.discover_title),
                scrollBehavior = discoverScrollBehavior
            )
        },
        bottomBar = {
            EventPopBottomBar(
                selectedEvents = false,
                selectedMap = false,
                selectedDiscover = true,
                selectedFavorites = false,
                selectedProfile = false,
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
                .nestedScroll(discoverScrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    placeholder = { Text(stringResource(R.string.discover_search_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = SubtitleGray
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onClearSearchAndFilters()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.discover_clear_search),
                                    tint = SubtitleGray
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedLabelColor = OrangeAccent,
                        cursorColor = OrangeAccent
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = uiState.selectedDateMillis != null,
                        onClick = { showDatePicker = true },
                        label = {
                            Text(dateChipLabel ?: stringResource(R.string.discover_filter_date))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeAccent.copy(alpha = 0.22f),
                            selectedLabelColor = OrangeAccent,
                            selectedLeadingIconColor = OrangeAccent
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    if (uiState.selectedDateMillis != null) {
                        TextButton(onClick = { onSelectedDateMillisChange(null) }) {
                            Text(stringResource(R.string.discover_clear), color = SubtitleGray)
                        }
                    }

                    FilterChip(
                        selected = uiState.selectedCategory != null,
                        onClick = { showCategoryPicker = !showCategoryPicker },
                        label = {
                            Text(uiState.selectedCategory ?: stringResource(R.string.discover_filter_category))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Category,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeAccent.copy(alpha = 0.22f),
                            selectedLabelColor = OrangeAccent,
                            selectedLeadingIconColor = OrangeAccent
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    if (uiState.selectedCategory != null) {
                        TextButton(
                            onClick = {
                                onSelectedCategoryChange(null)
                                showCategoryPicker = false
                            }
                        ) {
                            Text(stringResource(R.string.discover_clear), color = SubtitleGray)
                        }
                    }
                }
            }

            if (showCategoryPicker) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Surface(
                            color = CardBackground,
                            shape = RoundedCornerShape(18.dp),
                            shadowElevation = 3.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stringResource(R.string.discover_pick_category),
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppBarNavy
                                    )
                                    IconButton(onClick = { showCategoryPicker = false }) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.discover_close_picker),
                                            tint = SubtitleGray
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    allCategories.chunked(2).forEach { rowCats ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowCats.forEach { cat ->
                                                FilterChip(
                                                    selected = uiState.selectedCategory == cat.label,
                                                    onClick = {
                                                        val next =
                                                            if (uiState.selectedCategory == cat.label) null else cat.label
                                                        onSelectedCategoryChange(next)
                                                        showCategoryPicker = false
                                                    },
                                                    label = { Text(cat.label) },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = cat.icon,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = cat.color.copy(alpha = 0.22f),
                                                        selectedLabelColor = cat.color,
                                                        selectedLeadingIconColor = cat.color
                                                    ),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                            }
                                            if (rowCats.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangeAccent)
                    }
                }
            }

            if (hasActiveFilters) {
                item {
                    SectionHeader(
                        icon = Icons.Filled.Search,
                        title = stringResource(R.string.discover_results_header, events.size)
                    )
                }
                if (events.isEmpty() && !isLoading) {
                    item {
                        Text(
                            text = stringResource(R.string.discover_no_matches),
                            color = SubtitleGray,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                } else {
                    items(events, key = { it.id }) { event ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                            EventSummaryRow(event = event, onClick = { onEventClick(event) })
                        }
                    }
                }
            } else {
                item {
                    SectionHeader(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        title = stringResource(R.string.discover_popular_searches)
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        popularSearches.forEach { term ->
                            FilterChip(
                                selected = false,
                                onClick = { onSearchQueryChange(term) },
                                label = {
                                    Text(term, maxLines = 1, style = MaterialTheme.typography.labelLarge)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = CardBackground,
                                    labelColor = AppBarNavy
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
                        }
                    }
                }

                item {
                    SectionHeader(
                        icon = Icons.Filled.Whatshot,
                        title = stringResource(R.string.discover_browse_categories)
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        allCategories.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                row.forEach { cat ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        CategoryCard(
                                            category = cat,
                                            isSelected = uiState.selectedCategory == cat.label,
                                            onClick = {
                                                onSelectedCategoryChange(
                                                    if (uiState.selectedCategory == cat.label) null else cat.label
                                                )
                                            }
                                        )
                                    }
                                }
                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.discover_browse_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtitleGray,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                if (events.isNotEmpty() && !isLoading) {
                    item {
                        SectionHeader(
                            icon = Icons.Filled.Search,
                            title = stringResource(R.string.discover_from_feed)
                        )
                    }
                    items(events.take(8), key = { it.id }) { event ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                            EventSummaryRow(event = event, onClick = { onEventClick(event) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OrangeAccent,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = AppBarNavy
        )
    }
}

@Composable
private fun CategoryCard(
    category: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) {
        Brush.linearGradient(
            listOf(category.color.copy(alpha = 0.75f), category.color.copy(alpha = 0.45f))
        )
    } else {
        Brush.linearGradient(listOf(CardBackground, CardBackground))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = if (isSelected) 0.55f else 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else category.color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = category.label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else AppBarNavy,
                fontSize = 15.sp
            )
        }
    }
}
