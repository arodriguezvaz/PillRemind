package com.andres.pillremind.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * TopAppBar principal de la aplicación.
 *
 * Muestra el título actual, el logo opcional, acciones configurables
 * y un botón de navegación hacia atrás cuando la pantalla lo permite.
 *
 * La visibilidad del botón "back" depende de la ruta actual del NavController
 * y de si existe una pantalla anterior en la pila de navegación.
 *
 * @param state Estado de la TopBar (título, logo y acciones).
 * @param navController Controlador de navegación para gestionar el back stack.
 * @param topLevelRoutes Rutas principales que no muestran flecha de retroceso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    state: TopBarState,
    navController: NavHostController,
    topLevelRoutes: Set<String>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val canGoBack = currentRoute !in topLevelRoutes &&
        navController.previousBackStackEntry != null
    TopAppBar(
        navigationIcon = {
            if (canGoBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            }
        },
        title = {
            Column {
                if (state.showLogo) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("PILL")
                            }

                            withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                                append("REMIND")
                            }
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(state.title)
            }
        },
        actions = {
            state.actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(action.icon, contentDescription = action.contentDescription)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}