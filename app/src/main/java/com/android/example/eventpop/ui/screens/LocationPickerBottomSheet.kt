@file:Suppress("DEPRECATION")

package com.android.example.eventpop.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.example.eventpop.R
import com.android.example.eventpop.data.EventLocationData
import com.android.example.eventpop.data.NominatimResult
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import com.android.example.eventpop.ui.viewmodel.CreateEventViewModel
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
private val SheetBg = Color(0xFFF4F6F9)
private val DividerF0 = Color(0xFFF0F0F0)
private val ErrorRed = Color(0xFFE53935)
private val Shadow6 = Color(0x0F000000)
private val Shadow10 = Color(0x1A000000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerBottomSheet(
    viewModel: CreateEventViewModel,
    initialPin: LatLng,
    onDismiss: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pin by remember(initialPin) { mutableStateOf(initialPin) }
    var pendingCamera by remember { mutableStateOf<LatLng?>(null) }
    var pendingCameraDurationMs by remember { mutableStateOf(600) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQ by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val candidate by viewModel.pickerCandidate.collectAsStateWithLifecycle()

    LaunchedEffect(initialPin) {
        pin = initialPin
        viewModel.onPickerPinMoved(initialPin.latitude, initialPin.longitude)
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onLocationPickerDismissed()
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = SheetBg,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val sheetH = maxHeight * 0.85f
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetH)
            ) {
                LocationSearchBar(
                    query = searchQ,
                    onQueryChange = viewModel::updateLocationSearchQuery,
                    results = results,
                    onPickResult = { r ->
                        val ll = LatLng(r.lat, r.lon)
                        pin = ll
                        pendingCamera = ll
                        pendingCameraDurationMs = 600
                        viewModel.updateLocationSearchQuery("")
                        viewModel.onPickerPinMoved(r.lat, r.lon)
                    }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LocationPickerMap(
                        initialPin = initialPin,
                        pin = pin,
                        pendingCamera = pendingCamera,
                        pendingCameraDurationMs = pendingCameraDurationMs,
                        onPendingCameraConsumed = { pendingCamera = null },
                        onPinMoved = { ll ->
                            pin = ll
                            viewModel.onPickerPinMoved(ll.latitude, ll.longitude)
                        },
                        lifecycleOwner = lifecycleOwner
                    )
                    MyLocationFab(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        onClick = {
                            val loc = viewModel.getLastKnownLocationOrNull()
                            if (loc != null) {
                                val ll = LatLng(loc.latitude, loc.longitude)
                                pin = ll
                                pendingCamera = ll
                                pendingCameraDurationMs = 800
                                viewModel.onPickerPinMoved(loc.latitude, loc.longitude)
                            } else {
                                val ll = LatLng(0.3476, 32.5825)
                                pin = ll
                                pendingCamera = ll
                                pendingCameraDurationMs = 800
                                viewModel.onPickerPinMoved(ll.latitude, ll.longitude)
                            }
                        }
                    )
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.locationLoading,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 80.dp),
                        enter = slideInVertically { -it / 2 } + fadeIn()
                    ) {
                        Row(
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Shadow6, ambientColor = Shadow6)
                                .clip(RoundedCornerShape(20.dp))
                                .background(CardBackground)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = OrangeAccent,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.location_picker_finding_address),
                                color = AppBarNavy,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                LocationPickerFooter(
                    loading = uiState.locationLoading,
                    error = uiState.locationError,
                    candidate = candidate,
                    onConfirm = {
                        viewModel.confirmPickerLocation()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun LocationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<NominatimResult>,
    onPickResult: (NominatimResult) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Shadow6, ambientColor = Shadow6)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = SubtitleGray,
                    modifier = Modifier.size(18.dp)
                )
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textStyle = TextStyle(
                        color = AppBarNavy,
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(OrangeAccent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    stringResource(R.string.location_picker_search_hint),
                                    color = SubtitleGray,
                                    fontSize = 14.sp
                                )
                            }
                            inner()
                        }
                    }
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = null,
                            tint = SubtitleGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            if (query.length >= 2 && results.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Shadow6, ambientColor = Shadow6)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(results, key = { "${it.lat},${it.lon},${it.displayName}" }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clickable { onPickResult(item) }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = OrangeAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = item.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppBarNavy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.displayName,
                                        fontSize = 11.sp,
                                        color = SubtitleGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            HorizontalDivider(color = DividerF0, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun LocationPickerMap(
    initialPin: LatLng,
    pin: LatLng,
    pendingCamera: LatLng?,
    pendingCameraDurationMs: Int,
    onPendingCameraConsumed: () -> Unit,
    onPinMoved: (LatLng) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val context = LocalContext.current
    val mapRef = remember { AtomicReference<MapLibreMap?>(null) }
    val markerRef = remember { AtomicReference<Marker?>(null) }
    var mapViewHolder by remember { mutableStateOf<MapView?>(null) }
    val onPinMovedState = rememberUpdatedState(onPinMoved)
    val pendingCamState = rememberUpdatedState(pendingCamera)
    val pendingDurState = rememberUpdatedState(pendingCameraDurationMs)
    val pinState = rememberUpdatedState(pin)

    AndroidView(
        factory = { ctx ->
            MapLibre.getInstance(ctx)
            val mapView = MapView(ctx)
            mapViewHolder = mapView
            mapView.onCreate(null)
            mapView.getMapAsync { map ->
                mapRef.set(map)
                map.setStyle(Style.Builder().fromUri(STYLE_URI)) {
                    val icon = MapMarkerBitmaps.iconOrangePicker(ctx)
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(initialPin)
                            .icon(icon)
                    )
                    markerRef.set(marker)
                    map.addOnMapClickListener { latLng ->
                        marker.position = latLng
                        onPinMovedState.value(latLng)
                        true
                    }
                    map.cameraPosition = CameraPosition.Builder()
                        .target(initialPin)
                        .zoom(15.0)
                        .build()
                }
            }
            mapView
        },
        update = {
            val map = mapRef.get()
            val marker = markerRef.get()
            if (map != null && marker != null) {
                val target = pendingCamState.value
                val p = pinState.value
                if (target != null) {
                    marker.position = target
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(target, 15.0),
                        pendingDurState.value
                    )
                    onPendingCameraConsumed()
                } else {
                    val mpos = marker.position
                    if (kotlin.math.abs(mpos.latitude - p.latitude) > 1e-7 ||
                        kotlin.math.abs(mpos.longitude - p.longitude) > 1e-7
                    ) {
                        marker.position = p
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    LaunchedEffect(mapViewHolder, lifecycleOwner) {
        var mv = mapViewHolder
        var spins = 0
        while (mv == null && spins < 80) {
            delay(16)
            mv = mapViewHolder
            spins++
        }
        if (mv == null) return@LaunchedEffect
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mv.onStart()
                Lifecycle.Event.ON_RESUME -> mv.onResume()
                Lifecycle.Event.ON_PAUSE -> mv.onPause()
                Lifecycle.Event.ON_STOP -> mv.onStop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        try {
            awaitCancellation()
        } finally {
            lifecycleOwner.lifecycle.removeObserver(obs)
            mv.onPause()
            mv.onStop()
            mv.onDestroy()
        }
    }
}

@Composable
private fun MyLocationFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(6.dp, CircleShape, spotColor = Shadow10, ambientColor = Shadow10)
            .clip(CircleShape)
            .background(CardBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MyLocation,
            contentDescription = null,
            tint = OrangeAccent,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun LocationPickerFooter(
    loading: Boolean,
    error: String?,
    candidate: EventLocationData?,
    onConfirm: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(),
        label = "confirmLoc"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground)
    ) {
        HorizontalDivider(thickness = 1.dp, color = DividerF0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            when {
                loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OrangeAccent,
                            strokeWidth = 2.dp
                        )
                        Text(
                            stringResource(R.string.location_picker_finding_address_short),
                            color = SubtitleGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
                candidate != null -> {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = candidate.placeName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppBarNavy
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = candidate.displayAddress,
                            fontSize = 12.sp,
                            color = SubtitleGray
                        )
                        if (error != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = error,
                                color = ErrorRed,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .width(140.dp)
                            .height(44.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        interactionSource = interaction
                    ) {
                        Text(
                            stringResource(R.string.location_picker_confirm),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                error != null -> {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        stringResource(R.string.location_picker_geocode_error),
                        color = ErrorRed,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
                else -> {
                    Text(
                        stringResource(R.string.location_picker_move_map_hint),
                        color = SubtitleGray,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
