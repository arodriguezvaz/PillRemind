package com.andres.pillremind.navigation

/**
 * Destinos de navegación relacionados con las estadísticas.
 *
 * La pantalla es un detalle dentro del grafo principal, no una pestaña de la
 * barra inferior.
 *
 * @property route Ruta utilizada por Navigation Compose.
 */
sealed class StatisticsScreens(val route: String) {
    /**
     * Pantalla de resumen, gráfico, desglose e historial de tomas.
     */
    object Statistics : StatisticsScreens("statistics")
}
