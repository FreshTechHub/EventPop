package com.android.example.eventpop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.eventpop.data.EventCategory
import com.android.example.eventpop.data.SampleEvents
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class CategoryItem(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private val allCategories = listOf(
    CategoryItem("Music", Icons.Filled.MusicNote, Color(0xFF0D9488.toInt())),
    CategoryItem("Food", Icons.Filled.Fastfood, Color(0xFFEA580C.toInt())),
    CategoryItem("Comedy", Icons.Filled.TheaterComedy, Color(0xFF7C3AED.toInt())),
    CategoryItem("Art", Icons.Filled.Palette, Color(0xFFDC2626.toInt())),
    CategoryItem("Wellness", Icons.Filled.SelfImprovement, Color(0xFF059669.toInt()))
)

private val popularSearches = listOf(
    "Street Food", "DJ Party", "Comedy Night", "Rooftop Events",
    "Free Events", "This Weekend", "Live Music", "Art Gallery"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiscoverScreen(
    onNavEvents: () -> Unit,
    onNavMap: () -> Unit,
    onNavDiscover: () -> Unit,
    onNavFavorites: () -> Unit,
    onNavProfile: () -> Unit,
    onEventClick: (com.android.example.eventpop.data.Event) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedDateLabel by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    val filteredEvents = remember(query, selectedCategory) {
        val q = query.trim().lowercase()
        SampleEvents.list.filter { event ->
            val matchesQuery = q.isEmpty() ||
                event.title.lowercase().contains(q) ||
                event.location.lowercase().contains(q)
            val matchesCategory = selectedCategory == null ||
                event.category.displayName.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDateLabel = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK", color = OrangeAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = SubtitleGray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Discover",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBarNavy)
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = { Text("Search events, places...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = SubtitleGray
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear",
                                    tint = SubtitleGray
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = AppBarNavy
                    )
                )
            }

            // Filter buttons row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Date button
                    FilterChip(
                        selected = selectedDateLabel != null,
                        onClick = { showDatePicker = true },
                        label = {
                            Text(selectedDateLabel ?: "Date")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = if (selectedDateLabel != null) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear date",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { selectedDateLabel = null }
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeAccent.copy(alpha = 0.2f),
                            selectedLabelColor = OrangeAccent,
                            selectedLeadingIconColor = OrangeAccent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    // Category button
                    FilterChip(
                        selected = selectedCategory != null,
                        onClick = { showCategorySheet = true },
                        label = {
                            Text(selectedCategory ?: "Category")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Category,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = if (selectedCategory != null) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear category",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { selectedCategory = null }
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeAccent.copy(alpha = 0.2f),
                            selectedLabelColor = OrangeAccent,
                            selectedLeadingIconColor = OrangeAccent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Category inline picker (shown when showCategorySheet)
            if (showCategorySheet) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        color = CardBackground,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Select Category",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(onClick = { showCategorySheet = false }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = SubtitleGray)
                                }
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allCategories.forEach { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat.label,
                                        onClick = {
                                            selectedCategory = if (selectedCategory == cat.label) null else cat.label
                                            showCategorySheet = false
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
                                            selectedContainerColor = cat.color.copy(alpha = 0.2f),
                                            selectedLabelColor = cat.color,
                                            selectedLeadingIconColor = cat.color
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Show search results when query is active
            if (query.isNotEmpty() || selectedCategory != null || selectedDateLabel != null) {
                item {
                    SectionHeader(
                        icon = Icons.Filled.Search,
                        title = "Results (${filteredEvents.size})"
                    )
                }
                if (filteredEvents.isEmpty()) {
                    item {
                        Text(
                            text = "No events match your search.",
                            color = SubtitleGray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(filteredEvents, key = { it.id }) { event ->
                        DiscoverEventRow(event = event, onClick = { onEventClick(event) })
                    }
                }
            } else {
                // Popular Searches
                item {
                    SectionHeader(icon = Icons.Filled.TrendingUp, title = "Popular Searches")
                }
                item {
                    FlowRowBlock {
                        popularSearches.forEach { term ->
                            PopularSearchChip(label = term, onClick = { query = term })
                        }
                    }
                }

                // Categories
                item {
                    SectionHeader(icon = Icons.Filled.Whatshot, title = "Categories")
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
                                            isSelected = selectedCategory == cat.label,
                                            onClick = {
                                                selectedCategory = if (selectedCategory == cat.label) null else cat.label
                                            }
                                        )
                                    }
                                }
                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OrangeAccent,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowBlock(content: @Composable () -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun PopularSearchChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = CardBackground,
        tonalElevation = 2.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CategoryCard(
    category: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected)
        Brush.linearGradient(listOf(category.color.copy(alpha = 0.7f), category.color.copy(alpha = 0.4f)))
    else
        Brush.linearGradient(listOf(CardBackground, CardBackground))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
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
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = if (isSelected) 0.6f else 0.2f)),
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
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun DiscoverEventRow(
    event: com.android.example.eventpop.data.Event,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        color = CardBackground,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(event.category.markerColorHex.toInt()).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Whatshot,
                    contentDescription = null,
                    tint = Color(event.category.markerColorHex.toInt()),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = event.subtitle,
                    fontSize = 12.sp,
                    color = SubtitleGray
                )
            }
        }
    }
}
