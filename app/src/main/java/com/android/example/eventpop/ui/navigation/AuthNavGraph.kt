package com.android.example.eventpop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.example.eventpop.ui.auth.LoginScreen
import com.android.example.eventpop.ui.auth.LoginViewModel
import com.android.example.eventpop.ui.auth.SignUpScreen
import com.android.example.eventpop.ui.auth.SignUpViewModel

@Composable
fun AuthNavGraph(onAuthenticated: () -> Unit) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AuthDestinations.LOGIN
    ) {
        composable(AuthDestinations.LOGIN) {
            val viewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = {
                    navController.navigate(AuthDestinations.SIGN_UP)
                },
                onAuthenticated = onAuthenticated
            )
        }
        composable(AuthDestinations.SIGN_UP) {
            val viewModel: SignUpViewModel = viewModel()
            SignUpScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onAuthenticated = onAuthenticated
            )
        }
    }
}
