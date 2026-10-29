package com.andres.pillremind.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receptor de eventos del sistema que invalidan las alarmas de `AlarmManager`.
 *
 * Delega la reprogramación en [BootRescheduleHandler], que comprueba si la
 * acción recibida requiere regenerar la ventana de recordatorios.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var handler: BootRescheduleHandler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (!BootRescheduleHandler.isRescheduleAction(action)) {
            return
        }

        // El receiver debe terminar rápido; goAsync permite continuar el trabajo
        // asíncrono mientras se consulta Room y se programan las alarmas.
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                handler.handle(action)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
