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
 * BroadcastReceiver encargado de marcar una toma de medicación como realizada
 * cuando el usuario pulsa la acción "Tomar" desde la notificación.
 *
 * Este receptor se ejecuta desde una PendingIntent asociada a la notificación
 * y delega la actualización de la base de datos en [MarkAsTakenHandler].
 */
@AndroidEntryPoint
class MarkAsTakenReceiver : BroadcastReceiver() {

    @Inject
    lateinit var handler: MarkAsTakenHandler

    override fun onReceive(context: Context, intent: Intent) {
        val intakeId = intent.getIntExtra("intake_id", -1)
        if (intakeId == -1) return

        // Permite continuar la ejecución de forma asíncrona fuera de onReceive()
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                handler.handle(intakeId)
            } finally {
                // Finaliza el procesamiento asíncrono del BroadcastReceiver
                pendingResult.finish()
            }
        }
    }
}
