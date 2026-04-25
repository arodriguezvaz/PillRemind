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
import com.andres.pillremind.data.repository.ProfileRepositoryImpl
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.ui.screens.BottomBarScreen
import com.andres.pillremind.ui.screens.home.HomeScreen
import com.andres.pillremind.ui.screens.home.HomeViewModel
import com.andres.pillremind.ui.screens.home.HomeViewModelFactory
import com.andres.pillremind.ui.screens.medication.MedicationMode
import com.andres.pillremind.ui.screens.medication.MedicationScreen
import com.andres.pillremind.ui.screens.medication.MedicationViewModel
import com.andres.pillremind.ui.screens.medication.MedicationViewModelFactory
import com.andres.pillremind.ui.screens.profile.ProfileMode
import com.andres.pillremind.ui.screens.profile.ProfileScreen
import com.andres.pillremind.ui.screens.profile.ProfileViewModel
import com.andres.pillremind.ui.screens.profile.ProfileViewModelFactory
import com.andres.pillremind.ui.session.SessionViewModel

@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    profileRepository: ProfileRepository,
    medicationRepository: MedicationRepository,
) {
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(BottomBarScreen.Home.route) {
            val profileId by sessionViewModel.activeProfileId.collectAsState()
            //TODO revisar esto
            if (profileId == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@composable
            }
            val vm = viewModel<HomeViewModel>(
                key = "profile_${profileId}",
                factory = HomeViewModelFactory(
                    profileRepository,
                    medicationRepository,
                    profileId!!
                ),
            )
            HomeScreen(
                homeViewModel = vm,
                onAddClicked = {
                    navController.navigate(MedicationScreens.MedicationCreateScreen.route)
                }
            )
        }


        composable("profile/see") {
            val profileId by sessionViewModel.activeProfileId.collectAsState()

            val vm = viewModel<ProfileViewModel>(
                factory = ProfileViewModelFactory(profileRepository, profileId)
            )

            ProfileScreen(
                profileViewModel = vm,
                profileMode = ProfileMode.View
            )
        }

        medicationNavGraph(navController, sessionViewModel, medicationRepository)
    }
}

fun NavGraphBuilder.medicationNavGraph(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    medicationRepository: MedicationRepository
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
            val profileId by sessionViewModel.activeProfileId.collectAsState()

            val vm = viewModel<MedicationViewModel>(
                factory = MedicationViewModelFactory(medicationRepository, profileId!!)
            )
            MedicationScreen(
                medicationViewModel = viewModel(),
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
