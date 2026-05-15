package com.andres.pillremind.ui.screens.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
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
    var topBarTitle by remember {
        mutableStateOf("")
    }
    Scaffold(
        topBar = {
            MainTopBar(title = topBarTitle)
        },
        bottomBar = { BottomBar(navController = navController) },
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
            onTitleChange = { topBarTitle = it }
        )

    }
}

/**
 * Barra de navegación inferior reutilizable para `MainScreen`.
 *
 * @param navController Controlador de navegación que se usa al pulsar cada item.
 */
@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreens.Home,
        BottomBarScreens.Medications,
        BottomBarScreens.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomBarDestination = screens.any { it.route == currentDestination?.route }
    if (bottomBarDestination) {
        NavigationBar {
            screens.forEach { screen ->
                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController,

                    )
            }
        }
    }
}

/**
 * Item individual de la barra inferior.
 *
 * @param screen Elemento de navegación a representar.
 * @param currentDestination Destino actual para marcar selección.
 * @param navController Controlador de navegación.
 */
@Composable
fun RowScope.AddItem(
    screen: BottomBarScreens,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    NavigationBarItem(
        label = {
            Text(text = screen.title)
        },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = "Navigation Icon"
            )
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(title: String) {


    TopAppBar(
        title = {
            Text(text = title)
        }
    )
}