package com.andres.pillremind.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinos disponibles en la barra de navegación inferior.
 *
 * Cada objeto define la ruta de navegación, el título mostrado al usuario
 * y el icono asociado a una sección principal de la aplicación.
 *
 * @property route Ruta utilizada por Navigation Compose.
 * @property title Texto mostrado en la barra de navegación.
 * @property icon Icono representativo de la sección.
 */
sealed class BottomBarScreens(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    /**
     * Pantalla principal de la aplicación.
     */
    object Home : BottomBarScreens(
        route = "home",
        title = "Home",
        icon = Icons.Rounded.Home
    )

    /**
     * Pantalla de listado y gestión de medicamentos.
     */
    object Medications : BottomBarScreens(
        route = "medications", "Medications", Icons.Rounded.Medication
    )
    
    /**
     * Pantalla de visualización del perfil activo.
     */
    object Profile : BottomBarScreens(
        route = "profile/see",
        title = "Profile",
        icon = Icons.Rounded.Person
    )

}