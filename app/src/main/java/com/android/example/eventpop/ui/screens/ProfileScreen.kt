package com.android.example.eventpop.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.example.eventpop.R
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.navigation.EventPopDestinations
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onNavDiscover: () -> Unit,
    onNavEvents: () -> Unit,
    onNavProfile: () -> Unit,
    onNavSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
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
                selectedProfile = true,
                selectedSettings = false,
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.profile_avatar_initial),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profile_display_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.profile_email),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtitleGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    value = stringResource(R.string.profile_stat_events_value),
                    label = stringResource(R.string.profile_stat_events)
                )
                ProfileStat(
                    value = stringResource(R.string.profile_stat_saved_value),
                    label = stringResource(R.string.profile_stat_saved)
                )
                ProfileStat(
                    value = stringResource(R.string.profile_stat_rsvps_value),
                    label = stringResource(R.string.profile_stat_rsvps)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.profile_section_upcoming),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_upcoming_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtitleGray,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = OrangeAccent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SubtitleGray
        )
    }
}
