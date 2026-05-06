package com.andres.pillremind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.repository.MedicationRepositoryImpl
import com.andres.pillremind.data.repository.ProfileRepositoryImpl
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.ui.screens.main.MainScreen
import com.andres.pillremind.ui.session.SessionViewModel
import com.andres.pillremind.ui.session.SessionViewModelFactory

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

    }
}


object Graph {
    const val ROOT = "root_graph"
    const val PROFILE_SELECTION = "profile_selection_graph"
    const val MAIN = "main_graph"
    const val MEDICATION = "medication_graph"
}