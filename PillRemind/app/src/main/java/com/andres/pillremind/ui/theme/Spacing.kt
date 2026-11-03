package com.andres.pillremind.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Escala de espaciado de la aplicación.
 *
 * Los valores siguen una escala de 4dp y cubren los huecos y rellenos del
 * layout. Los tamaños de componentes (iconos, imágenes, botones) no se
 * incluyen aquí: viven en cada componente.
 */
object AppSpacing {
    /** Huecos mínimos, como el espacio interno de un icono. */
    val xxs = 4.dp

    /** Etiqueta y campo, chips y grupos relacionados. */
    val xs = 8.dp

    /** Entre tarjetas de una lista y entre campos de un formulario. */
    val sm = 12.dp

    /** Margen de pantalla y relleno interno de tarjetas y contenedores. */
    val md = 16.dp

    /** Separación entre secciones de formulario. */
    val lg = 20.dp

    /** Separación bajo la foto de un formulario. */
    val xl = 24.dp

    /** Alto visual de un estado vacío. */
    val xxl = 32.dp
}
