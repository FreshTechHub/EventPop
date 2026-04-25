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
import com.android.example.eventpop.data.EventCategory
import com.android.example.eventpop.data.SampleEvents
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// Static pins for events - in a real app these come from the Event data model lat/lng fields
private data class EventPin(
    val title: String,
    val location: String,
    val latLng: LatLng,
    val category: EventCategory
)

private val eventPins = listOf(
    EventPin("Street Food Fest", "Bugolobi", LatLng(0.3210, 32.5990), EventCategory.FOOD),
    EventPin("Zumba in the Park", "Kyadondo", LatLng(0.3350, 32.5720), EventCategory.WELLNESS),
    EventPin("DJ Party", "Ntinda", LatLng(0.3580, 32.6050), EventCategory.MUSIC),
    EventPin("Art Exhibition", "Kololo", LatLng(0.3400, 32.5840), EventCategory.ART),
    EventPin("Comedy Night", "Wandegeya", LatLng(0.3290, 32.5730), EventCategory.COMEDY),
    EventPin("Rooftop Jazz", "Nakasero", LatLng(0.3180, 32.5820), EventCategory.MUSIC),
    EventPin("Sunday Brunch", "Muyenga", LatLng(0.3080, 32.5900), EventCategory.FOOD),
    EventPin("Wellness Retreat", "Lubowa", LatLng(0.2950, 32.5480), EventCategory.WELLNESS)
)

private fun EventCategory.markerHue(): Float = when (this) {
    EventCategory.MUSIC -> BitmapDescriptorFactory.HUE_CYAN
    EventCategory.FOOD -> BitmapDescriptorFactory.HUE_ORANGE
    EventCategory.COMEDY -> BitmapDescriptorFactory.HUE_VIOLET
    EventCategory.WELLNESS -> BitmapDescriptorFactory.HUE_GREEN
    EventCategory.ART -> BitmapDescriptorFactory.HUE_RED
    EventCategory.VENUE -> BitmapDescriptorFactory.HUE_YELLOW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavEvents: () -> Unit,
    onNavMap: () -> Unit,
    onNavDiscover: () -> Unit,
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
                selectedProfile = false,
                onNavEvents = onNavEvents,
                onNavMap = onNavMap,
                onNavDiscover = onNavDiscover,
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
                val kampalaLatLng = LatLng(0.3350, 32.5900)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(kampalaLatLng, 12f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = remember { MapProperties(isMyLocationEnabled = false) }
                ) {
                    eventPins.forEach { pin ->
                        Marker(
                            state = MarkerState(position = pin.latLng),
                            title = pin.title,
                            snippet = pin.location,
                            icon = BitmapDescriptorFactory.defaultMarker(pin.category.markerHue())
                        )
                    }
                }
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
