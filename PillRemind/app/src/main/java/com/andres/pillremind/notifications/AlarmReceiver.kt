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
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver encargado de recibir las alarmas programadas y mostrar las notificaciones
 * de recordatorio de medicación.
 *
 * Tras mostrar la notificación, la alarma se vuelve a programar para la siguiente ocurrencia
 * del horario.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Inject
    lateinit var intakeRepository: IntakeRepository

    @Inject
    lateinit var profileRepository: ProfileRepository

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
                val intake = intakeRepository.getIntakeById(intakeId)

                // Si ya fue tomada no notificamos
                if (intake!!.takenAt != null) return@launch

                val schedule = scheduleRepository.getScheduleByIdOnce(intake.scheduleId)
                // Recupera el medicamento y el perfil asociados a la toma programada.
                // Si alguno de ellos no existe, se cancela el procesamiento para evitar
                // generar una notificación con información incompleta.
                val medication =
                    medicationRepository.getMedicationByIdOnce(schedule.medicationId)
                        ?: return@launch
                val profile =
                    profileRepository.getProfileByIdOnce(medication.profileId)
                        ?: return@launch

                // Muestra la notificación únicamente si el permiso ha sido concedido.
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    showNotification(context, intake, medication.name, profile.name)
                }
            } finally {
                // Finaliza el procesamiento asíncrono del BroadcastReceiver.
                pendingResult.finish()
            }
        }
    }

    /**
     * Genera y muestra una notificación recordando al usuario que debe realizar
     * una toma de medicación.
     *
     * La notificación incluye una acción rápida que permite marcar la toma como
     * realizada directamente desde el panel de notificaciones mediante un
     * [PendingIntent] asociado a [MarkAsTakenReceiver].
     *
     * @param context Contexto utilizado para crear y mostrar la notificación.
     * @param intake Toma programada asociada a la notificación.
     * @param medicationName Nombre del medicamento que debe tomarse.
     * @param profileName Nombre del perfil al que pertenece el tratamiento.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        context: Context,
        intake: Intake,
        medicationName: String,
        profileName: String
    ) {
        // Se utiliza un PendingIntent para permitir que la acción de la notificación
        // ejecute un BroadcastReceiver incluso cuando la aplicación no se encuentra abierta,
        // posibilitando marcar una toma como realizada directamente desde la notificación.
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            intake.id,
            Intent(context, MarkAsTakenReceiver::class.java).apply {
                putExtra("intake_id", intake.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Construye la notificación mostrando la información de la toma pendiente
        // e incorpora una acción rápida para registrar la medicación como tomada.
        val notification = NotificationCompat.Builder(context, "pill_remind_channel")
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("Hora de tomar $medicationName")
            .setContentText("${intake.scheduledAt.toLocalTime()} — no olvides la dosis de $profileName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_pill, "Tomar", takenPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(intake.id, notification)
    }
}
