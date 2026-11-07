package com.andres.pillremind.domain.model

/**
 * Datos publicados de la última versión disponible de la aplicación.
 *
 * @property versionCode Código de versión de la release publicada.
 * @property versionName Nombre visible de la release.
 * @property minimumVersionCode Código mínimo que debe tener instalado el
 *   usuario para poder seguir usando la aplicación. El valor `0` desactiva el
 *   bloqueo obligatorio.
 * @property apkUrl URL directa de descarga del APK.
 * @property releaseUrl Página de la release con las notas de la versión.
 */
data class UpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val minimumVersionCode: Long,
    val apkUrl: String,
    val releaseUrl: String,
) {

    /**
     * Compara [installedVersionCode] con esta versión publicada.
     *
     * @param installedVersionCode Código de versión instalado actualmente.
     * @return El estado que debe aplicar la interfaz.
     */
    fun statusFor(installedVersionCode: Long): UpdateStatus = when {
        installedVersionCode < minimumVersionCode -> UpdateStatus.Required(this)
        installedVersionCode < versionCode -> UpdateStatus.Optional(this)
        else -> UpdateStatus.UpToDate
    }
}
