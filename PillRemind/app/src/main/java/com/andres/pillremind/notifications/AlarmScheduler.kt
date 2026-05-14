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
     * Programa una alarma exacta para un horario concreto.
     *
     * @param scheduleId Identificador del horario asociado.
     * @param triggerAtMillis Momento en milisegundos en el que debe ejecutarse la alarma.
     */
    fun schedule(scheduleId: Int, triggerAtMillis: Long) {
        if (alarmManager.canScheduleExactAlarms()) {

            // Intent utilizado por el BroadcastReceiver para mostrar la notificación
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("schedule_id", scheduleId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancela la alarma asociada a un horario.
     * @param scheduleId Identificador del horario cuya alarma se desea cancelar.
     */
    fun cancel(scheduleId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}