package com.andres.pillremind.data.backup

import com.andres.pillremind.data.backup.legacy.v1.BackupV1FileDto
import com.andres.pillremind.data.backup.legacy.v1.toCurrentBackup
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Serializa y deserializa el archivo `backup.json` de la copia de seguridad.
 *
 * La lectura acepta los formatos antiguos que todavía se pueden importar y los
 * convierte a la representación actual antes de validarlos.
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
     * Lee el JSON de la copia y lo migra al formato actual.
     *
     * @throws BackupException Si el texto no tiene la estructura esperada o la
     *   versión no está soportada.
     */
    fun decode(content: String): BackupFileDto {
        val envelope = try {
            json.decodeFromString<BackupEnvelopeDto>(content)
        } catch (e: SerializationException) {
            throw invalidFile(e)
        } catch (e: IllegalArgumentException) {
            throw invalidFile(e)
        }

        if (envelope.format != BackupFormat.NAME) {
            throw invalidFile()
        }

        return try {
            when (envelope.formatVersion) {
                1 -> json.decodeFromString<BackupV1FileDto>(content).toCurrentBackup()
                BackupFormat.VERSION -> json.decodeFromString<BackupFileDto>(content)
                else -> throw BackupException(
                    BackupError.UNSUPPORTED_VERSION,
                    "Versión de copia no compatible"
                )
            }
        } catch (e: BackupException) {
            throw e
        } catch (e: SerializationException) {
            throw invalidFile(e)
        } catch (e: IllegalArgumentException) {
            throw invalidFile(e)
        }
    }

    private fun invalidFile(cause: Throwable? = null) = BackupException(
        BackupError.INVALID_FILE,
        "El archivo no es una copia de seguridad válida",
        cause
    )

    /**
     * Metadatos mínimos para elegir el DTO de la versión correspondiente.
     */
    @Serializable
    private data class BackupEnvelopeDto(
        val format: String,
        val formatVersion: Int,
    )
}
