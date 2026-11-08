package com.andres.pillremind.domain.model

/**
 * Resultado de comparar la versión instalada con la versión publicada.
 */
sealed interface UpdateStatus {

    /**
     * La versión instalada está por debajo del mínimo y la aplicación debe
     * bloquearse hasta que se actualice.
     *
     * @property info Información de la versión publicada.
     */
    data class Required(val info: UpdateInfo) : UpdateStatus

    /**
     * Hay una versión nueva, pero la instalada todavía es válida.
     *
     * @property info Información de la versión publicada.
     */
    data class Optional(val info: UpdateInfo) : UpdateStatus

    /**
     * La versión instalada ya está al día.
     */
    data object UpToDate : UpdateStatus
}
