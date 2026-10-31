package com.andres.pillremind.data.backup

/**
 * Error controlado durante la exportación o importación de una copia de seguridad.
 *
 * El mensaje está pensado para mostrarse directamente al usuario.
 *
 * @param message Descripción legible del problema.
 * @param cause Error original que provocó el fallo, si existe.
 */
class BackupException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
