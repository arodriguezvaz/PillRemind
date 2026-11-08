package com.andres.pillremind.data.remote

import com.andres.pillremind.domain.model.CimaMedication
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Consulta la API pública de CIMA por Código Nacional.
 *
 * Solo se envía el CN; no se envían datos personales ni se persiste el
 * resultado. La API responde 204 cuando el CN no está catalogado.
 */
@Singleton
class CimaDataSource @Inject constructor() {

    /**
     * Devuelve el medicamento asociado al CN o `null` si no está catalogado.
     *
     * @throws IOException Si la API responde con un error distinto de 204/404.
     */
    suspend fun findByCn(cn: String): CimaMedication? = withContext(Dispatchers.IO) {
        val connection = (URL("$BASE_URL?cn=$cn").openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", USER_AGENT)
        }

        try {
            when (val code = connection.responseCode) {
                HttpURLConnection.HTTP_NO_CONTENT,
                HttpURLConnection.HTTP_NOT_FOUND -> null

                HttpURLConnection.HTTP_OK -> connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }
                    .let { body ->
                        if (body.isBlank() || body.length > MAX_BODY_CHARS) {
                            null
                        } else {
                            parse(body, cn)
                        }
                    }

                else -> throw IOException("HTTP $code")
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Convierte el JSON de CIMA en el modelo de dominio.
     *
     * Es `internal` para poder probar el mapeo sin red.
     */
    internal fun parse(body: String, cn: String): CimaMedication? {
        val dto = runCatching {
            json.decodeFromString<CimaMedicationDto>(body)
        }.getOrNull() ?: return null

        if (dto.nombre.isBlank()) return null

        val presentation = dto.presentaciones
            .firstOrNull { it.cn == cn }
            ?.nombre
            ?.ifBlank { null }
            ?: dto.nombre

        return CimaMedication(
            cn = cn,
            name = dto.nombre,
            dosis = dto.dosis.ifBlank { null },
            form = dto.formaFarmaceutica?.nombre?.ifBlank { null },
            presentation = presentation.ifBlank { null },
            retrievedAt = LocalDate.now()
        )
    }

    @Serializable
    private data class CimaMedicationDto(
        val nregistro: String = "",
        val nombre: String = "",
        val dosis: String = "",
        val formaFarmaceutica: CimaItemDto? = null,
        val presentaciones: List<CimaPresentationDto> = emptyList()
    )

    @Serializable
    private data class CimaItemDto(val nombre: String = "")

    @Serializable
    private data class CimaPresentationDto(
        val cn: String = "",
        val nombre: String = ""
    )

    private companion object {
        const val BASE_URL = "https://cima.aemps.es/cima/rest/medicamento"
        const val TIMEOUT_MS = 7_000
        const val MAX_BODY_CHARS = 128 * 1024
        const val USER_AGENT = "PillRemind"
        val json = Json { ignoreUnknownKeys = true }
    }
}
