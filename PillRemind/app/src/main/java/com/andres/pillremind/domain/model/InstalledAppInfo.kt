package com.andres.pillremind.domain.model

/**
 * Información de la versión de PillRemind instalada en el dispositivo.
 *
 * @property versionCode Código de versión instalado.
 * @property versionName Nombre de versión instalado.
 * @property isDebuggable Indica si es una compilación de depuración. Las
 *   compilaciones locales no se bloquean para no inutilizar el desarrollo.
 */
data class InstalledAppInfo(
    val versionCode: Long,
    val versionName: String,
    val isDebuggable: Boolean,
)
