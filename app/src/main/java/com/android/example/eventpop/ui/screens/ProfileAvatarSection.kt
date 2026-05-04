package com.android.example.eventpop.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import com.android.example.eventpop.R
import com.android.example.eventpop.ui.components.AvatarComposable
import com.android.example.eventpop.ui.mvc.ProfileUiState
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.CardBackground
import com.android.example.eventpop.ui.theme.OrangeAccent
import com.android.example.eventpop.ui.theme.SubtitleGray
import androidx.compose.ui.res.stringResource

private val HeroSheetBg = Color(0xFFF4F6F9)
private val FieldBorderIdle = Color(0xFFE0E0E0)
private val SoftCircle = Color(0xFFF4F6F9)
private val RemoveTintBg = Color(0xFFFFE5E5)
private val RemoveTintFg = Color(0xFFE53935)
private val OverlayBlack55 = Color(0x8C000000)
private val ShadowBlack15 = Color(0x26000000)

@Composable
fun ProfileHeroAvatarBlock(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit,
    onOpenAvatarOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(heroShape)
            .background(AppBarNavy)
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(3.dp, OrangeAccent, CircleShape)
            ) {
                AvatarComposable(
                    avatarUrl = uiState.avatarUrl,
                    avatarLocalPath = uiState.avatarLocalPath,
                    displayName = uiState.displayName,
                    size = 96.dp,
                    modifier = Modifier.fillMaxSize(),
                    initialsFontSize = 30.sp
                )
                if (uiState.isUploadingAvatar) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(OverlayBlack55),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = OrangeAccent,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            CameraBadge(onClick = onOpenAvatarOptions, modifier = Modifier.align(Alignment.BottomEnd))
        }
        Text(
            text = uiState.displayName,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = uiState.email,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        OutlinedButton(
            onClick = onEditProfileClick,
            modifier = Modifier
                .padding(top = 8.dp)
                .height(32.dp),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OrangeAccent),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = OrangeAccent
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
        ) {
            Text(stringResource(R.string.profile_edit_profile_title), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CameraBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(),
        label = "cameraBadgeScale"
    )
    Box(
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .size(28.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(4.dp, CircleShape, ambientColor = ShadowBlack15, spotColor = ShadowBlack15)
            .clip(CircleShape)
            .background(OrangeAccent)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PhotoCamera,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarOptionsSheet(
    visible: Boolean,
    hasAvatar: Boolean,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = CardBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_sheet_photo_title),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppBarNavy,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            AvatarOptionRow(
                iconBg = SoftCircle,
                iconTint = AppBarNavy,
                icon = Icons.Outlined.CameraAlt,
                label = stringResource(R.string.profile_take_photo),
                labelColor = AppBarNavy,
                onClick = {
                    onDismiss()
                    onTakePhoto()
                }
            )
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            AvatarOptionRow(
                iconBg = SoftCircle,
                iconTint = AppBarNavy,
                icon = Icons.Filled.PhotoLibrary,
                label = stringResource(R.string.profile_choose_gallery),
                labelColor = AppBarNavy,
                onClick = {
                    onDismiss()
                    onChooseGallery()
                }
            )
            if (hasAvatar) {
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                AvatarOptionRow(
                    iconBg = RemoveTintBg,
                    iconTint = RemoveTintFg,
                    icon = Icons.Filled.DeleteOutline,
                    label = stringResource(R.string.profile_remove_photo),
                    labelColor = RemoveTintFg,
                    onClick = {
                        onDismiss()
                        onRemovePhoto()
                    }
                )
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.profile_cancel), color = SubtitleGray, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun AvatarOptionRow(
    iconBg: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    labelColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Text(label, fontSize = 15.sp, color = labelColor, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    visible: Boolean,
    uiState: ProfileUiState,
    initialDisplayName: String,
    initialEmail: String,
    onDismiss: () -> Unit,
    onSaveDisplayName: (String) -> Unit,
    onSaveEmail: (String) -> Unit
) {
    if (!visible) return
    var localName by remember(visible, initialDisplayName) { mutableStateOf(initialDisplayName) }
    var localEmail by remember(visible, initialEmail) { mutableStateOf(initialEmail) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = HeroSheetBg,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.profile_edit_profile_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppBarNavy
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = SubtitleGray, modifier = Modifier.size(20.dp))
                }
            }
            ProfileEditorLabel(stringResource(R.string.profile_display_name_label))
            OutlinedTextField(
                value = localName,
                onValueChange = { localName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.profile_placeholder_display_name)) },
                leadingIcon = {
                    Icon(Icons.Filled.PersonOutline, contentDescription = null, tint = SubtitleGray, modifier = Modifier.size(20.dp))
                },
                trailingIcon = if (localName.isNotBlank() && localName.trim() != initialDisplayName.trim()) {
                    {
                        IconButton(onClick = { onSaveDisplayName(localName.trim()) }, enabled = !uiState.isUpdatingName) {
                            if (uiState.isUpdatingName) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = OrangeAccent, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = FieldBorderIdle,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    cursorColor = OrangeAccent,
                    focusedLabelColor = OrangeAccent
                )
            )
            Spacer(Modifier.height(16.dp))
            ProfileEditorLabel(stringResource(R.string.profile_email_label))
            OutlinedTextField(
                value = localEmail,
                onValueChange = { localEmail = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = SubtitleGray, modifier = Modifier.size(20.dp))
                },
                trailingIcon = if (localEmail.isNotBlank() && localEmail.trim() != initialEmail.trim()) {
                    {
                        IconButton(onClick = { onSaveEmail(localEmail.trim()) }, enabled = !uiState.isUpdatingEmail) {
                            if (uiState.isUpdatingEmail) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = OrangeAccent, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = FieldBorderIdle,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    cursorColor = OrangeAccent
                )
            )
            if (uiState.emailUpdatePending) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                    Text(
                        stringResource(R.string.profile_email_verify_hint),
                        fontSize = 12.sp,
                        color = SubtitleGray,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileEditorLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = SubtitleGray,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
