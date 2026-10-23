package com.andres.pillremind.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receptor de eventos del sistema que invalidan las alarmas de `AlarmManager`.
 *
 * `AlarmManager` no conserva las alarmas exactas después de un reinicio, por lo
 * que al arrancar el dispositivo hay que volver a generarlas. También se
 * reprograman cuando la app se actualiza, ya que el sistema puede cancelar las
 * alarmas del proceso anterior, y cuando el usuario concede el permiso de
 * alarmas exactas, para convertir las alarmas inexactas de fallback en exactas.
 *
 * La generación se delega en [ReminderScheduler.generateWindow], la misma fuente
 * de verdad que usan Home y el worker diario.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    companion object {
        /**
         * Broadcast que envía el sistema al conceder el permiso de alarmas exactas.
         *
         * Se usa el literal porque la constante de `AlarmManager` solo existe
         * desde API 31 y el proyecto soporta API 26.
         */
        private const val ACTION_EXACT_ALARM_PERMISSION_CHANGED =
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
    }

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != ACTION_EXACT_ALARM_PERMISSION_CHANGED
        ) {
            return
        }

        // El receiver debe terminar rápido; goAsync permite continuar el trabajo
        // asíncrono mientras se consulta Room y se programan las alarmas.
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                reminderScheduler.generateWindow()
            } catch (e: Exception) {
                Log.e("BootReceiver", "No se pudieron reprogramar los recordatorios", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
