package com.andres.pillremind.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.andres.pillremind.data.repository.ProfileRepositoryImpl
import com.andres.pillremind.ui.screens.profile.ProfileViewModelFactory
import com.andres.pillremind.ui.screens.profile.ProfileMode
import com.andres.pillremind.ui.screens.profile.ProfileScreen
import com.andres.pillremind.ui.screens.profile.ProfileViewModel
import com.andres.pillremind.ui.screens.profiles.ProfilesScreen
import com.andres.pillremind.ui.screens.profiles.ProfilesViewModel
import com.andres.pillremind.ui.screens.profiles.ProfilesViewModelFactory
import com.andres.pillremind.ui.session.SessionViewModel

fun NavGraphBuilder.profileSelectionGraph(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    repo: ProfileRepositoryImpl
) {
    navigation(
        route = Graph.PROFILE_SELECTION,
        startDestination = ProfilesScreen.ProfileSelectionScreen.route
    ) {

        composable(ProfilesScreen.ProfileSelectionScreen.route) {
            val vm = viewModel<ProfilesViewModel>(
                factory = ProfilesViewModelFactory(repo)
            )
            ProfilesScreen(
                viewModel = vm,
                onAddClicked = {
                    navController.navigate(ProfilesScreen.ProfileCreateScreen.route)
                },

                onProfileSelected = { perfil ->
                    sessionViewModel.setActiveProfile(perfil.id)
                    navController.navigate(Graph.HOME)
                }
            )
        }

        composable(ProfilesScreen.ProfileCreateScreen.route) {
            val vm = viewModel<ProfileViewModel>(
                factory = ProfileViewModelFactory(repo)
            )

            ProfileScreen(
                profileMode = ProfileMode.Create,
                viewModel = vm,
                onSaveClicked = {
                    navController.navigate(ProfilesScreen.ProfileSelectionScreen.route) {
                        popUpTo(ProfilesScreen.ProfileSelectionScreen.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

sealed class ProfilesScreen(val route: String) {
    object ProfileSelectionScreen : ProfilesScreen(route = "profiles")
    object ProfileCreateScreen : ProfilesScreen(route = "profile/create")
}
