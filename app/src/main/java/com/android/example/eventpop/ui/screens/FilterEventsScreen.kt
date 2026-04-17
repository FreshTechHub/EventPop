package com.android.example.eventpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.eventpop.R
import androidx.navigation.NavController
import com.android.example.eventpop.data.EventFilter
import com.android.example.eventpop.data.EventLocation
import com.android.example.eventpop.data.EventType
import com.android.example.eventpop.data.TimeRange
import com.android.example.eventpop.ui.navigation.EventPopDestinations
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.viewmodel.FilterEventsViewModel

private val FilterScreenBackground = Color(0xFF0D1117)
private val FilterChipSelected = Color(0xFF2196F3)
private val FilterApplyOrange = Color(0xFFFF6B00)
private val SectionHeaderColor = Color.White

private const val FILTER_RESULT_KEY = "event_filter"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterEventsScreen(
    navController: NavController,
    viewModel: FilterEventsViewModel = viewModel()
) {
    val filter by viewModel.filter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.filter_events_title),
                        color = SectionHeaderColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SectionHeaderColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBarNavy,
                    titleContentColor = SectionHeaderColor,
                    navigationIconContentColor = SectionHeaderColor
                )
            )
        },
        containerColor = FilterScreenBackground,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FilterScreenBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetFilters() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SectionHeaderColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SectionHeaderColor
                    )
                ) {
                    Text(text = stringResource(R.string.filter_reset), fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = {
                        val result = viewModel.applyFilters()
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(EventPopDestinations.FILTER_RESULT_KEY, result)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FilterApplyOrange,
                        contentColor = SectionHeaderColor
                    )
                ) {
                    Text(text = stringResource(R.string.filter_apply), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            var section1Visible by remember { mutableStateOf(false) }
            var section2Visible by remember { mutableStateOf(false) }
            var section3Visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                section1Visible = true
            }
            LaunchedEffect(section1Visible) {
                if (section1Visible) section2Visible = true
            }
            LaunchedEffect(section2Visible) {
                if (section2Visible) section3Visible = true
            }

            AnimatedVisibility(
                visible = section1Visible,
                enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(300))
            ) {
                FilterSectionLabel("By Event Type:")
                Spacer(modifier = Modifier.height(8.dp))
                FlowRowChips(
                    eventTypes = EventType.entries,
                    selectedTypes = filter.selectedTypes,
                    onToggleType = viewModel::toggleEventType
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = section2Visible,
                enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(300))
            ) {
                FilterSectionLabel("By Location:")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventLocation.entries.forEach { location ->
                        val selected = filter.selectedLocation == location
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setLocation(location) },
                            label = { Text(location.label) },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                                        tint = SectionHeaderColor
                                    )
                                }
                            } else null,
                            colors = filterChipColors(),
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = section3Visible,
                enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(300))
            ) {
                FilterSectionLabel("By Time:")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeRange.entries.forEach { time ->
                        val selected = filter.selectedTime == time
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setTimeRange(time) },
                            label = { Text(time.label) },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                                        tint = SectionHeaderColor
                                    )
                                }
                            } else null,
                            colors = filterChipColors(),
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        color = SectionHeaderColor,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowChips(
    eventTypes: List<EventType>,
    selectedTypes: Set<EventType>,
    onToggleType: (EventType) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        eventTypes.forEach { type ->
            val selected = type in selectedTypes
            FilterChip(
                selected = selected,
                onClick = { onToggleType(type) },
                label = { Text(type.label) },
                leadingIcon = {
                    Icon(
                        imageVector = if (selected) Icons.Filled.Check else type.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = SectionHeaderColor
                    )
                },
                colors = filterChipColors(),
                shape = RoundedCornerShape(50)
            )
        }
    }
}

@Composable
private fun filterChipColors(): SelectableChipColors =
    FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        labelColor = SectionHeaderColor,
        iconColor = SectionHeaderColor,
        selectedContainerColor = FilterChipSelected,
        selectedLabelColor = SectionHeaderColor,
        selectedLeadingIconColor = SectionHeaderColor
    )
