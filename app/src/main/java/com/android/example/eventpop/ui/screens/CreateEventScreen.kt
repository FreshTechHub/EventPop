@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.android.example.eventpop.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.android.example.eventpop.R
import com.android.example.eventpop.data.EventLocationData
import com.android.example.eventpop.data.NamedLookupRow
import com.android.example.eventpop.ui.mvc.CreateEventUiState
import com.android.example.eventpop.ui.components.EventPopCenteredTopBar
import com.android.example.eventpop.ui.navigation.EventPopDestinations
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.util.MediaPickPermissions
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import com.android.example.eventpop.ui.viewmodel.CreateEventViewModel
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng

private val BodyBackground = Color(0xFFF4F6F9)
private val FieldShape = RoundedCornerShape(12.dp)
private val ErrorSurface = Color(0xFFFFEBEE)
private val ErrorText = Color(0xFFB71C1C)
private val LocationErrorRed = Color(0xFFE53935)
private val Shadow6 = Color(0x0F000000)
private val KampalaLatLng = LatLng(0.3476, 32.5825)

@Composable
private fun createEventFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedTextColor = Color.Black,
    focusedTextColor = Color.Black,
    disabledTextColor = Color.Black,
    unfocusedLabelColor = Color(0xFF424242),
    focusedLabelColor = OrangeAccent,
    cursorColor = OrangeAccent,
    unfocusedBorderColor = Color(0xFFBDBDBD),
    focusedBorderColor = OrangeAccent,
    errorBorderColor = LocationErrorRed,
    unfocusedPlaceholderColor = SubtitleGray,
    focusedPlaceholderColor = SubtitleGray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    viewModel: CreateEventViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var showLocationSheet by remember { mutableStateOf(false) }
    var sheetInitialPin by remember { mutableStateOf(KampalaLatLng) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val fine = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!fine && !coarse) {
            val act = context as? Activity ?: return@rememberLauncherForActivityResult
            val showRationaleFine = ActivityCompat.shouldShowRequestPermissionRationale(
                act,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val showRationaleCoarse = ActivityCompat.shouldShowRequestPermissionRationale(
                act,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (!showRationaleFine && !showRationaleCoarse) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.location_permission_settings_hint),
                        actionLabel = context.getString(R.string.location_permission_open_settings),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        runCatching { context.startActivity(intent) }
                    }
                }
            }
        }
    }

    LaunchedEffect(showLocationSheet) {
        if (!showLocationSheet) return@LaunchedEffect
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        val fineOk = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseOk = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val gps = if (fineOk || coarseOk) viewModel.getLastKnownLocationOrNull() else null
        val loc = uiState.locationData
        sheetInitialPin = when {
            gps != null -> LatLng(gps.latitude, gps.longitude)
            loc != null -> LatLng(loc.latitude, loc.longitude)
            else -> KampalaLatLng
        }
    }

    LaunchedEffect(uiState.navigateToEventId) {
        val id = uiState.navigateToEventId ?: return@LaunchedEffect
        navController.navigate(EventPopDestinations.eventDetailRoute(id)) {
            popUpTo(EventPopDestinations.EVENTS) { inclusive = false }
        }
        viewModel.consumeNavigateToEventId()
    }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onCoverPicked(uri)
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            pickImage.launch("image/*")
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.media_permission_rationale))
            }
        }
    }

    fun launchCoverImagePicker() {
        val perms = MediaPickPermissions.galleryPermissions()
        val ok = perms.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (ok) pickImage.launch("image/*") else galleryPermissionLauncher.launch(perms)
    }

    val createEventTopBarState = rememberTopAppBarState()
    val createEventScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(createEventTopBarState)

    Scaffold(
        containerColor = BodyBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EventPopCenteredTopBar(
                title = if (uiState.editingEventId != null) {
                    stringResource(R.string.create_event_edit_title)
                } else {
                    stringResource(R.string.create_event_title)
                },
                onBackClick = { navController.popBackStack() },
                backContentDescription = stringResource(R.string.back),
                scrollBehavior = createEventScrollBehavior
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoadingMeta -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.create_event_loading),
                        color = SubtitleGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            uiState.metaError != null -> {
                MetaErrorPanel(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    message = uiState.metaError.orEmpty(),
                    onRetry = { viewModel.refreshMeta() }
                )
            }

            uiState.subscribeGate -> {
                SubscribeGatePanel(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    hostedCount = uiState.hostedCount,
                    onContactUpgrade = {
                        val intent = Intent(
                            Intent.ACTION_SENDTO,
                            Uri.parse("mailto:support@eventpop.app")
                        ).apply {
                            putExtra(Intent.EXTRA_SUBJECT, "EventPop — Host subscription")
                        }
                        runCatching { context.startActivity(intent) }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CreateEventFormContent(
                        modifier = Modifier.fillMaxSize(),
                        scrollBehavior = createEventScrollBehavior,
                        uiState = uiState,
                        onTitleChange = viewModel::setTitle,
                        onOpenLocationPicker = { showLocationSheet = true },
                        onDescriptionChange = viewModel::setDescription,
                        onIsFreeChange = viewModel::setIsFree,
                        onPriceChange = viewModel::setPriceText,
                        onDateChange = viewModel::setDateText,
                        onStartTimeChange = viewModel::setStartTimeText,
                        onEndTimeChange = viewModel::setEndTimeText,
                        onAreaTextChange = viewModel::setAreaText,
                        onCategoryId = viewModel::setSelectedCategoryId,
                        onPickImage = { launchCoverImagePicker() },
                        onRemoveImage = viewModel::clearCover,
                        onPublish = viewModel::publish,
                        onDismissError = viewModel::clearPublishError
                    )
                    if (showLocationSheet) {
                        LocationPickerBottomSheet(
                            viewModel = viewModel,
                            initialPin = sheetInitialPin,
                            onDismiss = { showLocationSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaErrorPanel(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppBarNavy,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppBarNavy)
                ) {
                    Text(stringResource(R.string.create_event_retry))
                }
            }
        }
    }
}

@Composable
private fun SubscribeGatePanel(
    modifier: Modifier = Modifier,
    hostedCount: Int,
    onContactUpgrade: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text(
                    text = stringResource(R.string.create_event_gate_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppBarNavy
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.create_event_gate_body, hostedCount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SubtitleGray,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onContactUpgrade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Text(
                        stringResource(R.string.create_event_gate_cta),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppBarNavy)
                ) {
                    Text(stringResource(R.string.create_event_gate_back))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventFormContent(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    uiState: CreateEventUiState,
    onTitleChange: (String) -> Unit,
    onOpenLocationPicker: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIsFreeChange: (Boolean) -> Unit,
    onPriceChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onAreaTextChange: (String) -> Unit,
    onCategoryId: (String?) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onPublish: () -> Unit,
    onDismissError: () -> Unit
) {
    val fe = uiState.fieldErrors
    var showCategoryPicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val locationBringIntoView = remember { BringIntoViewRequester() }
    val context = LocalContext.current

    LaunchedEffect(fe.location) {
        if (fe.location != null) {
            delay(80)
            locationBringIntoView.bringIntoView()
        }
    }

    val categoryLabel =
        uiState.categories.find { it.id == uiState.selectedCategoryId }?.name.orEmpty()

    fun calendarFromDateText(): Calendar {
        val cal = Calendar.getInstance()
        val t = uiState.dateText.trim()
        if (t.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            val p = t.split("-")
            cal.set(p[0].toInt(), p[1].toInt() - 1, p[2].toInt())
        }
        return cal
    }

    fun openDatePicker() {
        val cal = calendarFromDateText()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateChange(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun parseTimeParts(text: String): Pair<Int, Int> {
        val t = text.trim()
        val m = Regex("(\\d{1,2}):(\\d{2})").find(t)
        return if (m != null) {
            m.groupValues[1].toInt().coerceIn(0, 23) to m.groupValues[2].toInt().coerceIn(0, 59)
        } else {
            18 to 0
        }
    }

    fun openTimePicker(isStart: Boolean) {
        val raw = if (isStart) uiState.startTimeText else uiState.endTimeText
        val (h, min) = parseTimeParts(raw)
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val s = String.format("%02d:%02d", hourOfDay, minute)
                if (isStart) onStartTimeChange(s) else onEndTimeChange(s)
            },
            h,
            min,
            true
        ).show()
    }

    if (showCategoryPicker) {
        SelectionListDialog(
            title = stringResource(R.string.create_event_field_category),
            options = uiState.categories,
            emptyListMessage = null,
            onDismiss = { showCategoryPicker = false },
            onPick = { row ->
                onCategoryId(row.id)
                showCategoryPicker = false
            }
        )
    }

    Column(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.create_event_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = SubtitleGray,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (uiState.publishError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.publishError.orEmpty(),
                        modifier = Modifier.weight(1f),
                        color = ErrorText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onDismissError) {
                        Text(
                            stringResource(R.string.create_event_dismiss_error),
                            color = OrangeAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        SectionCard {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_event_field_title)) },
                isError = fe.title != null,
                supportingText = { fe.title?.let { Text(it) } },
                singleLine = true,
                shape = FieldShape,
                colors = createEventFieldColors()
            )
            Spacer(modifier = Modifier.height(14.dp))
            EventLocationPreviewCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(locationBringIntoView),
                locationData = uiState.locationData,
                onClick = onOpenLocationPicker
            )
            fe.location?.let { msg ->
                Text(
                    text = msg,
                    color = LocationErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_event_field_description)) },
                isError = fe.description != null,
                supportingText = { fe.description?.let { Text(it) } },
                minLines = 4,
                shape = FieldShape,
                colors = createEventFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard {
            OutlinedTextField(
                value = uiState.areaText,
                onValueChange = onAreaTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_event_field_area)) },
                placeholder = { Text(stringResource(R.string.create_event_field_area_hint)) },
                isError = fe.area != null,
                supportingText = { fe.area?.let { Text(it) } },
                singleLine = true,
                shape = FieldShape,
                colors = createEventFieldColors()
            )
            Spacer(modifier = Modifier.height(14.dp))
            PickerOutlineField(
                label = stringResource(R.string.create_event_field_category),
                value = categoryLabel,
                error = fe.category,
                onClick = { showCategoryPicker = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.create_event_free_entry),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppBarNavy
                    )
                    Text(
                        stringResource(R.string.create_event_free_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtitleGray
                    )
                }
                Switch(
                    checked = uiState.isFree,
                    onCheckedChange = onIsFreeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OrangeAccent,
                        uncheckedBorderColor = SubtitleGray
                    )
                )
            }
            if (!uiState.isFree) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = uiState.priceText,
                    onValueChange = onPriceChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.create_event_field_price)) },
                    isError = fe.price != null,
                    supportingText = { fe.price?.let { Text(it) } },
                    singleLine = true,
                    shape = FieldShape,
                    colors = createEventFieldColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard {
            Text(
                stringResource(R.string.create_event_section_schedule),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppBarNavy,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = uiState.dateText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDatePicker() },
                label = { Text(stringResource(R.string.create_event_field_date)) },
                placeholder = { Text(stringResource(R.string.create_event_field_date_tap)) },
                isError = fe.date != null,
                supportingText = {
                    if (fe.date != null) Text(fe.date!!)
                    else Text(stringResource(R.string.create_event_field_date_hint))
                },
                singleLine = true,
                shape = FieldShape,
                trailingIcon = {
                    IconButton(onClick = { openDatePicker() }) {
                        Icon(
                            Icons.Filled.CalendarToday,
                            contentDescription = stringResource(R.string.create_event_field_date_tap),
                            tint = OrangeAccent
                        )
                    }
                },
                colors = createEventFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.startTimeText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { openTimePicker(true) },
                    label = { Text(stringResource(R.string.create_event_field_start)) },
                    placeholder = { Text(stringResource(R.string.create_event_field_time_tap)) },
                    isError = fe.time != null,
                    singleLine = true,
                    shape = FieldShape,
                    trailingIcon = {
                        IconButton(onClick = { openTimePicker(true) }) {
                            Icon(
                                Icons.Filled.AccessTime,
                                contentDescription = stringResource(R.string.create_event_field_time_tap),
                                tint = OrangeAccent
                            )
                        }
                    },
                    colors = createEventFieldColors()
                )
                OutlinedTextField(
                    value = uiState.endTimeText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { openTimePicker(false) },
                    label = { Text(stringResource(R.string.create_event_field_end)) },
                    placeholder = { Text(stringResource(R.string.create_event_field_time_tap)) },
                    singleLine = true,
                    shape = FieldShape,
                    trailingIcon = {
                        IconButton(onClick = { openTimePicker(false) }) {
                            Icon(
                                Icons.Filled.AccessTime,
                                contentDescription = stringResource(R.string.create_event_field_time_tap),
                                tint = OrangeAccent
                            )
                        }
                    },
                    colors = createEventFieldColors()
                )
            }
            if (fe.time != null) {
                Text(
                    fe.time!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard {
            Text(
                stringResource(R.string.create_event_section_cover),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppBarNavy,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPickImage,
                    enabled = !uiState.isUploadingCover,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppBarNavy)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                        Text(stringResource(R.string.create_event_pick_image))
                    }
                }
                if (uiState.isUploadingCover) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(28.dp),
                        color = OrangeAccent,
                        strokeWidth = 2.dp
                    )
                }
            }
            uiState.coverImageLabel?.let { label ->
                Text(
                    text = stringResource(R.string.create_event_cover_selected, label),
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtitleGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
                TextButton(onClick = onRemoveImage) {
                    Text(stringResource(R.string.create_event_remove_image), color = OrangeAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onPublish,
            enabled = !uiState.isPublishing && !uiState.isUploadingCover,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
        ) {
            if (uiState.isPublishing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (uiState.editingEventId != null) {
                        stringResource(R.string.create_event_save)
                    } else {
                        stringResource(R.string.create_event_publish)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EventLocationPreviewCard(
    modifier: Modifier = Modifier,
    locationData: EventLocationData?,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.create_event_field_location),
            style = MaterialTheme.typography.bodySmall,
            color = SubtitleGray,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = FieldShape,
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
            colors = CardDefaults.outlinedCardColors(containerColor = CardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    if (locationData == null) {
                        Text(
                            stringResource(R.string.create_event_location_preview_hint),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppBarNavy
                        )
                        Text(
                            stringResource(R.string.create_event_location_preview_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtitleGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            locationData.placeName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppBarNavy,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            locationData.displayAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtitleGray,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = SubtitleGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
private fun PickerOutlineField(
    label: String,
    value: String,
    error: String?,
    onClick: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SubtitleGray
                )
            },
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            shape = FieldShape,
            colors = createEventFieldColors()
        )
    }
}

@Composable
private fun SelectionListDialog(
    title: String,
    options: List<NamedLookupRow>,
    emptyListMessage: String? = null,
    onDismiss: () -> Unit,
    onPick: (NamedLookupRow) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CardBackground,
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppBarNavy,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp)
                )
                HorizontalDivider(color = Color(0xFFE8E8E8))
                if (options.isEmpty()) {
                    Text(
                        text = emptyListMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtitleGray,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 360.dp)
                            .padding(horizontal = 22.dp, vertical = 20.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(options, key = { it.id }) { row ->
                            Text(
                                text = row.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppBarNavy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPick(row)
                                    }
                                    .padding(horizontal = 22.dp, vertical = 16.dp)
                            )
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                        }
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(8.dp)
                ) {
                    Text(
                        stringResource(R.string.create_event_dialog_cancel),
                        color = OrangeAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
