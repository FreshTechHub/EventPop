@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.android.example.eventpop.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.android.example.eventpop.R
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.ui.components.EventPopCenteredTopBar
import com.android.example.eventpop.ui.mvc.HostedEventsUiState
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OnAppBar
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray

private val BodyBackground = Color(0xFFF4F6F9)

@Composable
fun MyHostedEventsScreen(
    uiState: HostedEventsUiState,
    modifier: Modifier = Modifier,
    onEventClick: (Event) -> Unit,
    onEditEvent: (Event) -> Unit,
    onCreateFirstEvent: () -> Unit,
    onDeleteRequested: (Event) -> Unit,
    onSignIn: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val hostedTopBarState = rememberTopAppBarState()
    val hostedScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(hostedTopBarState)

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val oldLight = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = oldLight ?: true
        }
    }

    var pendingDelete by remember { mutableStateOf<Event?>(null) }

    pendingDelete?.let { ev ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = {
                Text(
                    stringResource(R.string.hosted_events_delete_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color = AppBarNavy
                )
            },
            text = {
                Text(
                    stringResource(R.string.hosted_events_delete_confirm_body, ev.title),
                    color = SubtitleGray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = pendingDelete
                        pendingDelete = null
                        target?.let(onDeleteRequested)
                    }
                ) {
                    Text(stringResource(R.string.hosted_events_delete), color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.create_event_dialog_cancel), color = SubtitleGray)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = BodyBackground,
        contentColor = AppBarNavy,
        topBar = {
            EventPopCenteredTopBar(
                title = stringResource(R.string.hosted_events_title),
                scrollBehavior = hostedScrollBehavior,
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.EventAvailable,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = OnAppBar
                    )
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BodyBackground)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            uiState.needsSignIn -> {
                HostedEmptyMessage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    title = stringResource(R.string.hosted_events_sign_in_title),
                    body = stringResource(R.string.hosted_events_sign_in_body),
                    primaryLabel = stringResource(R.string.auth_sign_in),
                    onPrimary = onSignIn,
                    icon = { Icon(Icons.Outlined.EventAvailable, null, Modifier.size(52.dp), OrangeAccent) }
                )
            }
            uiState.needsOrganizerRole -> {
                HostedEmptyMessage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    title = stringResource(R.string.hosted_events_title),
                    body = stringResource(R.string.create_event_organizer_required),
                    primaryLabel = "",
                    onPrimary = { },
                    icon = {
                        Icon(
                            Icons.Outlined.EventAvailable,
                            null,
                            Modifier.size(52.dp),
                            SubtitleGray
                        )
                    },
                    showPrimary = false
                )
            }
            uiState.events.isEmpty() -> {
                HostedEmptyMessage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    title = stringResource(R.string.hosted_events_empty_title),
                    body = stringResource(R.string.hosted_events_empty_body),
                    primaryLabel = stringResource(R.string.hosted_events_create_first),
                    onPrimary = onCreateFirstEvent,
                    icon = {
                        Icon(
                            Icons.Outlined.EventAvailable,
                            null,
                            Modifier.size(52.dp),
                            OrangeAccent
                        )
                    }
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BodyBackground)
                        .padding(innerPadding)
                        .nestedScroll(hostedScrollBehavior.nestedScrollConnection)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.hosted_events_count_header, uiState.events.size),
                            color = SubtitleGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(uiState.events, key = { it.id }) { event ->
                        HostedEventRow(
                            event = event,
                            onOpen = { onEventClick(event) },
                            onEdit = { onEditEvent(event) },
                            onDelete = { pendingDelete = event }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HostedEmptyMessage(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    icon: @Composable () -> Unit,
    showPrimary: Boolean = true
) {
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
            icon()
        }
        Text(
            text = title,
            color = AppBarNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = body,
            color = SubtitleGray,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp)
        )
        if (showPrimary) {
            Button(
                onClick = onPrimary,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
            ) {
                Text(primaryLabel, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HostedEventRow(
    event: Event,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tint = Color(event.category.markerColorHex)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpen)
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EventAvailable,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = event.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtitleGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.hosted_events_edit),
                    tint = OrangeAccent
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.hosted_events_delete),
                    tint = SubtitleGray
                )
            }
        }
    }
}
