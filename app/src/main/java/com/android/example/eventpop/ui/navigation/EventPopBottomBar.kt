package com.android.example.eventpop.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.OrangeAccent

@Composable
fun EventPopBottomBar(
    selectedEvents: Boolean,
    selectedMap: Boolean,
    selectedDiscover: Boolean,
    selectedFavorites: Boolean,
    selectedProfile: Boolean,
    onNavEvents: () -> Unit,
    onNavMap: () -> Unit,
    onNavDiscover: () -> Unit,
    onNavFavorites: () -> Unit,
    onNavProfile: () -> Unit
) {
    NavigationBar(
        containerColor = AppBarNavy,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedEvents,
            onClick = onNavEvents,
            icon = {
                Icon(
                    imageVector = if (selectedEvents) Icons.Filled.Event else Icons.Outlined.Event,
                    contentDescription = "Events"
                )
            },
            label = { Text("Events") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = OrangeAccent.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = selectedMap,
            onClick = onNavMap,
            icon = {
                Icon(
                    imageVector = if (selectedMap) Icons.Filled.Map else Icons.Outlined.Map,
                    contentDescription = "Map"
                )
            },
            label = { Text("Map") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = OrangeAccent.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = selectedDiscover,
            onClick = onNavDiscover,
            icon = {
                Icon(
                    imageVector = if (selectedDiscover) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Discover"
                )
            },
            label = { Text("Discover") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = OrangeAccent.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = selectedFavorites,
            onClick = onNavFavorites,
            icon = {
                Icon(
                    imageVector = if (selectedFavorites) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                    contentDescription = "Favorites"
                )
            },
            label = { Text("Favorites") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = OrangeAccent.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = selectedProfile,
            onClick = onNavProfile,
            icon = {
                Icon(
                    imageVector = if (selectedProfile) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = OrangeAccent.copy(alpha = 0.15f)
            )
        )
    }
}
