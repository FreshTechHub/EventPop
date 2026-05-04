package com.android.example.eventpop.ui.navigation

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.android.example.eventpop.EventPopApp
import com.android.example.eventpop.LandingPageActivity
import com.android.example.eventpop.data.AuthRepository
import com.android.example.eventpop.data.EventFilter
import com.android.example.eventpop.ui.controller.AppViewModelFactory
import com.android.example.eventpop.ui.home.HomeScreen
import com.android.example.eventpop.ui.screens.DiscoverScreen
import com.android.example.eventpop.ui.screens.EventDetailScreen
import com.android.example.eventpop.ui.screens.FavoritesScreen
import com.android.example.eventpop.ui.screens.FilterEventsScreen
import com.android.example.eventpop.ui.screens.MapScreen
import com.android.example.eventpop.ui.screens.ProfileScreen
import com.android.example.eventpop.ui.screens.SearchScreen
import com.android.example.eventpop.ui.viewmodel.DiscoverViewModel
import com.android.example.eventpop.ui.viewmodel.EventDetailViewModel
import com.android.example.eventpop.ui.viewmodel.FavoritesViewModel
import com.android.example.eventpop.ui.viewmodel.HomeViewModel
import com.android.example.eventpop.ui.viewmodel.MapViewModel
import com.android.example.eventpop.ui.viewmodel.ProfileViewModel
import com.android.example.eventpop.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

object EventPopDestinations {
    const val EVENTS = "events"
    const val MAP = "map"
    const val DISCOVER = "discover"
    const val FAVOURITES = "favourites"
    const val PROFILE = "profile"
    const val FILTER_EVENTS = "filter_events"
    const val FILTER_RESULT_KEY = "event_filter"
    const val SEARCH = "search"
    const val EVENT_DETAIL = "event_detail/{eventId}"
    const val EVENT_DETAIL_ID_ARG = "eventId"

    fun eventDetailRoute(eventId: String) = "event_detail/$eventId"
}

/**
 * **Controller** wiring for the main graph: owns ViewModels, hoists [com.android.example.eventpop.ui.mvc] UI state
 * into **View** composables and forwards user actions back to ViewModels.
 */
@Composable
fun EventPopNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = remember(context.applicationContext) {
        context.applicationContext as EventPopApp
    }
    val viewModelFactory = remember(app) { AppViewModelFactory(app) }
    val coroutineScope = rememberCoroutineScope()

    var backPressedOnce by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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

    val navEvents = { navigateToTab(EventPopDestinations.EVENTS) }
    val navMap = { navigateToTab(EventPopDestinations.MAP) }
    val navDiscover = { navigateToTab(EventPopDestinations.DISCOVER) }
    val navFavorites = { navigateToTab(EventPopDestinations.FAVOURITES) }
    val navProfile = { navigateToTab(EventPopDestinations.PROFILE) }

    NavHost(
        navController = navController,
        startDestination = EventPopDestinations.EVENTS
    ) {
        composable(EventPopDestinations.EVENTS) {
            val eventsBackStackEntry = navController.getBackStackEntry(EventPopDestinations.EVENTS)
            val filterResult by eventsBackStackEntry.savedStateHandle
                .getStateFlow<EventFilter?>(EventPopDestinations.FILTER_RESULT_KEY, null)
                .collectAsState(initial = null)
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            val homeUiState by homeViewModel.uiState.collectAsState()
            HomeScreen(
                uiState = homeUiState,
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
                onSearchClick = { navController.navigate(EventPopDestinations.SEARCH) },
                onSeeAllHotEvents = navDiscover,
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) },
                onEventRsvp = { homeViewModel.rsvpEvent(it.id) }
            )
        }

        composable(EventPopDestinations.MAP) {
            val mapViewModel: MapViewModel = viewModel(factory = viewModelFactory)
            val mapUiState by mapViewModel.uiState.collectAsState()
            MapScreen(
                uiState = mapUiState,
                onNavEvents = navEvents,
                onNavMap = navMap,
                onNavDiscover = navDiscover,
                onNavFavorites = navFavorites,
                onNavProfile = navProfile
            )
        }

        composable(EventPopDestinations.DISCOVER) {
            val discoverViewModel: DiscoverViewModel = viewModel(factory = viewModelFactory)
            val discoverUiState by discoverViewModel.uiState.collectAsState()
            DiscoverScreen(
                onNavEvents = navEvents,
                onNavMap = navMap,
                onNavDiscover = navDiscover,
                onNavFavorites = navFavorites,
                onNavProfile = navProfile,
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) },
                uiState = discoverUiState,
                onSearchQueryChange = discoverViewModel::setSearchQuery,
                onRefresh = discoverViewModel::refresh
            )
        }

        composable(EventPopDestinations.FAVOURITES) {
            val favoritesViewModel: FavoritesViewModel = viewModel(factory = viewModelFactory)
            LaunchedEffect(Unit) {
                favoritesViewModel.refresh()
            }
            val favoritesUiState by favoritesViewModel.uiState.collectAsState()
            FavoritesScreen(
                uiState = favoritesUiState,
                onEventClick = { navController.navigate(EventPopDestinations.eventDetailRoute(it.id)) },
                onNavEvents = navEvents,
                onNavMap = navMap,
                onNavDiscover = navDiscover,
                onNavFavorites = navFavorites,
                onNavProfile = navProfile,
                onSignIn = {
                    context.startActivity(
                        Intent(context, LandingPageActivity::class.java).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            )
                        }
                    )
                },
                onRemoveFavorite = { favoritesViewModel.removeFromFavorites(it.id) }
            )
        }

        composable(EventPopDestinations.SEARCH) {
            val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
            val searchUiState by searchViewModel.uiState.collectAsState()
            SearchScreen(
                navController = navController,
                uiState = searchUiState,
                onQueryChange = searchViewModel::setQuery
            )
        }

        composable(EventPopDestinations.PROFILE) {
            val profileViewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
            LaunchedEffect(Unit) {
                profileViewModel.refresh()
            }
            val profileUiState by profileViewModel.uiState.collectAsState()
            ProfileScreen(
                uiState = profileUiState,
                onNavEvents = navEvents,
                onNavMap = navMap,
                onNavDiscover = navDiscover,
                onNavFavorites = navFavorites,
                onNavProfile = navProfile,
                onLogoutConfirmed = {
                    coroutineScope.launch {
                        try {
                            AuthRepository.signOut()
                            val ctx = context
                            ctx.startActivity(
                                Intent(ctx, LandingPageActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("EventPopNavGraph", "Logout failed", e)
                        }
                    }
                }
            )
        }

        composable(
            route = EventPopDestinations.EVENT_DETAIL,
            arguments = listOf(navArgument(EventPopDestinations.EVENT_DETAIL_ID_ARG) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val detailViewModel: EventDetailViewModel = viewModel(backStackEntry, factory = viewModelFactory)
            val detailUiState by detailViewModel.uiState.collectAsState()
            val eventId = backStackEntry.arguments?.getString(EventPopDestinations.EVENT_DETAIL_ID_ARG).orEmpty()
            LaunchedEffect(eventId) {
                detailViewModel.loadEvent(eventId)
            }
            EventDetailScreen(
                navController = navController,
                uiState = detailUiState,
                onToggleInterested = detailViewModel::toggleInterested,
                onSubmitRsvp = detailViewModel::submitRsvp,
                onConsumeRsvpSuccess = detailViewModel::consumeRsvpSuccess
            )
        }

        composable(EventPopDestinations.FILTER_EVENTS) {
            FilterEventsScreen(navController = navController)
        }
    }
}
