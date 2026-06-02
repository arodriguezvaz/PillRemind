package com.andres.pillremind.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.andres.pillremind.ui.screens.main.MainScreen

/**
 * Grafo de navegación raíz de la aplicación.
 *
 * Este composable inicializa el [NavHost] principal y registra los distintos
 * subgrafos y destinos de navegación de la aplicación.
 *
 * El flujo de navegación comienza en [Graph.PROFILE_SELECTION], donde el
 * usuario selecciona o crea un perfil. Una vez seleccionado un perfil,
 * se navega al destino principal [Graph.MAIN].
 *
 * También registra el flujo de autenticación mediante [Graph.AUTH].
 *
 * @param navController Controlador encargado de gestionar la navegación
 * entre los diferentes destinos.
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
        authGraph(navController)
    }
}

/**
 * Contiene las rutas de los grafos principales de navegación de la aplicación.
 *
 * Estas constantes se utilizan para identificar los distintos flujos
 * de navegación registrados en el [NavHost].
 */
object Graph {

    /**
     * Grafo raíz que contiene el resto de grafos de navegación.
     */
    const val ROOT = "root_graph"

    /**
     * Grafo encargado de la selección y gestión de perfiles.
     */
    const val PROFILE_SELECTION = "profile_selection_graph"

    /**
     * Grafo principal de la aplicación mostrado tras seleccionar un perfil.
     */
    const val MAIN = "main_graph"

    /**
     * Grafo relacionado con la gestión de medicamentos.
     */
    const val MEDICATION = "medication_graph"

    /**
     * Grafo encargado de las pantallas de autenticación.
     */
    const val AUTH = "auth_graph"
}