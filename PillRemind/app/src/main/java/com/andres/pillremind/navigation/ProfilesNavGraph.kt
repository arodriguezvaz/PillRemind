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
import com.andres.pillremind.ui.screens.settings.SettingsScreen
import com.andres.pillremind.ui.session.SessionViewModel

/**
 * Grafo de navegación para la selección y gestión de perfiles.
 *
 * Incluye las pantallas de selección de perfil, creación de perfil
 * y configuración de la aplicación
 *
 * @param navController Controlador de navegación para esta parte del grafo
 */
fun NavGraphBuilder.profileSelectionGraph(
    navController: NavHostController,
) {
    navigation(
        route = Graph.PROFILE_SELECTION,
        startDestination = ProfilesScreen.ProfileSelectionScreen.route
    ) {
        //Pantalla de selección de perfiles
        composable(ProfilesScreen.ProfileSelectionScreen.route) {
            val sessionViewModel: SessionViewModel = hiltViewModel()
            ProfilesScreen(
                onAddClicked = {
                    navController.navigate(ProfilesScreen.ProfileCreateScreen.route)
                },

                onProfileSelected = { perfil ->
                    //Gurdar el id del perfil activo y navegar al grafo main
                    sessionViewModel.setActiveProfile(perfil.id)
                    navController.navigate(Graph.MAIN)
                },
                navController = navController,
                onSettingClicked = {
                    navController.navigate(ProfilesScreen.SettingsScreen.route)
                }
            )
        }
        //Pantalla de selección de creación de perfil
        composable(ProfilesScreen.ProfileCreateScreen.route) {
            androidx.compose.material3.Scaffold { paddingValues ->
                ProfileScreen(
                    modifier = Modifier.padding(paddingValues),
                    profileMode = ProfileMode.Create,
                    onSaveClicked = {
                        navController.navigate(ProfilesScreen.ProfileSelectionScreen.route) {
                            popUpTo(ProfilesScreen.ProfileSelectionScreen.route) {
                                inclusive = false
                            }
                        }
                    }

                )
            }
        }
        //Pantalla de configuración de la aplicación
        composable(ProfilesScreen.SettingsScreen.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Destinos de navegación pertenecientes al flujo de selección de perfiles.
 *
 * Cada objeto representa una pantalla accesible dentro del grafo
 * [Graph.PROFILE_SELECTION].
 *
 * @property route Ruta utilizada por Navigation Compose para identificar
 * el destino.
 */
sealed class ProfilesScreen(val route: String) {

    /**
     * Pantalla de selección de perfiles.
     */
    object ProfileSelectionScreen : ProfilesScreen("profiles")

    /**
     * Pantalla de creación de un nuevo perfil.
     */
    object ProfileCreateScreen : ProfilesScreen("profile/create")

    /**
     * Pantalla de configuración de la aplicación.
     */
    object SettingsScreen : ProfilesScreen("settings")
}