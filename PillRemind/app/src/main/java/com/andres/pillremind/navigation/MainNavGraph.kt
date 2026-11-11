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
import com.andres.pillremind.ui.screens.history.HistoryScreen
import com.andres.pillremind.ui.screens.home.HomeScreen
import com.andres.pillremind.ui.screens.importer.MedicationImportScreen
import com.andres.pillremind.ui.screens.main.TopBarState
import com.andres.pillremind.ui.screens.medication.MedicationMode
import com.andres.pillremind.ui.screens.medication.MedicationScreen
import com.andres.pillremind.ui.screens.medications.ArchivedMedicationsScreen
import com.andres.pillremind.ui.screens.medications.MedicationsScreen
import com.andres.pillremind.ui.screens.profile.ProfileMode
import com.andres.pillremind.ui.screens.profile.ProfileScreen
import com.andres.pillremind.ui.screens.statistics.StatisticsScreen

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
    val openStatistics = {
        navController.navigate(StatisticsScreens.Statistics.route) {
            launchSingleTop = true
        }
    }
    val openHistory = {
        navController.navigate(HistoryScreens.History.createRoute()) {
            launchSingleTop = true
        }
    }
    val openMedicationImport = {
        navController.navigate(MedicationScreens.MedicationImportScreen.route) {
            launchSingleTop = true
        }
    }

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
            HomeScreen(
                onStatisticsClick = openStatistics,
                onImportMedicationsClick = openMedicationImport,
            )
        }
        composable(BottomBarScreens.Medications.route) {
            MedicationsScreen(
                onMedicationClick = { id ->
                    navController.navigate(
                        MedicationScreens.MedicationSeeScreen.createRoute(id)
                    )
                },
                onImportMedicationsClick = openMedicationImport,
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
                onTopBarChange = topBarChangeFor(
                    BottomBarScreens.Profile.route,
                    onTopBarChange
                ),
                onStatisticsClick = openStatistics,
                onImportMedicationsClick = openMedicationImport,
                onArchivedMedicationsClick = {
                    navController.navigate(MedicationScreens.ArchivedMedicationsScreen.route)
                },
                onHistoryClick = openHistory,
            )
        }
        composable(StatisticsScreens.Statistics.route) {
            StatisticsScreen()
        }
        composable(
            route = StatisticsScreens.MedicationStatistics.route,
            arguments = listOf(navArgument("medicationId") {
                type = NavType.IntType
            })
        ) {
            StatisticsScreen()
        }
        composable(
            route = HistoryScreens.History.route,
            arguments = listOf(navArgument("medicationId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) {
            HistoryScreen()
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
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getInt("medicationId")

            MedicationScreen(
                medicationMode = MedicationMode.View,
                onArchivedClicked = {
                    navController.popBackStack()
                },
                onStatisticsClick = {
                    medicationId?.let { id ->
                        navController.navigate(
                            StatisticsScreens.MedicationStatistics.createRoute(id)
                        ) {
                            launchSingleTop = true
                        }
                    }
                },
                onHistoryClick = {
                    medicationId?.let { id ->
                        navController.navigate(
                            HistoryScreens.History.createRoute(id)
                        ) {
                            launchSingleTop = true
                        }
                    }
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
        composable(route = MedicationScreens.MedicationImportScreen.route) {
            MedicationImportScreen(
                onSaved = { navController.popBackStack() },
                onOpenMedication = { id, archived ->
                    val route = if (archived) {
                        MedicationScreens.ArchivedMedicationSeeScreen.createRoute(id)
                    } else {
                        MedicationScreens.MedicationSeeScreen.createRoute(id)
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(route = MedicationScreens.ArchivedMedicationsScreen.route) {
            ArchivedMedicationsScreen(
                onMedicationClick = { id ->
                    navController.navigate(
                        MedicationScreens.ArchivedMedicationSeeScreen.createRoute(id)
                    )
                },
            )
        }
        composable(
            route = MedicationScreens.ArchivedMedicationSeeScreen.route,
            arguments = listOf(navArgument("medicationId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getInt("medicationId")

            MedicationScreen(
                medicationMode = MedicationMode.Archived,
                onUnarchivedClicked = { navController.popBackStack() },
                onDeletedPermanentlyClicked = { navController.popBackStack() },
                onStatisticsClick = {
                    medicationId?.let { id ->
                        navController.navigate(
                            StatisticsScreens.MedicationStatistics.createRoute(id)
                        ) {
                            launchSingleTop = true
                        }
                    }
                },
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

    /**
     * Importación de medicamentos desde una hoja de medicación en PDF.
     */
    object MedicationImportScreen : MedicationScreens("medication/import")

    /**
     * Lista de medicamentos archivados del perfil activo.
     */
    object ArchivedMedicationsScreen : MedicationScreens("medication/archived")

    /**
     * Detalle de un medicamento archivado.
     */
    object ArchivedMedicationSeeScreen :
        MedicationScreens("medication/archived/{medicationId}") {
        fun createRoute(medicationId: Int) = "medication/archived/$medicationId"
    }
}
