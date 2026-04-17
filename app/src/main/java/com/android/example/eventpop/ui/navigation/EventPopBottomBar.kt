package com.android.example.eventpop.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.example.eventpop.R
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.OrangeAccent

@Composable
fun EventPopBottomBar(
    selectedDiscover: Boolean,
    selectedEvents: Boolean,
    selectedProfile: Boolean,
    selectedSettings: Boolean,
    onNavDiscover: () -> Unit,
    onNavEvents: () -> Unit,
    onNavProfile: () -> Unit,
    onNavSettings: () -> Unit
) {
    NavigationBar(
        containerColor = AppBarNavy,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedDiscover,
            onClick = onNavDiscover,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_nav_discover),
                    contentDescription = stringResource(R.string.nav_discover)
                )
            },
            label = { Text(stringResource(R.string.nav_discover)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                unselectedIconColor = Color.White.copy(alpha = 0.8f),
                unselectedTextColor = Color.White.copy(alpha = 0.8f),
                indicatorColor = if (selectedDiscover) OrangeAccent else Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedEvents,
            onClick = onNavEvents,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_nav_events),
                    contentDescription = stringResource(R.string.nav_events)
                )
            },
            label = { Text(stringResource(R.string.nav_events)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                unselectedIconColor = Color.White.copy(alpha = 0.8f),
                unselectedTextColor = Color.White.copy(alpha = 0.8f),
                indicatorColor = if (selectedEvents) OrangeAccent else Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedProfile,
            onClick = onNavProfile,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_nav_profile),
                    contentDescription = stringResource(R.string.nav_profile)
                )
            },
            label = { Text(stringResource(R.string.nav_profile)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                unselectedIconColor = Color.White.copy(alpha = 0.8f),
                unselectedTextColor = Color.White.copy(alpha = 0.8f),
                indicatorColor = if (selectedProfile) OrangeAccent else Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedSettings,
            onClick = onNavSettings,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_nav_settings),
                    contentDescription = stringResource(R.string.nav_settings)
                )
            },
            label = { Text(stringResource(R.string.nav_settings)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                unselectedIconColor = Color.White.copy(alpha = 0.8f),
                unselectedTextColor = Color.White.copy(alpha = 0.8f),
                indicatorColor = if (selectedSettings) OrangeAccent else Color.Transparent
            )
        )
    }
}
