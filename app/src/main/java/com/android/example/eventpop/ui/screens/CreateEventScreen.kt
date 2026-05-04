package com.android.example.eventpop.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.android.example.eventpop.R
import com.android.example.eventpop.data.NamedLookupRow
import com.android.example.eventpop.ui.mvc.CreateEventUiState
import com.android.example.eventpop.ui.navigation.EventPopDestinations
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import com.android.example.eventpop.ui.viewmodel.CreateEventViewModel

private val BodyBackground = Color(0xFFF4F6F9)
private val FieldShape = RoundedCornerShape(12.dp)
private val ErrorSurface = Color(0xFFFFEBEE)
private val ErrorText = Color(0xFFB71C1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    viewModel: CreateEventViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.navigateToEventId) {
        val id = uiState.navigateToEventId ?: return@LaunchedEffect
        navController.navigate(EventPopDestinations.eventDetailRoute(id)) {
            popUpTo(EventPopDestinations.CREATE_EVENT) { inclusive = true }
        }
        viewModel.consumeNavigateToEventId()
    }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onCoverPicked(uri)
    }

    Scaffold(
        containerColor = BodyBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.create_event_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBarNavy)
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
                CreateEventFormContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    uiState = uiState,
                    onTitleChange = viewModel::setTitle,
                    onLocationChange = viewModel::setLocation,
                    onDescriptionChange = viewModel::setDescription,
                    onIsFreeChange = viewModel::setIsFree,
                    onPriceChange = viewModel::setPriceText,
                    onDateChange = viewModel::setDateText,
                    onStartTimeChange = viewModel::setStartTimeText,
                    onEndTimeChange = viewModel::setEndTimeText,
                    onLatChange = viewModel::setLatitudeText,
                    onLngChange = viewModel::setLongitudeText,
                    onAreaId = viewModel::setSelectedAreaId,
                    onCategoryId = viewModel::setSelectedCategoryId,
                    onPickImage = { pickImage.launch("image/*") },
                    onRemoveImage = viewModel::clearCover,
                    onPublish = viewModel::publish,
                    onDismissError = viewModel::clearPublishError
                )
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

@Composable
private fun CreateEventFormContent(
    modifier: Modifier = Modifier,
    uiState: CreateEventUiState,
    onTitleChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIsFreeChange: (Boolean) -> Unit,
    onPriceChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onLatChange: (String) -> Unit,
    onLngChange: (String) -> Unit,
    onAreaId: (String?) -> Unit,
    onCategoryId: (String?) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onPublish: () -> Unit,
    onDismissError: () -> Unit
) {
    val fe = uiState.fieldErrors
    var showAreaPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val areaLabel = uiState.areas.find { it.id == uiState.selectedAreaId }?.name.orEmpty()
    val categoryLabel = uiState.categories.find { it.id == uiState.selectedCategoryId }?.name.orEmpty()

    if (showAreaPicker) {
        SelectionListDialog(
            title = stringResource(R.string.create_event_field_area),
            options = uiState.areas,
            onDismiss = { showAreaPicker = false },
            onPick = { row ->
                onAreaId(row.id)
                showAreaPicker = false
            }
        )
    }
    if (showCategoryPicker) {
        SelectionListDialog(
            title = stringResource(R.string.create_event_field_category),
            options = uiState.categories,
            onDismiss = { showCategoryPicker = false },
            onPick = { row ->
                onCategoryId(row.id)
                showCategoryPicker = false
            }
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    focusedLabelColor = OrangeAccent,
                    cursorColor = OrangeAccent
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = uiState.location,
                onValueChange = onLocationChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_event_field_location)) },
                isError = fe.location != null,
                supportingText = { fe.location?.let { Text(it) } },
                singleLine = true,
                shape = FieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    focusedLabelColor = OrangeAccent,
                    cursorColor = OrangeAccent
                )
            )
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    focusedLabelColor = OrangeAccent,
                    cursorColor = OrangeAccent
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard {
            PickerOutlineField(
                label = stringResource(R.string.create_event_field_area),
                value = areaLabel,
                error = fe.area,
                onClick = { showAreaPicker = true }
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        focusedLabelColor = OrangeAccent,
                        cursorColor = OrangeAccent
                    )
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
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_event_field_date)) },
                placeholder = { Text("2026-06-15") },
                isError = fe.date != null,
                supportingText = {
                    if (fe.date != null) Text(fe.date!!)
                    else Text(stringResource(R.string.create_event_field_date_hint))
                },
                singleLine = true,
                shape = FieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    focusedLabelColor = OrangeAccent,
                    cursorColor = OrangeAccent
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.startTimeText,
                    onValueChange = onStartTimeChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.create_event_field_start)) },
                    isError = fe.time != null,
                    singleLine = true,
                    shape = FieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        focusedLabelColor = OrangeAccent,
                        cursorColor = OrangeAccent
                    )
                )
                OutlinedTextField(
                    value = uiState.endTimeText,
                    onValueChange = onEndTimeChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.create_event_field_end)) },
                    singleLine = true,
                    shape = FieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        focusedLabelColor = OrangeAccent,
                        cursorColor = OrangeAccent
                    )
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
                stringResource(R.string.create_event_section_map),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppBarNavy,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.create_event_map_hint),
                style = MaterialTheme.typography.bodySmall,
                color = SubtitleGray,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.latitudeText,
                    onValueChange = onLatChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.create_event_field_lat)) },
                    singleLine = true,
                    shape = FieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        focusedLabelColor = OrangeAccent,
                        cursorColor = OrangeAccent
                    )
                )
                OutlinedTextField(
                    value = uiState.longitudeText,
                    onValueChange = onLngChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.create_event_field_lng)) },
                    singleLine = true,
                    shape = FieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        focusedLabelColor = OrangeAccent,
                        cursorColor = OrangeAccent
                    )
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
                    stringResource(R.string.create_event_publish),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeAccent,
                focusedLabelColor = OrangeAccent,
                cursorColor = OrangeAccent
            )
        )
    }
}

@Composable
private fun SelectionListDialog(
    title: String,
    options: List<NamedLookupRow>,
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
