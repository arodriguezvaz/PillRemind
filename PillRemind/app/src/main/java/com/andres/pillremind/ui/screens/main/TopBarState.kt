package com.andres.pillremind.ui.screens.main

/**
 * Representa el estado de la barra superior.
 *
 * @property title Título que se mostrará en la topbar.
 * @property showLogo Si es true, se muestra el logo de la app junto al título,
 *   en la misma línea. El logo es decorativo para accesibilidad.
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
 * Selecciona el estado de la barra superior que corresponde a la ruta actual.
 *
 * `MainScreen` calcula el estado por defecto de cada ruta; el mapa solo
 * contiene los overrides de pantallas con título dinámico (perfil o
 * medicamento en creación/edición). Guardarlos por ruta evita que el título
 * de una pantalla anterior se quede visible al volver a otra pestaña.
 *
 * @param currentRoute Ruta del destino visible.
 * @param statesByRoute Overrides publicados por cada destino.
 * @param fallback Estado por defecto de la ruta actual.
 */
internal fun resolveTopBarState(
    currentRoute: String?,
    statesByRoute: Map<String, TopBarState>,
    fallback: TopBarState
): TopBarState = currentRoute?.let(statesByRoute::get) ?: fallback

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