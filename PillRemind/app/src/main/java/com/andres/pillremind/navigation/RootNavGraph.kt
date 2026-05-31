package com.andres.pillremind.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.andres.pillremind.ui.screens.main.MainScreen

/**
 * Composable que representa la navegación principal de la aplicación
 */
@Composable
fun RootNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = Graph.PROFILE_SELECTION

    ) {

        profileSelectionGraph(navController)
        composable(route = Graph.MAIN) {
            MainScreen(rootNavController = navController)
        }
        authGraph(navController)
    }
}


object Graph {
    const val ROOT = "root_graph"
    const val PROFILE_SELECTION = "profile_selection_graph"
    const val MAIN = "main_graph"
    const val MEDICATION = "medication_graph"
    const val AUTH = "auth_graph"
}