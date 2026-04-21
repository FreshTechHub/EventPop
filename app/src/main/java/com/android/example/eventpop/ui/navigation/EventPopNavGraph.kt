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
import com.android.example.eventpop.ui.home.HomeScreen
import com.android.example.eventpop.ui.screens.EventDetailScreen
import com.android.example.eventpop.ui.screens.FilterEventsScreen
import com.android.example.eventpop.ui.screens.ProfileScreen
import com.android.example.eventpop.ui.screens.SearchScreen
import com.android.example.eventpop.ui.screens.SettingsScreen
import com.android.example.eventpop.ui.viewmodel.EventDetailViewModel
import kotlinx.coroutines.delay

object EventPopDestinations {
    const val DISCOVER = "discover"
    const val EVENTS = "events"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
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

    // Double back to exit logic on Events tab
    if (currentRoute == EventPopDestinations.EVENTS) {
        BackHandler {
            if (backPressedOnce) {
                (context as? android.app.Activity)?.finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                // Reset flag after 2 seconds
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

    NavHost(
        navController = navController,
        startDestination = EventPopDestinations.EVENTS
    ) {
        composable(EventPopDestinations.EVENTS) {
            val eventsBackStackEntry = navController.getBackStackEntry(EventPopDestinations.EVENTS)
            val filterResult by eventsBackStackEntry.savedStateHandle
                .getStateFlow<EventFilter?>(EventPopDestinations.FILTER_RESULT_KEY, null)
                .collectAsState(initial = null)
            HomeScreen(
                onFilterClick = { navController.navigate(EventPopDestinations.FILTER_EVENTS) },
                currentFilter = filterResult,
                selectedDiscover = false,
                selectedEvents = true,
                selectedProfile = false,
                selectedSettings = false,
                onNavDiscover = { navigateToTab(EventPopDestinations.DISCOVER) },
                onNavEvents = { navigateToTab(EventPopDestinations.EVENTS) },
                onNavProfile = { navigateToTab(EventPopDestinations.PROFILE) },
                onNavSettings = { navigateToTab(EventPopDestinations.SETTINGS) },
                onSearchClick = { navController.navigate(EventPopDestinations.SEARCH) },
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) }
            )
        }
        composable(
            route = EventPopDestinations.EVENT_DETAIL,
            arguments = listOf(navArgument(EventPopDestinations.EVENT_DETAIL_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel: EventDetailViewModel = viewModel()
            EventDetailScreen(
                navController = navController,
                viewModel = viewModel,
                navBackStackEntry = backStackEntry
            )
        }
        composable(EventPopDestinations.DISCOVER) {
            HomeScreen(
                selectedDiscover = true,
                selectedEvents = false,
                selectedProfile = false,
                selectedSettings = false,
                onNavDiscover = { navigateToTab(EventPopDestinations.DISCOVER) },
                onNavEvents = { navigateToTab(EventPopDestinations.EVENTS) },
                onNavProfile = { navigateToTab(EventPopDestinations.PROFILE) },
                onNavSettings = { navigateToTab(EventPopDestinations.SETTINGS) },
                onSearchClick = { navController.navigate(EventPopDestinations.SEARCH) },
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) }
            )
        }
        composable(EventPopDestinations.PROFILE) {
            ProfileScreen(
                navController = navController,
                onNavDiscover = { navigateToTab(EventPopDestinations.DISCOVER) },
                onNavEvents = { navigateToTab(EventPopDestinations.EVENTS) },
                onNavProfile = { navigateToTab(EventPopDestinations.PROFILE) },
                onNavSettings = { navigateToTab(EventPopDestinations.SETTINGS) }
            )
        }
        composable(EventPopDestinations.SETTINGS) {
            SettingsScreen(
                navController = navController,
                onNavDiscover = { navigateToTab(EventPopDestinations.DISCOVER) },
                onNavEvents = { navigateToTab(EventPopDestinations.EVENTS) },
                onNavProfile = { navigateToTab(EventPopDestinations.PROFILE) },
                onNavSettings = { navigateToTab(EventPopDestinations.SETTINGS) }
            )
        }
        composable(EventPopDestinations.SEARCH) {
            SearchScreen(navController = navController)
        }
        composable(EventPopDestinations.FILTER_EVENTS) {
            FilterEventsScreen(navController = navController)
        }
    }
}
