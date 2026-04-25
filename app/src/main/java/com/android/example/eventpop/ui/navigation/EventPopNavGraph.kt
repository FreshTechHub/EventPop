package com.android.example.eventpop.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.example.eventpop.data.EventFilter
import com.android.example.eventpop.ui.viewmodel.EventDetailViewModel
import com.android.example.eventpop.ui.home.HomeScreen
import com.android.example.eventpop.ui.screens.DiscoverScreen
import com.android.example.eventpop.ui.screens.EventDetailScreen
import com.android.example.eventpop.ui.screens.FavoritesScreen
import com.android.example.eventpop.ui.screens.FilterEventsScreen
import com.android.example.eventpop.ui.screens.MapScreen
import com.android.example.eventpop.ui.screens.ProfileScreen

object EventPopDestinations {
    const val EVENTS = "events"
    const val MAP = "map"
    const val DISCOVER = "discover"
    const val FAVOURITES = "favourites"
    const val PROFILE = "profile"
    const val FILTER_EVENTS = "filter_events"
    const val FILTER_RESULT_KEY = "event_filter"
    const val EVENT_DETAIL = "event_detail/{eventId}"
    const val EVENT_DETAIL_ID_ARG = "eventId"

    fun eventDetailRoute(eventId: String) = "event_detail/$eventId"
}

@Composable
fun EventPopNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    var backPressedOnce by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Double-back to exit on home screen
    if (currentRoute == EventPopDestinations.EVENTS) {
        BackHandler {
            if (backPressedOnce) {
                (context as? android.app.Activity)?.finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    backPressedOnce = false
                }, 2000)
            }
        }
    }

    fun navigateToTab(route: String) {
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val navEvents    = { navigateToTab(EventPopDestinations.EVENTS) }
    val navMap       = { navigateToTab(EventPopDestinations.MAP) }
    val navDiscover  = { navigateToTab(EventPopDestinations.DISCOVER) }
    val navFavorites = { navigateToTab(EventPopDestinations.FAVOURITES) }
    val navProfile   = { navigateToTab(EventPopDestinations.PROFILE) }

    NavHost(
        navController = navController,
        startDestination = EventPopDestinations.EVENTS
    ) {
        // Events tab
        composable(EventPopDestinations.EVENTS) {
            val eventsBackStackEntry = navController.getBackStackEntry(EventPopDestinations.EVENTS)
            val filterResult by eventsBackStackEntry.savedStateHandle
                .getStateFlow<EventFilter?>(EventPopDestinations.FILTER_RESULT_KEY, null)
                .collectAsState(initial = null)
            HomeScreen(
                onFilterClick = { navController.navigate(EventPopDestinations.FILTER_EVENTS) },
                currentFilter = filterResult,
                selectedEvents = true,
                selectedMap = false,
                selectedDiscover = false,
                selectedFavorites = false,
                selectedProfile = false,
                onNavEvents = navEvents,
                onNavMap = navMap,
                onNavDiscover = navDiscover,
                onNavFavorites = navFavorites,
                onNavProfile = navProfile,
                onSearchClick = { navController.navigate(EventPopDestinations.DISCOVER) },
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) }
            )
        }

        // Map tab
        composable(EventPopDestinations.MAP) {
            MapScreen(
                onNavEvents = navEvents,
                onNavMap = navMap,
                onNavDiscover = navDiscover,
                onNavFavorites = navFavorites,
                onNavProfile = navProfile
            )
        }

        // Discover tab
        composable(EventPopDestinations.DISCOVER) {
            DiscoverScreen(
                onNavEvents = navEvents,
                onNavMap = navMap,
                onNavDiscover = navDiscover,
                onNavFavorites = navFavorites,
                onNavProfile = navProfile,
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) }
            )
        }

        // Favorites tab
        composable(EventPopDestinations.FAVOURITES) {
            FavoritesScreen(
                onNavEvents = navEvents,
                onNavMap = navMap,
                onNavDiscover = navDiscover,
                onNavFavorites = navFavorites,
                onNavProfile = navProfile
            )
        }

        // Profile tab
        composable(EventPopDestinations.PROFILE) {
            ProfileScreen(
                onNavEvents = navEvents,
                onNavMap = navMap,
                onNavDiscover = navDiscover,
                onNavFavorites = navFavorites,
                onNavProfile = navProfile
            )
        }

        // Full-screen event detail (off-tab)
        composable(
            route = EventPopDestinations.EVENT_DETAIL,
            arguments = listOf(navArgument(EventPopDestinations.EVENT_DETAIL_ID_ARG) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val viewModel: EventDetailViewModel = viewModel(backStackEntry)
            EventDetailScreen(
                navController = navController,
                viewModel = viewModel,
                navBackStackEntry = backStackEntry
            )
        }

        // Filter modal (off-tab)
        composable(EventPopDestinations.FILTER_EVENTS) {
            FilterEventsScreen(navController = navController)
        }
    }
}
