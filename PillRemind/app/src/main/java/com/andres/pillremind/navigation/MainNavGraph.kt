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
import com.andres.pillremind.ui.screens.main.TopBarState
import com.andres.pillremind.ui.screens.medication.MedicationMode
import com.andres.pillremind.ui.screens.medication.MedicationScreen
import com.andres.pillremind.ui.screens.medications.MedicationsScreen
import com.andres.pillremind.ui.screens.profile.ProfileMode
import com.andres.pillremind.ui.screens.profile.ProfileScreen

/**
 * Grafo de navegación principal de la aplicación.
 *
 * Este composable contiene los destinos accesibles desde la barra de navegación
 * inferior, así como el subgrafo relacionado con la gestión de medicamentos.
 *
 * El grafo se inicializa mostrando la pantalla de inicio y permite navegar
 * entre las secciones principales de la aplicación: inicio, medicamentos y perfil.
 *
 * @param navController Controlador de navegación utilizado dentro del grafo principal.
 * @param rootNavController Controlador de navegación raíz utilizado para realizar
 * navegaciones fuera de este grafo, como volver a la selección de perfiles.
 * @param paddingValues Espaciado proporcionado por el contenedor padre para evitar
 * solapamientos con otros componentes de la interfaz.
 * @param onTopBarChange Callback utilizado para actualizar el estado de la barra
 * superior según la pantalla mostrada.
 */
@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    paddingValues: PaddingValues,
    onTopBarChange: (TopBarState) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreens.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        //Home screen donde se ven las tomas del día
        composable(BottomBarScreens.Home.route) {
            HomeScreen(onTopBarChange = onTopBarChange)
        }
        composable(BottomBarScreens.Medications.route) {
            MedicationsScreen(
                onMedicationClick = { id ->
                    navController.navigate(
                        MedicationScreens.MedicationSeeScreen.createRoute(id)
                    )
                },
                onTopBarChange = onTopBarChange,

                )
        }

        composable(BottomBarScreens.Profile.route) {
            ProfileScreen(
                profileMode = ProfileMode.View,
                onDeletedClicked = {
                    // Si el perfil activo es eliminado, se regresa a la pantalla de selección
                    // de perfiles limpiando completamente la pila de navegación.
                    rootNavController.navigate(ProfilesScreen.ProfileSelectionScreen.route) {
                        popUpTo(Graph.ROOT) {
                            inclusive = true
                        }
                    }
                },
                onTopBarChange = onTopBarChange,
            )
        }
        medicationNavGraph(navController, onTopBarChange)
    }
}

/**
 * Añade al grafo de navegación los destinos relacionados con la gestión
 * de medicamentos.
 *
 * Este subgrafo permite visualizar la información de un medicamento
 * existente o crear uno nuevo. Las pantallas incluidas reutilizan el mismo
 * composable adaptando su estado dependiendo del modo de operación.
 *
 * El destino de visualización recibe el identificador del medicamento
 * mediante argumentos de navegación.
 *
 * @param navController Controlador utilizado para gestionar la navegación
 * entre los destinos de este subgrafo.
 * @param onTopBarChange Callback utilizado para actualizar el estado de la
 * barra superior según la pantalla mostrada.
 */
fun NavGraphBuilder.medicationNavGraph(
    navController: NavHostController,
    onTopBarChange: (TopBarState) -> Unit
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
        ) {
            MedicationScreen(
                medicationMode = MedicationMode.View,
                onArchivedClicked = {
                    navController.popBackStack()
                },
                onTopBarChange = onTopBarChange,
            )
        }
        composable(route = MedicationScreens.MedicationCreateScreen.route) {
            MedicationScreen(
                onSaveClicked = { navController.popBackStack() },
                medicationMode = MedicationMode.Create,
                onTopBarChange = onTopBarChange
            )
        }
    }
}

/**
 * Destinos de navegación pertenecientes al flujo de gestión de medicamentos.
 *
 * Cada objeto representa una pantalla accesible dentro del grafo
 * [Graph.MEDICATION].
 *
 * @property route Ruta utilizada por Navigation Compose para identificar
 * el destino.
 */
sealed class MedicationScreens(val route: String) {
    object MedicationSeeScreen : MedicationScreens("medication/see/{medicationId}") {
        fun createRoute(medicationId: Int) = "medication/see/$medicationId"
    }

    object MedicationCreateScreen : MedicationScreens("medication/create")
}
