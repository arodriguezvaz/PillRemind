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

    /**
     * Estadísticas limitadas a un medicamento concreto.
     *
     * Se abre desde la ficha del medicamento y reutiliza la pantalla global
     * filtrando el informe por el identificador recibido.
     */
    object MedicationStatistics : StatisticsScreens("statistics/medication/{medicationId}") {
        /**
         * Construye la ruta para el medicamento indicado.
         *
         * @param medicationId Identificador del medicamento.
         */
        fun createRoute(medicationId: Int) = "statistics/medication/$medicationId"
    }
}
