package com.andres.pillremind.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.repository.IntakeRepository
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
    lateinit var intakeRepository: IntakeRepository


    override fun onReceive(context: Context, intent: Intent) {
        val intakeId = intent.getIntExtra("intake_id", -1)
        if (intakeId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val intake = intakeRepository.getIntakeById(intakeId)

                // Si ya fue tomada no notificamos
                if (intake!!.takenAt != null) return@launch

                val schedule = scheduleRepository.getScheduleById(intake.scheduleId)
                val medication = medicationRepository.getMedicationById(schedule.medicationId)
                    .firstOrNull() ?: return@launch

                showNotification(context, intake, medication.name)
            } finally {
                pendingResult.finish()
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, intake: Intake, medicationName: String) {
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            intake.id,
            Intent(context, MarkAsTakenReceiver::class.java).apply {
                putExtra("intake_id", intake.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "pill_remind_channel")
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("Hora de tomar $medicationName")
            .setContentText("${intake.scheduledAt.toLocalTime()} — no olvides tu dosis")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_pill, "Tomar", takenPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(intake.id, notification)
    }
}
