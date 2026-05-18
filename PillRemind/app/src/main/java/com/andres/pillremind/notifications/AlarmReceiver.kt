package com.andres.pillremind.notifications

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver encargado de recibir las alarmas programadas
 * y mostrar las notificaciones de recordatorio de medicación.
 *
 * Tras mostrar la notificación, la alarma se vuelve a programar
 * para la siguiente ocurrencia del horario.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("schedule_id", -1)
        if (scheduleId == -1) return

        // Ejecuta operaciones asíncronas para evitar bloquear el BroadcastReceiver
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val schedule: Schedule = scheduleRepository.getScheduleById(scheduleId)
                val medication = medicationRepository.getMedicationById(schedule.medicationId)
                    .firstOrNull() ?: return@launch

                showNotification(context, medication.name, schedule)

                // Reprogramar para la próxima semana (misma hora, mismo día)
                val next = nextTriggerMillis(schedule.weekDay, schedule.time)
                alarmScheduler.schedule(scheduleId, next)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Muestra una notificación de recordatorio para la toma de medicación.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, medicationName: String, schedule: Schedule) {
        val notification = NotificationCompat.Builder(context, "pill_remind_channel")
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("Hora de tomar $medicationName")
            .setContentText("${schedule.time} — no olvides tu dosis")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(schedule.id, notification)
    }
}