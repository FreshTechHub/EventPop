package com.android.example.eventpop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.example.eventpop.data.EventFilter
import com.android.example.eventpop.ui.home.HomeScreen
import com.android.example.eventpop.ui.screens.EventDetailScreen
import com.android.example.eventpop.ui.screens.FilterEventsScreen
import com.android.example.eventpop.ui.viewmodel.EventDetailViewModel

object EventPopDestinations {
    const val DISCOVER = "discover"
    const val EVENTS = "events"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
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
                onNavDiscover = { navController.navigate(EventPopDestinations.DISCOVER) },
                onNavEvents = { },
                onNavProfile = { navController.navigate(EventPopDestinations.PROFILE) },
                onNavSettings = { navController.navigate(EventPopDestinations.SETTINGS) },
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
                onNavDiscover = { },
                onNavEvents = { navController.navigate(EventPopDestinations.EVENTS) },
                onNavProfile = { navController.navigate(EventPopDestinations.PROFILE) },
                onNavSettings = { navController.navigate(EventPopDestinations.SETTINGS) },
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) }
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
                onNavSettings = { navController.navigate(EventPopDestinations.SETTINGS) },
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) }
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
                onNavSettings = { },
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) }
            )
        }
        composable(EventPopDestinations.FILTER_EVENTS) {
            FilterEventsScreen(navController = navController)
        }
    }
}
