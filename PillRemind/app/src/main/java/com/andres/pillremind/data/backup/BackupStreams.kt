package com.andres.pillremind.data.backup

import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Tamaño máximo del payload comprimido que se acepta al importar.
 *
 * Es algo mayor que el límite del ZIP descomprimido para dejar sitio a la
 * cabecera, al cifrado y a la propia estructura del ZIP, pero evita cargar en
 * memoria un archivo arbitrariamente grande.
 */
internal const val MAX_BACKUP_PAYLOAD_BYTES = 64L * 1024 * 1024

/**
 * Lee un flujo completo sin superar [maxBytes].
 *
 * @throws BackupException Si el flujo contiene más bytes de los permitidos.
 */
internal fun InputStream.readBytesLimited(maxBytes: Long): ByteArray {
    if (maxBytes <= 0) {
        throw BackupException(BackupError.TOO_LARGE, "La copia es demasiado grande")
    }

    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var total = 0L

    while (true) {
        val read = read(buffer)
        if (read == -1) break

        total += read
        if (total > maxBytes) {
            throw BackupException(BackupError.TOO_LARGE, "La copia es demasiado grande")
        }
        output.write(buffer, 0, read)
    }

    return output.toByteArray()
}
