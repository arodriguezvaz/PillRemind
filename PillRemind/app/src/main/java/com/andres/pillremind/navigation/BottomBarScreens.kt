package com.andres.pillremind.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.andres.pillremind.R

/**
 * Destinos disponibles en la barra de navegación inferior.
 *
 * Cada objeto define la ruta de navegación, el título mostrado al usuario
 * y el icono asociado a una sección principal de la aplicación.
 *
 * @property route Ruta utilizada por Navigation Compose.
 * @property titleRes Recurso de texto mostrado en la barra de navegación.
 * @property icon Icono representativo de la sección.
 */
sealed class BottomBarScreens(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    /**
     * Pantalla principal de la aplicación.
     */
    object Home : BottomBarScreens(
        route = "home",
        titleRes = R.string.nav_home,
        icon = Icons.Rounded.Home
    )

    /**
     * Pantalla de listado y gestión de medicamentos.
     */
    object Medications : BottomBarScreens(
        route = "medications",
        titleRes = R.string.nav_medications,
        icon = Icons.Rounded.Medication
    )

    /**
     * Pantalla de visualización del perfil activo.
     */
    object Profile : BottomBarScreens(
        route = "profile/see",
        titleRes = R.string.nav_profile,
        icon = Icons.Rounded.Person
    )
}
