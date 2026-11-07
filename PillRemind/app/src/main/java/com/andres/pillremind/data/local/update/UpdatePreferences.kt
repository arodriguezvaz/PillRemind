package com.andres.pillremind.data.local.update

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.andres.pillremind.domain.model.UpdateInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// DataStore propio para no mezclar las preferencias de actualización con las
// de sesión. El nombre es estable porque los datos sobreviven a las versiones.
private val Context.updateDataStore: DataStore<Preferences> by
    preferencesDataStore(
        name = "update_prefs",
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    )

/**
 * Guarda en el dispositivo la información de la última actualización conocida.
 *
 * La caché permite aplicar un bloqueo obligatorio aunque después no haya
 * conexión. Si el contenido guardado no se puede leer, se ignora: es preferible
 * dejar usar la aplicación antes que bloquearla por un dato corrupto.
 */
@Singleton
class UpdatePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val CACHED_INFO = stringPreferencesKey("cached_info")
        val LAST_CHECK_AT = longPreferencesKey("last_check_at")
        val DISMISSED_VERSION_CODE = longPreferencesKey("dismissed_version_code")
    }

    /**
     * Última información válida guardada.
     */
    val cachedInfo: Flow<UpdateInfo?> = context.updateDataStore.data.map { preferences ->
        preferences[Keys.CACHED_INFO]?.toUpdateInfo()
    }

    /**
     * Momento del último intento de comprobación.
     */
    val lastCheckAt: Flow<Long?> = context.updateDataStore.data.map { preferences ->
        preferences[Keys.LAST_CHECK_AT]
    }

    /**
     * Versión que el usuario decidió posponer.
     */
    val dismissedVersionCode: Flow<Long?> = context.updateDataStore.data.map { preferences ->
        preferences[Keys.DISMISSED_VERSION_CODE]
    }

    /**
     * Guarda la información remota y la marca temporal en una sola escritura.
     */
    suspend fun saveCheck(info: UpdateInfo, checkedAt: Long) {
        context.updateDataStore.edit { preferences ->
            preferences[Keys.CACHED_INFO] = CachedUpdate.from(info).toJson()
            preferences[Keys.LAST_CHECK_AT] = checkedAt
        }
    }

    /**
     * Guarda el momento de un intento de comprobación aunque haya fallado.
     *
     * Se usa para no repetir la petición automática en cada arranque y para
     * poder afirmar que la consulta automática es como máximo diaria.
     */
    suspend fun saveLastCheckAt(checkedAt: Long) {
        context.updateDataStore.edit { preferences ->
            preferences[Keys.LAST_CHECK_AT] = checkedAt
        }
    }

    /**
     * Guarda la versión que el usuario no quiere volver a ver.
     */
    suspend fun dismiss(versionCode: Long) {
        context.updateDataStore.edit { preferences ->
            preferences[Keys.DISMISSED_VERSION_CODE] = versionCode
        }
    }

    /**
     * Borra la versión pospuesta para que el aviso pueda volver a mostrarse.
     */
    suspend fun clearDismissal() {
        context.updateDataStore.edit { preferences ->
            preferences.remove(Keys.DISMISSED_VERSION_CODE)
        }
    }

    private fun String.toUpdateInfo(): UpdateInfo? = try {
        json.decodeFromString<CachedUpdate>(this).toDomain()
    } catch (e: Exception) {
        null
    }

    private fun CachedUpdate.toJson(): String = json.encodeToString(this)

    /**
     * Representación serializable de [UpdateInfo].
     *
     * Se usa una clase propia de la capa de datos para no acoplar el modelo de
     * dominio a kotlinx.serialization.
     */
    @Serializable
    private data class CachedUpdate(
        val versionCode: Long,
        val versionName: String,
        val minimumVersionCode: Long,
        val apkUrl: String,
        val releaseUrl: String,
    ) {
        fun toDomain(): UpdateInfo = UpdateInfo(
            versionCode = versionCode,
            versionName = versionName,
            minimumVersionCode = minimumVersionCode,
            apkUrl = apkUrl,
            releaseUrl = releaseUrl,
        )

        companion object {
            fun from(info: UpdateInfo): CachedUpdate = CachedUpdate(
                versionCode = info.versionCode,
                versionName = info.versionName,
                minimumVersionCode = info.minimumVersionCode,
                apkUrl = info.apkUrl,
                releaseUrl = info.releaseUrl,
            )
        }
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
