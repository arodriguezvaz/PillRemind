package com.andres.pillremind.notifications

import android.content.Intent
import android.util.Log
import javax.inject.Inject

/**
 * Reprograma las alarmas de recordatorio cuando el sistema invalida las
 * existentes.
 *
 * `AlarmManager` no conserva las alarmas exactas después de un reinicio, por lo
 * que al arrancar el dispositivo hay que volver a generarlas. También se
 * reprograman cuando la app se actualiza, ya que el sistema puede cancelar las
 * alarmas del proceso anterior, y cuando el usuario concede el permiso de
 * alarmas exactas, para convertir las alarmas inexactas de fallback en exactas.
 *
 * La generación se delega en [ReminderScheduler.generateWindow], la misma fuente
 * de verdad que usan Home y el worker diario.
 *
 * @param reminderScheduler Coordina la generación de tomas y la programación de alarmas.
 */
class BootRescheduleHandler @Inject constructor(
    private val reminderScheduler: ReminderScheduler,
) {

    companion object {
        /**
         * Broadcast que envía el sistema al conceder el permiso de alarmas exactas.
         *
         * Se usa el literal porque la constante de `AlarmManager` solo existe
         * desde API 31 y el proyecto soporta API 26.
         */
        internal const val ACTION_EXACT_ALARM_PERMISSION_CHANGED =
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"

        /**
         * Indica si la acción recibida obliga a reprogramar los recordatorios.
         *
         * @param action Acción del broadcast recibido.
         */
        fun isRescheduleAction(action: String?): Boolean =
            action == Intent.ACTION_BOOT_COMPLETED ||
                action == Intent.ACTION_MY_PACKAGE_REPLACED ||
                action == ACTION_EXACT_ALARM_PERMISSION_CHANGED
    }

    /**
     * Reprograma la ventana de recordatorios si la acción recibida lo requiere.
     *
     * @param action Acción del broadcast recibido.
     */
    suspend fun handle(action: String?) {
        if (!isRescheduleAction(action)) return

        try {
            reminderScheduler.generateWindow()
        } catch (e: Exception) {
            Log.e("BootReceiver", "No se pudieron reprogramar los recordatorios", e)
        }
    }
}
