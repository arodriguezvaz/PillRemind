package com.andres.pillremind.data.backup

/**
 * Categoría de error de una copia de seguridad.
 *
 * La UI traduce cada categoría a un recurso de texto propio.
 */
enum class BackupError {
    INVALID_FILE,
    UNSUPPORTED_VERSION,
    PASSWORD_REQUIRED,
    WRONG_PASSWORD,
    CORRUPTED,
    TOO_LARGE,
    MISSING_IMAGES,
    INVALID_DATA,
    IMAGES_ERROR,
    SAVE_ERROR,
    IO_ERROR,
    GENERIC
}

/**
 * Error controlado durante la exportación o importación de una copia de seguridad.
 *
 * El mensaje detallado se conserva para depuración; la UI muestra el texto
 * asociado a [error] desde `strings.xml`.
 *
 * @param error Categoría del fallo.
 * @param message Descripción técnica del problema.
 * @param cause Error original que provocó el fallo, si existe.
 */
class BackupException(
    val error: BackupError,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
