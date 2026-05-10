package com.android.example.eventpop.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.android.example.eventpop.R
import com.android.example.eventpop.data.ProfileRepository
import com.android.example.eventpop.ui.util.MediaPickPermissions
import com.android.example.eventpop.ui.viewmodel.ProfileViewModel
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File
import com.android.example.eventpop.ui.mvc.ProfileUiState
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray

private val BodyBackground = Color(0xFFF4F6F9)
private val DividerRow = Color(0xFFF0F0F0)
private val DividerVertical = Color(0xFFE0E0E0)
private val ChevronMuted = Color(0xFFC4C4C4)
private val Shadow6 = Color(0x0F000000)
private val ErrorRed = Color(0xFFE53935)
private val SuccessSnackbarGreen = Color(0xFF059669)

private val ugandaCities = listOf(
    "Kampala", "Entebbe", "Jinja", "Mbarara", "Gulu",
    "Mbale", "Lira", "Masaka", "Fort Portal", "Arua"
)

private val DialogShape = RoundedCornerShape(20.dp)
private val DialogButtonShape = RoundedCornerShape(10.dp)

@Composable
private fun ProfileStatusBarSideEffect() {
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val oldColor = window?.statusBarColor
        val oldLight = controller?.isAppearanceLightStatusBars
        if (window != null && controller != null) {
            controller.isAppearanceLightStatusBars = false
            window.statusBarColor = AppBarNavy.toArgb()
        }
        onDispose {
            if (window != null && oldColor != null) {
                window.statusBarColor = oldColor
            }
            if (controller != null) {
                controller.isAppearanceLightStatusBars = oldLight ?: true
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    profileViewModel: ProfileViewModel,
    rsvpCount: Int,
    modifier: Modifier = Modifier,
    onCreateEvent: () -> Unit,
    onLogoutConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ProfileStatusBarSideEffect()

    var city by remember { mutableStateOf("Kampala") }
    var notificationsEnabled by remember { mutableStateOf(true) }

    var showCityDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }

    var showAvatarSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showRemoveAvatarConfirm by remember { mutableStateOf(false) }

    var pendingPermissionAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val cropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val data = res.data ?: return@rememberLauncherForActivityResult
            val out = UCrop.getOutput(data) ?: return@rememberLauncherForActivityResult
            profileViewModel.onAvatarSelected(out)
        }
    }

    fun launchCrop(source: Uri) {
        val destFile = File(context.cacheDir, "avatar_cropped.jpg")
        val destUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destFile
        )
        val opts = UCrop.Options().apply {
            setCircleDimmedLayer(true)
            setShowCropGrid(false)
            setToolbarColor(AppBarNavy.toArgb())
            setToolbarWidgetColor(android.graphics.Color.WHITE)
            setActiveControlsWidgetColor(OrangeAccent.toArgb())
            setStatusBarColor(AppBarNavy.toArgb())
        }
        val intent = UCrop.of(source, destUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .withOptions(opts)
            .getIntent(context)
        cropLauncher.launch(intent)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { launchCrop(it) }
    }

    val captureFile = remember(context) { ProfileRepository.tempCaptureFile(context) }
    val captureUri = remember(context) {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            captureFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) launchCrop(captureUri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            pendingPermissionAction?.invoke()
        }
        pendingPermissionAction = null
    }

    fun ensurePermissionsThen(permissions: Array<String>, action: () -> Unit) {
        val ok = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (ok) action()
        else {
            pendingPermissionAction = action
            permissionLauncher.launch(permissions)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = msg,
            duration = SnackbarDuration.Short
        )
        profileViewModel.dismissMessages()
    }

    LaunchedEffect(uiState.errorMessage) {
        val msg = uiState.errorMessage ?: return@LaunchedEffect
        val retryable = uiState.snackbarRetryable
        val result = snackbarHostState.showSnackbar(
            message = msg,
            actionLabel = if (retryable) context.getString(R.string.profile_snackbar_retry) else null,
            duration = SnackbarDuration.Long,
            withDismissAction = true
        )
        if (result == SnackbarResult.ActionPerformed && retryable) {
            profileViewModel.retrySnackbarAction()
        }
        profileViewModel.dismissMessages()
    }

    val hasAvatar = uiState.avatarUrl.isNotBlank() ||
        (uiState.avatarLocalPath.isNotBlank() && File(uiState.avatarLocalPath).exists())

    AvatarOptionsSheet(
        visible = showAvatarSheet,
        hasAvatar = hasAvatar,
        onDismiss = { showAvatarSheet = false },
        onTakePhoto = {
            ensurePermissionsThen(MediaPickPermissions.cameraPermission()) {
                scope.launch {
                    runCatching {
                        captureFile.parentFile?.mkdirs()
                        captureFile.createNewFile()
                    }
                    cameraLauncher.launch(captureUri)
                }
            }
        },
        onChooseGallery = {
            ensurePermissionsThen(MediaPickPermissions.galleryPermissions()) {
                galleryLauncher.launch("image/*")
            }
        },
        onRemovePhoto = { showRemoveAvatarConfirm = true }
    )

    EditProfileBottomSheet(
        visible = showEditSheet,
        uiState = uiState,
        initialDisplayName = uiState.displayName,
        initialEmail = uiState.email,
        onDismiss = { showEditSheet = false },
        onSaveDisplayName = profileViewModel::onUpdateDisplayName
    )

    if (showRemoveAvatarConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveAvatarConfirm = false },
            shape = DialogShape,
            title = {
                Text(
                    stringResource(R.string.profile_remove_photo_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color = AppBarNavy
                )
            },
            text = {
                Text(stringResource(R.string.profile_remove_photo_confirm_body), color = SubtitleGray)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRemoveAvatarConfirm = false
                        profileViewModel.onRemoveAvatar()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = DialogButtonShape
                ) { Text(stringResource(R.string.profile_remove_photo), color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveAvatarConfirm = false }) {
                    Text(stringResource(R.string.profile_cancel), color = SubtitleGray)
                }
            }
        )
    }

    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            shape = DialogShape,
            title = {
                Text(
                    "Select City",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBarNavy
                )
            },
            text = {
                Column {
                    ugandaCities.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    city = c
                                    showCityDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = c,
                                color = if (c == city) OrangeAccent else SubtitleGray,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (c == city) FontWeight.Bold else FontWeight.Normal
                            )
                            if (c == city) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OrangeAccent)
                                )
                            }
                        }
                        if (c != ugandaCities.last()) {
                            HorizontalDivider(color = DividerRow, thickness = 1.dp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("Cancel", color = SubtitleGray)
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = DialogShape,
            title = {
                Text(
                    "Log Out",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBarNavy
                )
            },
            text = {
                Text(
                    "Are you sure you want to log out of EventPop?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtitleGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = DialogButtonShape
                ) { Text("Log Out", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = SubtitleGray)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = DialogShape,
            title = {
                Text(
                    "Delete Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBarNavy
                )
            },
            text = {
                Text(
                    "This will permanently delete your account and all your data. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtitleGray
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false /* handle delete */ },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = DialogButtonShape
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = SubtitleGray)
                }
            }
        )
    }

    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            shape = DialogShape,
            title = {
                Text(
                    "Send Feedback",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBarNavy
                )
            },
            text = {
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Your feedback") },
                    placeholder = { Text("Tell us what you think...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = DividerVertical,
                        focusedLabelColor = OrangeAccent,
                        unfocusedLabelColor = SubtitleGray,
                        cursorColor = OrangeAccent
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@eventpop.app")
                            putExtra(Intent.EXTRA_SUBJECT, "EventPop Feedback")
                            putExtra(Intent.EXTRA_TEXT, feedbackText)
                        }
                        context.startActivity(intent)
                        showFeedbackDialog = false
                        feedbackText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = DialogButtonShape
                ) { Text("Send", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancel", color = SubtitleGray)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val successTone = data.visuals.duration == SnackbarDuration.Short
                Snackbar(
                    snackbarData = data,
                    containerColor = if (successTone) SuccessSnackbarGreen else ErrorRed,
                    contentColor = Color.White,
                    actionColor = Color.White,
                    actionContentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BodyBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            ProfileHeroAvatarBlock(
                uiState = uiState,
                onEditProfileClick = { showEditSheet = true },
                onOpenAvatarOptions = { showAvatarSheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileStatsRow(
                rsvpCount = rsvpCount,
                city = city,
                alertsOn = notificationsEnabled
            )

            Spacer(modifier = Modifier.height(20.dp))

            ProfileSectionLabel("LOCATION")

            Spacer(modifier = Modifier.height(8.dp))

            ProfileSectionCard {
                ProfileRowItem(
                    icon = Icons.Outlined.LocationOn,
                    label = "City",
                    value = city,
                    onClick = { showCityDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ProfileSectionLabel("NOTIFICATIONS")

            Spacer(modifier = Modifier.height(8.dp))

            ProfileSectionCard {
                ProfileNotificationsRow(
                    enabled = notificationsEnabled,
                    onToggle = { notificationsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ProfileSectionLabel("ACCOUNT SETTINGS")

            Spacer(modifier = Modifier.height(8.dp))

            ProfileSectionCard {
                ProfileRowItem(
                    icon = Icons.Outlined.PersonOutline,
                    label = stringResource(R.string.profile_edit_profile_title),
                    onClick = { showEditSheet = true }
                )
                if (uiState.canCreateEvents) {
                    ProfileRowDivider()
                    ProfileRowItem(
                        icon = Icons.Outlined.AddCircleOutline,
                        label = stringResource(R.string.profile_create_event),
                        onClick = onCreateEvent
                    )
                }
                ProfileRowDivider()
                ProfileRowItem(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = "Send Feedback",
                    onClick = { showFeedbackDialog = true }
                )
                ProfileRowDivider()
                ProfileRowItem(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    label = "Log Out",
                    onClick = { showLogoutDialog = true }
                )
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = DividerRow
                )
                ProfileRowItem(
                    icon = Icons.Outlined.DeleteOutline,
                    label = "Delete Account",
                    destructive = true,
                    onClick = { showDeleteDialog = true }
                )
            }
        }
    }
}

@Composable
private fun ProfileStatsRow(
    rsvpCount: Int,
    city: String,
    alertsOn: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatColumn(value = rsvpCount.toString(), label = "RSVPs", modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(DividerVertical)
        )
        StatColumn(value = city, label = "City", modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(DividerVertical)
        )
        StatColumn(
            value = if (alertsOn) "On" else "Off",
            label = "Alerts",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatColumn(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppBarNavy,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = SubtitleGray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun ProfileSectionLabel(label: String) {
    Text(
        text = label,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
        fontSize = 11.sp,
        color = SubtitleGray,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun ProfileSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Shadow6, spotColor = Shadow6)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
    ) {
        content()
    }
}

@Composable
private fun ProfileNotificationsRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconBg =
            if (enabled) OrangeAccent.copy(alpha = 0.1f) else SubtitleGray.copy(alpha = 0.12f)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (enabled) Icons.Outlined.Notifications else Icons.Outlined.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) OrangeAccent else SubtitleGray
            )
        }
        Text(
            text = "Push Notifications",
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AppBarNavy
        )
        NotificationTogglePill(
            enabled = enabled,
            onToggle = onToggle
        )
    }
}

@Composable
private fun NotificationTogglePill(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val thumbX by animateDpAsState(
        targetValue = if (enabled) 24.dp else 2.dp,
        animationSpec = spring(stiffness = 400f),
        label = "notifThumb"
    )
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) OrangeAccent else DividerVertical)
            .clickable { onToggle(!enabled) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbX)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun ProfileRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        thickness = 1.dp,
        color = DividerRow
    )
}

@Composable
private fun ProfileRowItem(
    icon: ImageVector,
    label: String,
    value: String? = null,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "rowScale"
    )
    val iconBg = when {
        destructive -> ErrorRed.copy(alpha = 0.1f)
        else -> OrangeAccent.copy(alpha = 0.1f)
    }
    val iconTint = if (destructive) ErrorRed else OrangeAccent
    val labelColor = AppBarNavy
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin.Center
            }
            .defaultMinSize(minHeight = 56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = OrangeAccent.copy(alpha = 0.08f)),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtitleGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = ChevronMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
