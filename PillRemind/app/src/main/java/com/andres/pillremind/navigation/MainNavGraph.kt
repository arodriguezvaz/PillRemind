package com.andres.pillremind.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.andres.pillremind.ui.screens.home.HomeScreen
import com.andres.pillremind.ui.screens.medication.MedicationMode
import com.andres.pillremind.ui.screens.medication.MedicationScreen
import com.andres.pillremind.ui.screens.medications.MedicationsScreen
import com.andres.pillremind.ui.screens.profile.ProfileMode
import com.andres.pillremind.ui.screens.profile.ProfileScreen


@Composable
fun MainNavigationGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = BottomBarScreens.Home.route
    ) {
        composable(BottomBarScreens.Home.route) {
            HomeScreen()
        }
        composable(BottomBarScreens.Medications.route) {
            MedicationsScreen()
        }

        composable(BottomBarScreens.Profile.route) {
            ProfileScreen(
                profileMode = ProfileMode.View
            )
        }

        medicationNavGraph(navController)
    }
}

fun NavGraphBuilder.medicationNavGraph(
    navController: NavHostController
) {
    navigation(
        route = Graph.MEDICATION,
        startDestination = MedicationScreens.SeeMedicationScreen.route
    ) {
        /*composable(route = MedicationScreen.SeeMedicationScreen.route) {
            MedicationScreen() {
                //navController.navigate(MedicationScreen.MedicationCreateScreen.route)
            }
        }*/
        composable(route = MedicationScreens.MedicationCreateScreen.route) {
            MedicationScreen(
                onSaveClicked = { navController.popBackStack() },
                medicationMode = MedicationMode.Create
            )
        }
    }
}

sealed class MedicationScreens(val route: String) {
    object SeeMedicationScreen : MedicationScreens("medication/see/{medicationId}") {
        fun createRoute(medicationId: Int) = "medication/see/$medicationId"
    }

    object MedicationEditScreen : MedicationScreens("medication/edit/{medicationId}") {
        fun createRoute(medicationId: Int) = "medication/edit/$medicationId"
    }

    object MedicationCreateScreen : MedicationScreens("medication/create")
}
