package com.andres.pillremind.data.remote

import com.andres.pillremind.domain.model.UpdateInfo
import java.net.URI
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Descarga y valida el manifiesto de actualizaciones publicado en GitHub
 * Releases.
 *
 * El manifiesto solo puede apuntar a ficheros del propio repositorio y siempre
 * por HTTPS. Así un JSON manipulado no puede abrir una URL arbitraria.
 */
class GitHubUpdateDataSource @Inject constructor(
    private val fetcher: HttpTextFetcher,
) {

    /**
     * Descarga el manifiesto de la última release.
     *
     * @throws UpdateFormatException Si el contenido no es válido.
     * @throws java.io.IOException Si falla la conexión.
     */
    suspend fun fetchLatest(): UpdateInfo = parse(fetcher.fetch(MANIFEST_URL))

    /**
     * Valida y convierte el cuerpo del manifiesto.
     *
     * @param body Contenido JSON publicado por el workflow de release.
     */
    internal fun parse(body: String): UpdateInfo {
        val payload = try {
            json.decodeFromString<UpdatePayload>(body)
        } catch (e: SerializationException) {
            throw UpdateFormatException("JSON de actualización inválido", e)
        } catch (e: IllegalArgumentException) {
            throw UpdateFormatException("JSON de actualización inválido", e)
        }

        val versionName = payload.versionName.trim()
        if (payload.versionCode < 1) {
            throw UpdateFormatException("versionCode inválido")
        }
        if (payload.minimumVersionCode !in 0..payload.versionCode) {
            throw UpdateFormatException("minimumVersionCode inválido")
        }
        if (versionName.isEmpty() || versionName.length > MAX_VERSION_NAME_LENGTH) {
            throw UpdateFormatException("versionName inválido")
        }
        validateRepositoryUrl(payload.apkUrl, APK_PATH_MARKER)
        validateRepositoryUrl(payload.releaseUrl, RELEASE_PATH_MARKER)

        return UpdateInfo(
            versionCode = payload.versionCode,
            versionName = versionName,
            minimumVersionCode = payload.minimumVersionCode,
            apkUrl = payload.apkUrl,
            releaseUrl = payload.releaseUrl,
        )
    }

    /**
     * Comprueba que la URL sea HTTPS y pertenezca a las releases del
     * repositorio oficial.
     */
    private fun validateRepositoryUrl(url: String, pathMarker: String) {
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            throw UpdateFormatException("URL de actualización inválida", e)
        }

        if (uri.scheme != "https" ||
            uri.host != GITHUB_HOST ||
            uri.userInfo != null ||
            uri.port != -1
        ) {
            throw UpdateFormatException("URL de actualización no permitida")
        }

        val path = uri.path.orEmpty()
        if (!path.startsWith(REPOSITORY_RELEASES_PATH) || !path.contains(pathMarker)) {
            throw UpdateFormatException("URL de actualización no permitida")
        }
    }

    /**
     * Estructura exacta del manifiesto publicado por el workflow.
     */
    @Serializable
    private data class UpdatePayload(
        val versionCode: Long,
        val versionName: String,
        val minimumVersionCode: Long = 0,
        val apkUrl: String,
        val releaseUrl: String,
    )

    private companion object {
        /** URL estable del manifiesto adjunto a la última release. */
        const val MANIFEST_URL =
            "https://github.com/arodriguezvaz/PillRemind/releases/latest/download/update.json"

        const val GITHUB_HOST = "github.com"
        const val REPOSITORY_RELEASES_PATH = "/arodriguezvaz/PillRemind/releases/"
        const val APK_PATH_MARKER = "/download/"
        const val RELEASE_PATH_MARKER = "/tag/"
        const val MAX_VERSION_NAME_LENGTH = 32

        val json = Json { ignoreUnknownKeys = true }
    }
}

/**
 * El manifiesto remoto no tiene el formato esperado.
 */
class UpdateFormatException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
