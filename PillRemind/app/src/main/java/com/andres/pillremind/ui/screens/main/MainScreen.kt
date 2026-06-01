package com.andres.pillremind.ui.screens.main

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.andres.pillremind.navigation.BottomBarScreens
import com.andres.pillremind.navigation.MainNavigationGraph
import com.andres.pillremind.navigation.MedicationScreens

/**
 * Pantalla principal de la aplicación.
 *
 * Contiene la navegación inferior, el `Scaffold` general y el botón flotante
 * para crear una nueva medicación cuando la ruta lo permite.
 *
 * @param navController Controlador de navegación interno de la barra inferior.
 * @param rootNavController Controlador de navegación raíz usado por el grafo principal.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    rootNavController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showFab =
        currentRoute == BottomBarScreens.Home.route || currentRoute == BottomBarScreens.Medications.route
    var topBarState by remember {
        mutableStateOf(TopBarState(""))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomBarRoutes = remember {
        setOf(
            BottomBarScreens.Home.route,
            BottomBarScreens.Medications.route,
            BottomBarScreens.Profile.route
        )
    }

    currentRoute !in bottomBarRoutes
            && navController.previousBackStackEntry != null
    Scaffold(
        topBar = { MainTopBar(state = topBarState, navController = navController) },
        bottomBar = { MainBottomBar(navController = navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(MedicationScreens.MedicationCreateScreen.route) },
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                }
            }
        }

    ) { paddingValues ->
        MainNavigationGraph(
            navController = navController,
            rootNavController = rootNavController,
            paddingValues = paddingValues,
            onTopBarChange = { topBarState = it },
            snackbarHostState = snackbarHostState
        )
    }
}