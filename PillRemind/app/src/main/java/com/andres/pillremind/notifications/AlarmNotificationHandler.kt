package com.andres.pillremind.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.andres.pillremind.MainActivity
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Procesa las alarmas programadas y muestra las notificaciones de recordatorio
 * de medicación.
 *
 * Tras procesar el aviso, repone la ventana rodante de recordatorios. Así una
 * alarma disparada también sirve como mecanismo de auto-recuperación si el
 * worker diario se ha retrasado o ha fallado.
 *
 * @param context Contexto utilizado para crear y mostrar la notificación.
 * @param intakeRepository Repositorio de tomas.
 * @param medicationRepository Repositorio de medicamentos.
 * @param profileRepository Repositorio de perfiles.
 * @param reminderScheduler Coordina la generación de tomas y la programación de alarmas.
 */
class AlarmNotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intakeRepository: IntakeRepository,
    private val medicationRepository: MedicationRepository,
    private val profileRepository: ProfileRepository,
    private val reminderScheduler: ReminderScheduler,
) {

    /**
     * Comprueba la toma asociada a la alarma y muestra su notificación si procede.
     *
     * @param intakeId Identificador de la toma que ha disparado la alarma.
     */
    suspend fun handle(intakeId: Int) {
        try {
            val intake = intakeRepository.getIntakeById(intakeId)

            // Si la toma ya no existe o ya fue registrada, no se muestra el aviso.
            if (intake != null && intake.takenAt == null) {
                // Recupera el medicamento y el perfil asociados a la toma programada.
                // Si alguno de ellos no existe, se omite la notificación para evitar
                // mostrar información incompleta.
                val medication = medicationRepository.getMedicationByIdOnce(intake.medicationId)
                val profile = medication?.let {
                    profileRepository.getProfileByIdOnce(it.profileId)
                }

                // Muestra la notificación únicamente si el medicamento sigue
                // activo, el perfil existe y el permiso ha sido concedido.
                if (medication != null && medication.deletedAt == null && profile != null &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showNotification(intake, medication.name, profile.name)
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error procesando la alarma de la toma $intakeId", e)
        } finally {
            // Una alarma disparada también repone la ventana rodante. Así, aunque
            // el worker diario se retrase o falle, los próximos días siguen cubiertos.
            runCatching { reminderScheduler.generateWindow() }
                .onFailure {
                    Log.e(
                        "AlarmReceiver",
                        "No se pudo refrescar la ventana de recordatorios",
                        it
                    )
                }
        }
    }

    /**
     * Genera y muestra una notificación recordando al usuario que debe realizar
     * una toma de medicación.
     *
     * La notificación abre la app al tocar el cuerpo y incluye una acción rápida
     * que permite marcar la toma como realizada directamente desde el panel de
     * notificaciones mediante un [PendingIntent] asociado a [MarkAsTakenReceiver].
     *
     * @param intake Toma programada asociada a la notificación.
     * @param medicationName Nombre del medicamento que debe tomarse.
     * @param profileName Nombre del perfil al que pertenece el tratamiento.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        intake: Intake,
        medicationName: String,
        profileName: String
    ) {
        // Abre la app al tocar el cuerpo de la notificación.
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            intake.id,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle(context.getString(R.string.notification_title, medicationName))
            .setContentText(
                context.getString(
                    R.string.notification_text,
                    intake.scheduledAt.toLocalTime().toString(),
                    profileName
                )
            )
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_pill,
                context.getString(R.string.notification_action_take),
                takenPendingIntent
            )
            .build()

        NotificationManagerCompat.from(context).notify(intake.id, notification)
    }
}
