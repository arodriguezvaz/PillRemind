package com.andres.pillremind.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import com.andres.pillremind.ui.screens.adherence.AdherenceScreen
import com.andres.pillremind.ui.screens.archived.ArchivedMedicationsScreen
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
 * Los cambios de destino son inmediatos: se desactivan las transiciones de
 * Navigation Compose para que moverse entre secciones no parezca lento.
 *
 * Los títulos estáticos de la top bar se derivan de la ruta en `MainScreen`.
 * Solo los destinos con estado dinámico (perfil o medicamento en
 * creación/edición) publican un override.
 *
 * @param navController Controlador de navegación utilizado dentro del grafo principal.
 * @param rootNavController Controlador de navegación raíz utilizado para realizar
 * navegaciones fuera de este grafo, como volver a la selección de perfiles.
 * @param paddingValues Espaciado proporcionado por el contenedor padre para evitar
 * solapamientos con otros componentes de la interfaz.
 * @param onTopBarChange Callback para publicar overrides de la barra superior.
 * Recibe la ruta del destino que lo publica y su estado.
 */
@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    paddingValues: PaddingValues,
    onTopBarChange: (route: String, state: TopBarState) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreens.Home.route,
        modifier = Modifier.padding(paddingValues),
        // Sin fundido: el cambio entre secciones debe ser inmediato.
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        //Home screen donde se ven las tomas del día
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
                onArchivedClick = {
                    navController.navigate(MedicationScreens.ArchivedMedicationsScreen.route)
                },
            )
        }
        composable(MedicationScreens.ArchivedMedicationsScreen.route) {
            ArchivedMedicationsScreen()
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
                onAdherenceClicked = {
                    navController.navigate(ProfileScreens.Adherence.route)
                },
                onTopBarChange = topBarChangeFor(
                    BottomBarScreens.Profile.route,
                    onTopBarChange
                ),
            )
        }
        composable(ProfileScreens.Adherence.route) {
            AdherenceScreen()
        }
        medicationNavGraph(navController, onTopBarChange)
    }
}

/**
 * Vincula un override de la top bar con la ruta del destino que lo publica.
 *
 * Solo se usa en pantallas con estado dinámico (perfil o medicamento en
 * creación/edición). Los títulos estáticos se resuelven en `MainScreen` a
 * partir de la ruta actual.
 *
 * @param route Ruta del destino que informa el estado.
 * @param onTopBarChange Callback general que recibe la ruta y el estado.
 */
private fun topBarChangeFor(
    route: String,
    onTopBarChange: (route: String, state: TopBarState) -> Unit
): (TopBarState) -> Unit = { state -> onTopBarChange(route, state) }

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
 * @param onTopBarChange Callback para publicar overrides de la barra superior.
 * Recibe la ruta del destino que lo publica y su estado.
 */
fun NavGraphBuilder.medicationNavGraph(
    navController: NavHostController,
    onTopBarChange: (route: String, state: TopBarState) -> Unit
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
                onTopBarChange = topBarChangeFor(
                    MedicationScreens.MedicationSeeScreen.route,
                    onTopBarChange
                ),
            )
        }
        composable(route = MedicationScreens.MedicationCreateScreen.route) {
            MedicationScreen(
                onSaveClicked = { navController.popBackStack() },
                medicationMode = MedicationMode.Create,
                onTopBarChange = topBarChangeFor(
                    MedicationScreens.MedicationCreateScreen.route,
                    onTopBarChange
                )
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

    /** Lista de medicamentos archivados. */
    object ArchivedMedicationsScreen : MedicationScreens("medication/archived")
}

/**
 * Destinos secundarios accesibles desde la pestaña de perfil.
 */
sealed class ProfileScreens(val route: String) {
    /** Estadísticas de adherencia del perfil activo. */
    object Adherence : ProfileScreens("profile/adherence")
}
