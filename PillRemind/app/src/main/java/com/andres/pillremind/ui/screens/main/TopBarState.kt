package com.andres.pillremind.ui.screens.main

/**
 * Representa el estado de la barra superior.
 *
 * @property title Título que se mostrará en la topbar.
 * @property showLogo Si es true, se mostrará el logo de la app encima del título.
 * @property actions Acciones que se muestran en la barra superior.
 * @property navigationAction Acción mostrada como icono de navegación. Si es
 *   null, se mantiene el comportamiento de retroceso por defecto.
 */
data class TopBarState(
    val title: String,
    val showLogo: Boolean = false,
    val actions: List<TopBarAction> = emptyList(),
    val navigationAction: TopBarAction? = null
)

/**
 * Representa una acción que se puede realizar en la barra superior.
 * @property icon Icono de la acción.
 * @property contentDescription Descripción del icono.
 * @property onClick Función que se ejecuta cuando se hace clic en la acción.
 */
data class TopBarAction(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)