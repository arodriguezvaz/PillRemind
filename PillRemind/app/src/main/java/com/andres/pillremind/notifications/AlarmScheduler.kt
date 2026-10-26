package com.andres.pillremind.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Clase encargada de programar y cancelar alarmas relacionadas
 * con los recordatorios de medicación.
 */
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /**
     * Programa una alarma en el sistema para ejecutar una acción en un momento concreto.
     *
     * En Android 12+ se comprueba si la app puede usar alarmas exactas. Si no tiene
     * permiso, se programa una alarma inexacta como fallback para no perder el aviso.
     * En versiones anteriores a Android 12 se programa directamente, porque el permiso
     * de alarmas exactas no existe y `canScheduleExactAlarms()` tampoco.
     *
     * @param intakeId Identificador de la toma asociada, usado para identificar la alarma.
     * @param triggerAtMillis Momento (en milisegundos) en el que debe dispararse la alarma.
     */
    fun schedule(intakeId: Int, triggerAtMillis: Long) {
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

        // canScheduleExactAlarms() solo existe desde API 31. En API 26-30 se
        // programa directamente para evitar un NoSuchMethodError.
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        if (canScheduleExact) {
            // Programa la alarma exacta, incluso en modo ahorro de energía.
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            // Fallback: la alarma puede retrasarse, pero el recordatorio no se pierde.
            Log.w(
                "AlarmScheduler",
                "Sin permiso de alarmas exactas; se programa alarma inexacta para intake $intakeId"
            )
            alarmManager.setAndAllowWhileIdle(
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