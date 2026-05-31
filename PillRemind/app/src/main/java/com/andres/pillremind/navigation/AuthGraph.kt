package com.andres.pillremind.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.andres.pillremind.ui.screens.auth.LoginScreen
import com.andres.pillremind.ui.screens.auth.RegisterScreen

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        route = Graph.AUTH,
        startDestination = AuthScreen.Login.route
    ) {
        composable(AuthScreen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Graph.PROFILE_SELECTION) {
                        popUpTo(Graph.AUTH) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AuthScreen.Register.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(AuthScreen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Graph.PROFILE_SELECTION) {
                        popUpTo(Graph.AUTH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object Register : AuthScreen("register")
}