package com.andres.pillremind.data.remote

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cliente HTTP mínimo para descargar el manifiesto de actualizaciones.
 *
 * No se usa una librería de red completa porque solo hace falta una petición
 * GET a un fichero estático. Las peticiones llevan un User-Agent genérico, sin
 * identificadores de la aplicación ni del usuario.
 */
class UrlConnectionHttpTextFetcher @Inject constructor() : HttpTextFetcher {

    override suspend fun fetch(url: String): String = withContext(Dispatchers.IO) {
        val connection = (URI.create(url).toURL().openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            requestMethod = "GET"
            instanceFollowRedirects = true
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Cache-Control", "no-cache")
            setRequestProperty("User-Agent", USER_AGENT)
        }

        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            if (connection.contentLengthLong > MAX_RESPONSE_CHARS) {
                throw IOException("Respuesta de actualización demasiado grande")
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            if (body.length > MAX_RESPONSE_CHARS) {
                throw IOException("Respuesta de actualización demasiado grande")
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        /** Tiempo máximo de conexión y lectura de la comprobación. */
        const val TIMEOUT_MS = 5_000

        /** Tamaño máximo aceptado para el manifiesto. */
        const val MAX_RESPONSE_CHARS = 64 * 1024

        /** Identificador genérico; no incluye la versión instalada. */
        const val USER_AGENT = "PillRemind-UpdateCheck"
    }
}
