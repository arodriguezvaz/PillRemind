package com.andres.pillremind.data.backup

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Serializa y deserializa el archivo `backup.json` de la copia de seguridad.
 */
object BackupSerializer {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /**
     * Convierte el contenido de la copia en texto JSON.
     */
    fun encode(file: BackupFileDto): String = json.encodeToString(file)

    /**
     * Lee el JSON de la copia.
     *
     * @throws BackupException Si el texto no tiene la estructura esperada.
     */
    fun decode(content: String): BackupFileDto =
        try {
            json.decodeFromString<BackupFileDto>(content)
        } catch (e: SerializationException) {
            throw BackupException("El archivo no es una copia de seguridad válida", e)
        } catch (e: IllegalArgumentException) {
            throw BackupException("El archivo no es una copia de seguridad válida", e)
        }
}
