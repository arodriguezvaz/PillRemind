package com.andres.pillremind.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.andres.pillremind.ui.screens.profile.ProfileMode
import com.andres.pillremind.ui.screens.profile.ProfileScreen
import com.andres.pillremind.ui.screens.profiles.ProfilesScreen
import com.andres.pillremind.ui.session.SessionViewModel

fun NavGraphBuilder.profileSelectionGraph(
    navController: NavHostController,
) {
    navigation(
        route = Graph.PROFILE_SELECTION,
        startDestination = ProfilesScreen.ProfileSelectionScreen.route
    ) {

        composable(ProfilesScreen.ProfileSelectionScreen.route) {
            val sessionViewModel: SessionViewModel = hiltViewModel()
            ProfilesScreen(
                onAddClicked = {
                    navController.navigate(ProfilesScreen.ProfileCreateScreen.route)
                },

                onProfileSelected = { perfil ->
                    sessionViewModel.setActiveProfile(perfil.id)
                    navController.navigate(Graph.MAIN)
                },
                navController = navController
            )
        }

        composable(ProfilesScreen.ProfileCreateScreen.route) {
            androidx.compose.material3.Scaffold { paddingValues ->
                ProfileScreen(
                    modifier = Modifier.padding(paddingValues),
                    profileMode = ProfileMode.Create,
                    onSaveClicked = {
                        navController.navigate(ProfilesScreen.ProfileSelectionScreen.route) {
                            popUpTo(ProfilesScreen.ProfileSelectionScreen.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}

sealed class ProfilesScreen(val route: String) {
    object ProfileSelectionScreen : ProfilesScreen(route = "profiles")
    object ProfileCreateScreen : ProfilesScreen(route = "profile/create")
}
