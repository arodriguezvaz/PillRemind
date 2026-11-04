package com.andres.pillremind.notifications

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Estados posibles del permiso de notificaciones.
 *
 * - [GRANTED]: la app puede mostrar notificaciones.
 * - [REQUESTABLE]: falta el permiso runtime y todavía se puede solicitar.
 * - [BLOCKED]: el usuario o el sistema han bloqueado las notificaciones; solo
 *   se pueden volver a activar desde los ajustes.
 */
enum class NotificationStatus { GRANTED, REQUESTABLE, BLOCKED }

/**
 * Acción que debe ejecutar la UI para resolver el permiso de notificaciones.
 */
enum class NotificationAction { REQUEST, OPEN_SETTINGS, NONE }

/**
 * Estados posibles del permiso de alarmas exactas.
 *
 * - [GRANTED]: la app puede programar alarmas exactas.
 * - [PENDING]: el permiso existe, pero no está concedido.
 * - [NOT_REQUIRED]: la versión de Android no necesita este permiso.
 * - [UNAVAILABLE]: el dispositivo no ofrece una pantalla para concederlo.
 */
enum class ExactAlarmStatus { GRANTED, PENDING, NOT_REQUIRED, UNAVAILABLE }

/**
 * Permiso de notificaciones pospuesto en Android 13.
 *
 * Se declara como constante para poder consultarlo también en versiones
 * anteriores sin referenciar directamente un campo que solo existe desde API 33.
 */
const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"

/**
 * Consulta el estado de los permisos que necesitan los recordatorios.
 *
 * Centraliza las comprobaciones que antes vivían en `SettingsScreen` para que la
 * bienvenida y los ajustes usen exactamente la misma lógica.
 *
 * @property context Contexto de aplicación usado para consultar el sistema.
 */
class ReminderPermissions @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Comprueba si la app puede mostrar notificaciones.
     *
     * Tiene en cuenta el permiso runtime de Android 13, las notificaciones
     * desactivadas a nivel de app y el canal de recordatorios bloqueado.
     */
    fun notificationStatus(): NotificationStatus {
        val manager = NotificationManagerCompat.from(context)
        val permissionGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS_PERMISSION) ==
                PackageManager.PERMISSION_GRANTED
        }
        val channelBlocked = manager.getNotificationChannel(NotificationChannels.REMINDERS)
            ?.importance == NotificationManagerCompat.IMPORTANCE_NONE

        return resolveNotificationStatus(
            permissionGranted = permissionGranted,
            notificationsEnabled = manager.areNotificationsEnabled(),
            channelBlocked = channelBlocked,
        )
    }

    /**
     * Comprueba si la app puede programar alarmas exactas.
     */
    fun exactAlarmStatus(): ExactAlarmStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return ExactAlarmStatus.NOT_REQUIRED
        }
        if (canScheduleExactAlarms()) {
            return ExactAlarmStatus.GRANTED
        }
        return if (exactAlarmSettingsIntent(context) != null) {
            ExactAlarmStatus.PENDING
        } else {
            ExactAlarmStatus.UNAVAILABLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun canScheduleExactAlarms(): Boolean =
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
}

/**
 * Calcula el estado de notificaciones a partir de los datos del sistema.
 *
 * Es una función pura para poder probar todos los casos sin Android.
 */
internal fun resolveNotificationStatus(
    permissionGranted: Boolean,
    notificationsEnabled: Boolean,
    channelBlocked: Boolean,
): NotificationStatus = when {
    !permissionGranted -> NotificationStatus.REQUESTABLE
    !notificationsEnabled || channelBlocked -> NotificationStatus.BLOCKED
    else -> NotificationStatus.GRANTED
}

/**
 * Decide si la UI debe solicitar el permiso o abrir los ajustes del sistema.
 *
 * @param status Estado actual del permiso.
 * @param requested Indica si ya se ha lanzado la petición al menos una vez.
 * @param showRationale Resultado de `shouldShowRequestPermissionRationale`.
 */
internal fun resolveNotificationAction(
    status: NotificationStatus,
    requested: Boolean,
    showRationale: Boolean,
): NotificationAction = when (status) {
    NotificationStatus.GRANTED -> NotificationAction.NONE
    NotificationStatus.BLOCKED -> NotificationAction.OPEN_SETTINGS
    NotificationStatus.REQUESTABLE ->
        if (requested && !showRationale) {
            NotificationAction.OPEN_SETTINGS
        } else {
            NotificationAction.REQUEST
        }
}

/**
 * Indica si Android todavía puede mostrar el diálogo de permiso de
 * notificaciones. Antes de la primera petición devuelve `false`, por eso la UI
 * combina este dato con [resolveNotificationAction].
 */
fun shouldShowNotificationRationale(activity: Activity?): Boolean =
    activity != null &&
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            POST_NOTIFICATIONS_PERMISSION,
        )

/**
 * Abre los ajustes de notificaciones de la app.
 */
fun notificationSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

/**
 * Devuelve el intent para conceder alarmas exactas o `null` si el dispositivo
 * no expone esa pantalla.
 */
@RequiresApi(Build.VERSION_CODES.S)
fun exactAlarmSettingsIntent(context: Context): Intent? {
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, context.packageUri())
    return intent.takeIf { it.resolveActivity(context.packageManager) != null }
}

/**
 * Abre la ficha de la app en los ajustes del sistema.
 *
 * Se usa como alternativa cuando el fabricante no ofrece la pantalla de
 * alarmas exactas.
 */
fun appDetailsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, context.packageUri())

private fun Context.packageUri(): Uri = "package:$packageName".toUri()
