@file:Suppress("DEPRECATION")

package com.android.example.eventpop.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.example.eventpop.data.Event
import com.android.example.eventpop.data.EventCategory
import com.android.example.eventpop.ui.mvc.MapUiState
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.FreeGreen
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.util.concurrent.atomic.AtomicReference

private const val STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"
private val Kampala = LatLng(0.3476, 32.5825)
private const val ZOOM = 12.0

private val DividerLight = Color(0xFFE0E0E0)
private val Shadow8 = Color(0x14000000)
private val Shadow10 = Color(0x1A000000)
private val Shadow12 = Color(0x1F000000)
private val Shadow14 = Color(0x24000000)
private val Shadow15 = Color(0x26000000)
private val PreviewBg = Color(0xFFF4F6F9)

private class MapListenerBridge {
    var onMapTap: () -> Unit = {}
    var onMarkerId: (String) -> Unit = {}
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapScreen(
    uiState: MapUiState,
    onNavEvents: () -> Unit,
    onNavMap: () -> Unit,
    onNavDiscover: () -> Unit,
    onNavFavorites: () -> Unit,
    onNavProfile: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val inspection = LocalInspectionMode.current
    var selectedCategory by remember { mutableStateOf<EventCategory?>(null) }
    var legendExpanded by remember { mutableStateOf(false) }
    var peekEvent by remember { mutableStateOf<Event?>(null) }
    val listenerBridge = remember { MapListenerBridge() }
    val pins by rememberUpdatedState(uiState.eventPins)
    val filteredPins = remember(pins, selectedCategory) {
        if (selectedCategory == null) pins
        else pins.filter { it.latitude != null && it.longitude != null && it.category == selectedCategory }
    }
    val pinKey = uiState.eventPins.joinToString { "${it.id}:${it.latitude}:${it.longitude}" }
    val mapRef = remember { AtomicReference<MapLibreMap?>(null) }
    val density = LocalDensity.current

    SideEffect {
        listenerBridge.onMapTap = { peekEvent = null }
        listenerBridge.onMarkerId = { id ->
            peekEvent = pins.find { it.id == id }
        }
    }

    DisposableEffect(inspection) {
        if (inspection) {
            onDispose { }
        } else {
            val window = (context as? Activity)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            val oldLight = controller?.isAppearanceLightStatusBars
            window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
            controller?.isAppearanceLightStatusBars = false
            onDispose {
                window?.let { WindowCompat.setDecorFitsSystemWindows(it, true) }
                controller?.isAppearanceLightStatusBars = oldLight ?: true
            }
        }
    }

    fun refreshMarkers(map: MapLibreMap, ctx: android.content.Context) {
        if (map.style == null) return
        for (m in map.markers.toList()) {
            map.removeMarker(m)
        }
        for (event in filteredPins) {
            val lat = event.latitude ?: continue
            val lon = event.longitude ?: continue
            try {
                val icon = MapMarkerBitmaps.iconForCategory(ctx, event.category)
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lon))
                        .title(event.title)
                        .snippet(event.id)
                        .icon(icon)
                )
            } catch (_: Throwable) {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lon))
                        .title(event.title)
                        .snippet(event.id)
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (LocalInspectionMode.current) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PreviewBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = OrangeAccent
                    )
                    Text(
                        text = "Map Preview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppBarNavy,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Kampala, Uganda",
                        fontSize = 13.sp,
                        color = SubtitleGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            key(pinKey) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapLibre.getInstance(ctx)
                        MapView(ctx).apply {
                            getMapAsync { map ->
                                mapRef.set(map)
                                map.setStyle(Style.Builder().fromUri(STYLE_URI)) {
                                    map.cameraPosition = CameraPosition.Builder()
                                        .target(Kampala)
                                        .zoom(ZOOM)
                                        .build()
                                    map.addOnMapClickListener { _ ->
                                        listenerBridge.onMapTap()
                                        false
                                    }
                                    map.setOnMarkerClickListener { marker: Marker ->
                                        marker.snippet?.let { listenerBridge.onMarkerId(it) }
                                        true
                                    }
                                    refreshMarkers(map, ctx)
                                }
                            }
                        }
                    },
                    update = { mapView ->
                        val map = mapRef.get()
                        if (map != null && map.style != null) {
                            refreshMarkers(map, mapView.context)
                        }
                    }
                )
            }
        }

        if (!LocalInspectionMode.current) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(26.dp), ambientColor = Shadow12, spotColor = Shadow12)
                    .clip(RoundedCornerShape(26.dp))
                    .background(CardBackground),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = SubtitleGray
                    )
                    Text(
                        text = "Search events in Kampala…",
                        color = SubtitleGray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp)
                            .background(DividerLight)
                    )
                    Box(modifier = Modifier.padding(start = 12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(OrangeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        }
                        if (selectedCategory != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(OrangeAccent)
                            )
                        }
                    }
                }
            }

            val chipCategories = remember { listOf<EventCategory?>(null) + EventCategory.entries }
            LazyRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 116.dp, start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(chipCategories, key = { it?.name ?: "ALL" }) { cat ->
                    val selected = if (cat == null) selectedCategory == null else selectedCategory == cat
                    val label = if (cat == null) "All" else cat.displayName
                    val bg by animateColorAsState(
                        targetValue = when {
                            cat == null && selected -> AppBarNavy
                            cat != null && selected -> Color(cat.markerColorHex.toInt())
                            else -> CardBackground
                        },
                        animationSpec = tween(200),
                        label = "chipBg"
                    )
                    val fg by animateColorAsState(
                        targetValue = if (selected) Color.White else AppBarNavy,
                        animationSpec = tween(200),
                        label = "chipFg"
                    )
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedCategory = if (cat == null) null else if (selected) null else cat
                        },
                        label = { Text(label, fontSize = 14.sp) },
                        leadingIcon = if (cat != null) {
                            {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(cat.markerColorHex.toInt()))
                                )
                            }
                        } else null,
                        modifier = Modifier
                            .height(34.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Shadow8, spotColor = Shadow8),
                        shape = RoundedCornerShape(20.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = DividerLight,
                            selectedBorderColor = Color.Transparent,
                            disabledBorderColor = DividerLight
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = bg,
                            labelColor = fg,
                            selectedContainerColor = bg,
                            selectedLabelColor = fg
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 116.dp, end = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Shadow15, spotColor = Shadow15)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppBarNavy)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(OrangeAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${filteredPins.count { it.latitude != null && it.longitude != null }} Events",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val peekEnter = slideInVertically(
                animationSpec = tween(280, easing = EaseOutQuart)
            ) { with(density) { 120.dp.roundToPx() } } + fadeIn(tween(280, easing = EaseOutQuart))
            val peekExit = slideOutVertically(tween(200)) { with(density) { 120.dp.roundToPx() } } + fadeOut(tween(200))

            AnimatedVisibility(
                visible = peekEvent != null,
                enter = peekEnter,
                exit = peekExit,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp, start = 16.dp, end = 16.dp)
            ) {
                val ev = peekEvent ?: return@AnimatedVisibility
                PeekEventSheet(event = ev)
            }

            val myLocInteraction = remember { MutableInteractionSource() }
            val myLocPressed by myLocInteraction.collectIsPressedAsState()
            val myLocScale by animateFloatAsState(
                targetValue = if (myLocPressed) 0.9f else 1f,
                animationSpec = spring(stiffness = 400f),
                label = "myLocScale"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 148.dp, end = 16.dp)
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = myLocScale
                        scaleY = myLocScale
                        transformOrigin = TransformOrigin.Center
                    }
                    .shadow(6.dp, CircleShape, ambientColor = Shadow15, spotColor = Shadow15)
                    .clip(CircleShape)
                    .background(OrangeAccent)
                    .clickable(
                        interactionSource = myLocInteraction,
                        indication = null
                    ) {
                        mapRef.get()?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(Kampala, ZOOM),
                            800
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
            }

            val legendInteraction = remember { MutableInteractionSource() }
            val legendPressed by legendInteraction.collectIsPressedAsState()
            val legendScale by animateFloatAsState(
                targetValue = if (legendPressed) 0.92f else 1f,
                animationSpec = spring(stiffness = 400f),
                label = "legendFab"
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 88.dp, end = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Column(
                    modifier = Modifier
                        .width(168.dp)
                        .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing))
                ) {
                    if (legendExpanded) {
                        Column(
                            modifier = Modifier
                                .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Shadow10, spotColor = Shadow10)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardBackground)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CATEGORIES",
                                    color = AppBarNavy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { legendExpanded = false },
                                    tint = SubtitleGray
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            EventCategory.entries.forEachIndexed { index, cat ->
                                val chipSelected = selectedCategory == cat
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clickable {
                                            selectedCategory = if (chipSelected) null else cat
                                        }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(cat.markerColorHex.toInt()))
                                    )
                                    Text(
                                        text = cat.displayName,
                                        color = AppBarNavy,
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .weight(1f)
                                    )
                                    if (chipSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = OrangeAccent
                                        )
                                    }
                                }
                                if (index < EventCategory.entries.lastIndex) {
                                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = legendScale
                            scaleY = legendScale
                            transformOrigin = TransformOrigin.Center
                        }
                        .shadow(6.dp, CircleShape, ambientColor = Shadow10, spotColor = Shadow10)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .clickable(
                            interactionSource = legendInteraction,
                            indication = null
                        ) { legendExpanded = !legendExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = AppBarNavy
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .shadow(8.dp, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp), ambientColor = Shadow15, spotColor = Shadow15)
                .background(AppBarNavy.copy(alpha = 0.95f))
        ) {
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
    }
}

@Composable
private fun PeekEventSheet(event: Event) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = Shadow14, spotColor = Shadow14)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OrangeAccent)
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
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = event.title,
                color = AppBarNavy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = event.location,
                color = SubtitleGray,
                fontSize = 12.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (event.isFree) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(FreeGreen)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("FREE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(OrangeAccent)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.priceInfo,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Button(
                onClick = { },
                modifier = Modifier
                    .width(64.dp)
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("RSVP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = { },
                modifier = Modifier.padding(top = 4.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("View", color = OrangeAccent, fontSize = 11.sp)
            }
        }
    }
}
