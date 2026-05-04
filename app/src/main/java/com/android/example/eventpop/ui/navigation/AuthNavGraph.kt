package com.android.example.eventpop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.example.eventpop.EventPopApp
import com.android.example.eventpop.ui.auth.LoginScreen
import com.android.example.eventpop.ui.auth.LoginViewModel
import com.android.example.eventpop.ui.auth.SignUpScreen
import com.android.example.eventpop.ui.auth.SignUpViewModel
import com.android.example.eventpop.ui.controller.AppViewModelFactory
import com.android.example.eventpop.ui.screens.LandingScreen
import com.android.example.eventpop.ui.viewmodel.LandingViewModel

@Composable
fun AuthNavGraph(onAuthenticated: (pendingEventDetailId: String?) -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = remember(context.applicationContext) {
        context.applicationContext as EventPopApp
    }
    val viewModelFactory = remember(app) { AppViewModelFactory(app) }

    NavHost(
        navController = navController,
        startDestination = AuthDestinations.LANDING
    ) {
        composable(AuthDestinations.LANDING) {
            val landingViewModel: LandingViewModel = viewModel(factory = viewModelFactory)
            LandingScreen(
                viewModel = landingViewModel,
                onSignIn = {
                    PendingAfterAuth.clear()
                    navController.navigate(AuthDestinations.LOGIN)
                },
                onRegister = {
                    PendingAfterAuth.clear()
                    navController.navigate(AuthDestinations.SIGN_UP)
                },
                onSeeAllLive = {
                    PendingAfterAuth.clear()
                    navController.navigate(AuthDestinations.LOGIN)
                },
                onViewAllFeatured = {
                    PendingAfterAuth.clear()
                    navController.navigate(AuthDestinations.LOGIN)
                },
                onGetTickets = { eventId ->
                    PendingAfterAuth.setPendingEventDetail(eventId)
                    navController.navigate(AuthDestinations.LOGIN)
                }
            )
        }
        composable(AuthDestinations.LOGIN) {
            val viewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = {
                    navController.navigate(AuthDestinations.SIGN_UP)
                },
                onAuthenticated = {
                    onAuthenticated(PendingAfterAuth.takePendingEventDetail())
                }
            )
        }
        composable(AuthDestinations.SIGN_UP) {
            val viewModel: SignUpViewModel = viewModel()
            SignUpScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() },
                onAuthenticated = {
                    onAuthenticated(PendingAfterAuth.takePendingEventDetail())
                }
            )
        }
    }
}
