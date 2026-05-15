package com.andres.pillremind.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.andres.pillremind.ui.screens.home.HomeScreen
import com.andres.pillremind.ui.screens.medication.MedicationMode
import com.andres.pillremind.ui.screens.medication.MedicationScreen
import com.andres.pillremind.ui.screens.medications.MedicationsScreen
import com.andres.pillremind.ui.screens.profile.ProfileMode
import com.andres.pillremind.ui.screens.profile.ProfileScreen


@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    paddingValues: PaddingValues,
    onTitleChange: (String) -> Unit
) {
    NavHost(
        navController = navController,
        route = Graph.MAIN,
        startDestination = BottomBarScreens.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(BottomBarScreens.Home.route) {
            HomeScreen()
        }
        composable(BottomBarScreens.Medications.route) {
            MedicationsScreen(
                onMedicationClick = { id ->
                    navController.navigate(
                        MedicationScreens.MedicationSeeScreen.createRoute(id)
                    )
                },
            )
        }

        composable(BottomBarScreens.Profile.route) {
            ProfileScreen(
                profileMode = ProfileMode.View,
                onDeletedClicked = {
                    rootNavController.navigate(ProfilesScreen.ProfileSelectionScreen.route) {
                        popUpTo(Graph.ROOT) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        medicationNavGraph(navController, onTitleChange)
    }
}

fun NavGraphBuilder.medicationNavGraph(
    navController: NavHostController,
    onTitleChange: (String) -> Unit
) {
    navigation(
        route = Graph.MEDICATION,
        startDestination = MedicationScreens.MedicationCreateScreen.route
    ) {
        composable(
            route = MedicationScreens.MedicationSeeScreen.route,
            arguments = listOf(navArgument("medicationId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->

            backStackEntry.arguments?.getInt("medicationId")
            MedicationScreen(
                medicationMode = MedicationMode.View,
                onDeleteClicked = {
                    navController.popBackStack()
                },
                onTitleChange = onTitleChange,
            )
        }
        composable(route = MedicationScreens.MedicationCreateScreen.route) {
            MedicationScreen(
                onSaveClicked = { navController.popBackStack() },
                medicationMode = MedicationMode.Create,
                onTitleChange = onTitleChange,
            )
        }
    }
}

sealed class MedicationScreens(val route: String) {
    object MedicationSeeScreen : MedicationScreens("medication/see/{medicationId}") {
        fun createRoute(medicationId: Int) = "medication/see/$medicationId"
    }


    object MedicationCreateScreen : MedicationScreens("medication/create")
}
