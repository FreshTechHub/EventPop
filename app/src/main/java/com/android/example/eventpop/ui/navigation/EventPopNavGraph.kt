package com.android.example.eventpop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.example.eventpop.ui.home.HomeScreen

object EventPopDestinations {
    const val DISCOVER = "discover"
    const val EVENTS = "events"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}

@Composable
fun EventPopNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = EventPopDestinations.EVENTS
    ) {
        composable(EventPopDestinations.EVENTS) {
            HomeScreen(
                selectedDiscover = false,
                selectedEvents = true,
                selectedProfile = false,
                selectedSettings = false,
                onNavDiscover = { navController.navigate(EventPopDestinations.DISCOVER) },
                onNavEvents = { },
                onNavProfile = { navController.navigate(EventPopDestinations.PROFILE) },
                onNavSettings = { navController.navigate(EventPopDestinations.SETTINGS) }
            )
        }
        composable(EventPopDestinations.DISCOVER) {
            HomeScreen(
                selectedDiscover = true,
                selectedEvents = false,
                selectedProfile = false,
                selectedSettings = false,
                onNavDiscover = { },
                onNavEvents = { navController.navigate(EventPopDestinations.EVENTS) },
                onNavProfile = { navController.navigate(EventPopDestinations.PROFILE) },
                onNavSettings = { navController.navigate(EventPopDestinations.SETTINGS) }
            )
        }
        composable(EventPopDestinations.PROFILE) {
            HomeScreen(
                selectedDiscover = false,
                selectedEvents = false,
                selectedProfile = true,
                selectedSettings = false,
                onNavDiscover = { navController.navigate(EventPopDestinations.DISCOVER) },
                onNavEvents = { navController.navigate(EventPopDestinations.EVENTS) },
                onNavProfile = { },
                onNavSettings = { navController.navigate(EventPopDestinations.SETTINGS) }
            )
        }
        composable(EventPopDestinations.SETTINGS) {
            HomeScreen(
                selectedDiscover = false,
                selectedEvents = false,
                selectedProfile = false,
                selectedSettings = true,
                onNavDiscover = { navController.navigate(EventPopDestinations.DISCOVER) },
                onNavEvents = { navController.navigate(EventPopDestinations.EVENTS) },
                onNavProfile = { navController.navigate(EventPopDestinations.PROFILE) },
                onNavSettings = { }
            )
        }
    }
}
