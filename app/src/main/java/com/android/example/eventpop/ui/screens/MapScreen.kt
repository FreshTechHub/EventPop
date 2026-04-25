package com.android.example.eventpop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.example.eventpop.data.EventCategory
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.theme.AppBarNavy
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions

// Static pins for events - in a real app these come from the Event data model lat/lng fields
private data class EventPin(
    val title: String,
    val location: String,
    val latLng: LatLng,
    val category: EventCategory
)

private val eventPins = emptyList<EventPin>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
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
                        text = "Event Map",
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
                selectedMap = true,
                selectedDiscover = false,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (LocalInspectionMode.current) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1B2A4A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Map Preview", color = Color.White)
                }
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        MapLibre.getInstance(context)
                        MapView(context).apply {
                            getMapAsync { map ->
                                map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
                                    // Map is ready
                                }
                                
                                val kampalaLatLng = LatLng(0.3350, 32.5900)
                                val position = CameraPosition.Builder()
                                    .target(kampalaLatLng)
                                    .zoom(12.0)
                                    .build()
                                map.cameraPosition = position
                                
                                eventPins.forEach { pin ->
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(pin.latLng)
                                            .title(pin.title)
                                            .snippet(pin.location)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // Legend overlay
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppBarNavy.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Legend",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    LegendRow(color = Color(0xFF0D9488), label = "Music")
                    LegendRow(color = Color(0xFFEA580C), label = "Food")
                    LegendRow(color = Color(0xFF7C3AED), label = "Comedy")
                    LegendRow(color = Color(0xFF059669), label = "Wellness")
                    LegendRow(color = Color(0xFFDC2626), label = "Art")
                }
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
    }
}
