package com.andres.pillremind.navigation

/**
 * Destinos de navegación relacionados con el historial de tomas.
 *
 * El historial es un detalle dentro del grafo principal, no una pestaña de la
 * barra inferior. Se abre desde el perfil o desde la ficha de un medicamento.
 *
 * @property route Ruta utilizada por Navigation Compose.
 */
sealed class HistoryScreens(val route: String) {
    /**
     * Pantalla de búsqueda y filtrado de tomas.
     *
     * El argumento `medicationId` es opcional; se usa `-1` cuando se abre
     * desde el perfil para no preseleccionar ningún medicamento.
     */
    object History : HistoryScreens("history?medicationId={medicationId}") {
        /**
         * Construye la ruta del historial.
         *
         * @param medicationId Medicamento por el que se filtra, o `null` para
         * mostrar todas las tomas del perfil.
         */
        fun createRoute(medicationId: Int? = null): String =
            "history?medicationId=${medicationId ?: -1}"
    }
}
