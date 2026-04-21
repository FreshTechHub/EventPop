package com.android.example.eventpop.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.example.eventpop.R
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.navigation.EventPopDestinations
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.SubtitleGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onNavDiscover: () -> Unit,
    onNavEvents: () -> Unit,
    onNavProfile: () -> Unit,
    onNavSettings: () -> Unit
) {
    var pushNotifications by remember { mutableStateOf(true) }
    var eventReminders by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val versionName = remember(context) {
        try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBarNavy)
            )
        },
        bottomBar = {
            EventPopBottomBar(
                selectedDiscover = false,
                selectedEvents = false,
                selectedProfile = false,
                selectedSettings = true,
                onNavDiscover = onNavDiscover,
                onNavEvents = onNavEvents,
                onNavProfile = onNavProfile,
                onNavSettings = onNavSettings
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_section_notifications),
                style = MaterialTheme.typography.labelLarge,
                color = SubtitleGray,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_push_notifications),
                checked = pushNotifications,
                onCheckedChange = { pushNotifications = it }
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_event_reminders),
                checked = eventReminders,
                onCheckedChange = { eventReminders = it }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text(
                text = stringResource(R.string.settings_section_about),
                style = MaterialTheme.typography.labelLarge,
                color = SubtitleGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.settings_about_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.settings_version, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = SubtitleGray
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
