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
 * BroadcastReceiver encargado de recibir las alarmas programadas y delegar el
 * procesamiento de la notificación en [AlarmNotificationHandler].
 *
 * Tras procesar el aviso, el handler repone la ventana rodante de recordatorios.
 * Así una alarma disparada también sirve como mecanismo de auto-recuperación si
 * el worker diario se ha retrasado o ha fallado.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var handler: AlarmNotificationHandler

    override fun onReceive(context: Context, intent: Intent) {
        // Obtiene el identificador de la toma asociada a la alarma.
        val intakeId = intent.getIntExtra("intake_id", -1)
        if (intakeId == -1) return

        // Permite que el BroadcastReceiver continúe ejecutando tareas asíncronas
        // después de que onReceive() haya finalizado. Esto es necesario porque las
        // consultas a la base de datos se realizan en una corrutina y pueden tardar
        // más tiempo del que Android espera para la ejecución de un BroadcastReceiver.
        // El procesamiento se considera completado cuando se invoca pendingResult.finish()
        val pendingResult = goAsync()

        // CoroutineScope permite crear un contexto desde el que lanzar corrutinas.
        // Dispatchers.IO utiliza un conjunto de hilos optimizado para operaciones
        // de entrada/salida, como consultas a bases de datos.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handler.handle(intakeId)
            } finally {
                // Finaliza el procesamiento asíncrono del BroadcastReceiver.
                pendingResult.finish()
            }
        }
    }
}
