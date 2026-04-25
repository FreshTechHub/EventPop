package com.android.example.eventpop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.SubtitleGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavEvents: () -> Unit,
    onNavMap: () -> Unit,
    onNavDiscover: () -> Unit,
    onNavFavorites: () -> Unit,
    onNavProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favorites",
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
                selectedDiscover = false,
                selectedFavorites = true,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No favorite events yet.",
                color = SubtitleGray,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Events you favorite will appear here.",
                color = SubtitleGray.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
