package com.android.example.eventpop.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.eventpop.ui.navigation.EventPopBottomBar
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray

private val GradientOrange = Color(0xFFFF6B00)
private val GradientPurple = Color(0xFF7B2FBE)

private val ugandaCities = listOf(
    "Kampala", "Entebbe", "Jinja", "Mbarara", "Gulu",
    "Mbale", "Lira", "Masaka", "Fort Portal", "Arua"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavEvents: () -> Unit,
    onNavMap: () -> Unit,
    onNavDiscover: () -> Unit,
    onNavProfile: () -> Unit
) {
    val context = LocalContext.current

    var displayName by remember { mutableStateOf("Kalanzi") }
    val email = "kalanzi@example.com"
    var city by remember { mutableStateOf("Kampala") }
    var notificationsEnabled by remember { mutableStateOf(true) }

    var showCityDialog by remember { mutableStateOf(false) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var editNameField by remember { mutableStateOf(displayName) }
    var feedbackText by remember { mutableStateOf("") }

    // Initials from display name
    val initials = displayName.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "?" }

    // City dialog
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = { Text("Select City", fontWeight = FontWeight.Bold) },
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
                                color = if (c == city) OrangeAccent else MaterialTheme.colorScheme.onSurface,
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
                        if (c != ugandaCities.last()) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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

    // Edit username dialog
    if (showEditUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showEditUsernameDialog = false },
            title = { Text("Edit Username", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editNameField,
                    onValueChange = { editNameField = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        focusedLabelColor = OrangeAccent
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNameField.isNotBlank()) displayName = editNameField.trim()
                        showEditUsernameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditUsernameDialog = false }) {
                    Text("Cancel", color = SubtitleGray)
                }
            }
        )
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of EventPop?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false /* handle logout */ },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = SubtitleGray)
                }
            }
        )
    }

    // Delete account dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will permanently delete your account and all your data. This action cannot be undone.",
                    color = SubtitleGray
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false /* handle delete */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = SubtitleGray)
                }
            }
        )
    }

    // Feedback dialog
    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Send Feedback", fontWeight = FontWeight.Bold) },
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeAccent,
                        focusedLabelColor = OrangeAccent
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Open email intent
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@eventpop.app")
                            putExtra(Intent.EXTRA_SUBJECT, "EventPop Feedback")
                            putExtra(Intent.EXTRA_TEXT, feedbackText)
                        }
                        context.startActivity(intent)
                        showFeedbackDialog = false
                        feedbackText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) { Text("Send") }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancel", color = SubtitleGray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile", color = Color.White, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBarNavy)
            )
        },
        bottomBar = {
            EventPopBottomBar(
                selectedEvents = false,
                selectedMap = false,
                selectedDiscover = false,
                selectedProfile = true,
                onNavEvents = onNavEvents,
                onNavMap = onNavMap,
                onNavDiscover = onNavDiscover,
                onNavProfile = onNavProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile header card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(AppBarNavy, AppBarNavy.copy(alpha = 0.5f))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Initials circle with gradient
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(GradientOrange, GradientPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = email,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // City row
            ProfileSectionCard {
                ProfileRowItem(
                    icon = Icons.Filled.LocationCity,
                    label = "City",
                    value = city,
                    onClick = { showCityDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notifications row
            ProfileSectionCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (notificationsEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                        contentDescription = null,
                        tint = if (notificationsEnabled) OrangeAccent else SubtitleGray,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Notifications",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OrangeAccent,
                            uncheckedThumbColor = SubtitleGray,
                            uncheckedTrackColor = CardBackground
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Account settings section
            ProfileSectionLabel("Account Settings")

            ProfileSectionCard {
                ProfileRowItem(
                    icon = Icons.Filled.Edit,
                    label = "Edit Username",
                    onClick = {
                        editNameField = displayName
                        showEditUsernameDialog = true
                    }
                )
                ProfileDivider()
                ProfileRowItem(
                    icon = Icons.Filled.Logout,
                    label = "Log Out",
                    onClick = { showLogoutDialog = true }
                )
                ProfileDivider()
                ProfileRowItem(
                    icon = Icons.Filled.Delete,
                    label = "Delete Account",
                    labelColor = Color(0xFFDC2626),
                    iconTint = Color(0xFFDC2626),
                    onClick = { showDeleteDialog = true }
                )
                ProfileDivider()
                ProfileRowItem(
                    icon = Icons.Filled.Feedback,
                    label = "Send Feedback",
                    onClick = { showFeedbackDialog = true }
                )
                ProfileDivider()
                ProfileRowItem(
                    icon = Icons.Filled.AddBusiness,
                    label = "Create Event",
                    value = "Opens website",
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://eventpop.app/create")
                        )
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileSectionLabel(label: String) {
    Text(
        text = label,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelLarge,
        color = SubtitleGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun ProfileSectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

@Composable
private fun ProfileRowItem(
    icon: ImageVector,
    label: String,
    value: String? = null,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = OrangeAccent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = labelColor
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtitleGray
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = SubtitleGray,
            modifier = Modifier.size(20.dp)
        )
    }
}
