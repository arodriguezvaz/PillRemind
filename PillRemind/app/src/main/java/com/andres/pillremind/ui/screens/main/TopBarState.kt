package com.andres.pillremind.ui.screens.main

/**
 * Representa el estado de la barra superior.
 *
 * @property title Título que se mostrará en la topbar.
 * @property showLogo Si es true, se mostrará el logo de la app encima del título.
 * @property canGoBack Si es true , se muestra la flecha de retroceso
 */
data class TopBarState(
    val title: String,
    val showLogo: Boolean = false,
)