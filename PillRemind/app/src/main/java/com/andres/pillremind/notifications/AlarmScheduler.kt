package com.andres.pillremind.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

/**
 * Clase encargada de programar y cancelar alarmas relacionadas
 * con los recordatorios de medicación.
 */
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /**
     * Programa una alarma exacta en el sistema para ejecutar una acción en un momento concreto.
     *
     * Se utiliza [AlarmManager] para garantizar la ejecución,
     * siempre que el dispositivo lo permita mediante permisos de alarmas exactas.
     *
     * @param intakeId Identificador de la toma asociada, usado para identificar la alarma.
     * @param triggerAtMillis Momento exacto (en milisegundos) en el que debe dispararse la alarma.
     */
    fun schedule(intakeId: Int, triggerAtMillis: Long) {
        // Verifica si la app tiene permiso para programar alarmas exactas.
        if (alarmManager.canScheduleExactAlarms()) {
            // Intent que se ejecutará cuando se dispare la alarma.
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("intake_id", intakeId)
            }

            // PendingIntent que permite a AlarmManager ejecutar el BroadcastReceiver
            // incluso si la aplicación no está en ejecución.
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                intakeId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // Programa la alarma exacta, incluso en modo ahorro de energía
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancela la alarma asociada a un horario.
     * @param intakeId Identificador del horario cuya alarma se desea cancelar.
     */
    fun cancel(intakeId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            intakeId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}