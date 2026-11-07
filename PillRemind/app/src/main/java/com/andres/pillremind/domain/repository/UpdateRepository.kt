package com.andres.pillremind.domain.repository

import com.andres.pillremind.domain.model.InstalledAppInfo
import com.andres.pillremind.domain.model.UpdateInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de comprobación de actualizaciones.
 *
 * La información remota se cachea en el dispositivo para que el bloqueo
 * obligatorio siga aplicándose aunque después no haya conexión.
 */
interface UpdateRepository {

    /**
     * Última información de actualización guardada en el dispositivo.
     */
    val cachedInfo: Flow<UpdateInfo?>

    /**
     * Versión que el usuario decidió posponer.
     */
    val dismissedVersionCode: Flow<Long?>

    /**
     * Momento del último intento de comprobación, en milisegundos.
     */
    val lastCheckAt: Flow<Long?>

    /**
     * Consulta el manifiesto remoto y guarda el resultado si es válido.
     *
     * Si la consulta falla, la información anterior se conserva y el error se
     * devuelve para que la interfaz pueda decidir si lo muestra.
     *
     * @return La información publicada o el error de red o formato.
     */
    suspend fun refresh(): Result<UpdateInfo>

    /**
     * Guarda que el usuario pospuso la versión indicada.
     *
     * @param versionCode Versión que no debe volver a avisarse.
     */
    suspend fun dismiss(versionCode: Long)

    /**
     * Olvida la versión pospuesta para que vuelva a mostrarse el aviso.
     *
     * Se usa cuando el usuario pide una comprobación manual.
     */
    suspend fun clearDismissal()

    /**
     * Lee la información de la versión instalada en el dispositivo.
     */
    fun installedAppInfo(): InstalledAppInfo
}
