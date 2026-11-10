package com.andres.pillremind.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.andres.pillremind.R

/**
 * Destinos disponibles en la barra de navegación inferior.
 *
 * Cada objeto define la ruta de navegación, el título mostrado al usuario
 * y el recurso del icono asociado a una sección principal de la aplicación.
 *
 * @property route Ruta utilizada por Navigation Compose.
 * @property titleRes Recurso de texto mostrado en la barra de navegación.
 * @property iconRes Recurso drawable del icono representativo de la sección.
 */
sealed class BottomBarScreens(
    val route: String,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int
) {
    /**
     * Pantalla principal de la aplicación.
     */
    object Home : BottomBarScreens(
        route = "home",
        titleRes = R.string.nav_home,
        iconRes = R.drawable.ic_home
    )

    /**
     * Pantalla de listado y gestión de medicamentos.
     */
    object Medications : BottomBarScreens(
        route = "medications",
        titleRes = R.string.nav_medications,
        iconRes = R.drawable.ic_medication
    )

    /**
     * Pantalla de visualización del perfil activo.
     */
    object Profile : BottomBarScreens(
        route = "profile/see",
        titleRes = R.string.nav_profile,
        iconRes = R.drawable.ic_person
    )
}
