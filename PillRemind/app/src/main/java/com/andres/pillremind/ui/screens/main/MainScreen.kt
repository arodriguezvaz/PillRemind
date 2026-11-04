package com.andres.pillremind.ui.screens.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.andres.pillremind.R
import com.andres.pillremind.navigation.BottomBarScreens
import com.andres.pillremind.navigation.MainNavigationGraph
import com.andres.pillremind.navigation.MedicationScreens
import com.andres.pillremind.navigation.ProfileScreens
import com.andres.pillremind.ui.components.AppFab

/**
 * Pantalla principal de la aplicación.
 *
 * Contiene la navegación inferior, el `Scaffold` general y el botón flotante
 * para crear una nueva medicación cuando la ruta lo permite.
 *
 * @param navController Controlador de navegación interno de la barra inferior.
 * @param rootNavController Controlador de navegación raíz usado por el grafo principal.
 */
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    rootNavController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showFab =
        currentRoute == BottomBarScreens.Home.route || currentRoute == BottomBarScreens.Medications.route
    val topLevelRoutes = remember {
        setOf(
            BottomBarScreens.Home.route,
            BottomBarScreens.Medications.route,
            BottomBarScreens.Profile.route
        )
    }
    val canGoBack = currentRoute !in topLevelRoutes &&
        navController.previousBackStackEntry != null

    val defaultTopBarState = when (currentRoute) {
        BottomBarScreens.Home.route -> TopBarState(
            // En Inicio la barra muestra el logo y la fecha vive en el
            // contenido de HomeScreen, para no apilar dos líneas.
            title = "",
            showLogo = true
        )
        BottomBarScreens.Medications.route -> TopBarState(
            title = stringResource(R.string.nav_medications)
        )
        BottomBarScreens.Profile.route -> TopBarState(
            title = stringResource(R.string.profile_view_title)
        )
        ProfileScreens.Adherence.route -> TopBarState(
            title = stringResource(R.string.adherence_title)
        )
        MedicationScreens.MedicationSeeScreen.route -> TopBarState(
            title = stringResource(R.string.medication_view_title)
        )
        MedicationScreens.MedicationCreateScreen.route -> TopBarState(
            title = stringResource(R.string.medication_add_title)
        )
        MedicationScreens.ArchivedMedicationsScreen.route -> TopBarState(
            title = stringResource(R.string.medication_archived_title)
        )
        else -> TopBarState(title = stringResource(R.string.nav_home))
    }

    // Los títulos estáticos se derivan de la ruta actual. El mapa solo guarda
    // overrides de pantallas con estado dinámico (perfil o medicamento en
    // creación/edición), siempre bajo la ruta que los publica.
    val topBarStates = remember { mutableStateMapOf<String, TopBarState>() }
    val topBarState = resolveTopBarState(
        currentRoute = currentRoute,
        statesByRoute = topBarStates,
        fallback = defaultTopBarState
    )

    Scaffold(
        topBar = {
            MainTopBar(
                state = topBarState,
                canGoBack = canGoBack,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = { MainBottomBar(navController = navController) },
        floatingActionButton = {
            if (showFab) {
                AppFab(
                    onClick = { navController.navigate(MedicationScreens.MedicationCreateScreen.route) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.medication_add_title)
                    )
                }
            }
        }

    ) { paddingValues ->
        MainNavigationGraph(
            navController = navController,
            rootNavController = rootNavController,
            paddingValues = paddingValues,
            onTopBarChange = { route, state -> topBarStates[route] = state }
        )
    }
}