package com.andres.pillremind.notifications

import android.util.Log
import com.andres.pillremind.domain.repository.IntakeRepository
import javax.inject.Inject
import java.time.LocalDateTime

/**
 * Marca una toma de medicación como realizada cuando el usuario pulsa la acción
 * "Tomar" desde la notificación.
 *
 * También elimina la notificación asociada una vez registrada la toma.
 *
 * @param intakeRepository Repositorio de tomas.
 * @param notifications Gestor de notificaciones de recordatorio.
 */
class MarkAsTakenHandler @Inject constructor(
    private val intakeRepository: IntakeRepository,
    private val notifications: ReminderNotifications,
) {

    /**
     * Registra la toma indicada y cancela su notificación.
     *
     * @param intakeId Identificador de la toma marcada como realizada.
     */
    suspend fun handle(intakeId: Int) {
        if (intakeId == -1) return

        try {
            // Actualiza únicamente takenAt para no tocar los snapshots de la toma.
            intakeRepository.markTaken(intakeId, LocalDateTime.now())

            // Elimina la notificación una vez marcada como tomada
            notifications.cancel(intakeId)
        } catch (e: Exception) {
            Log.e("PillDebug", "MarkAsTaken - Error", e)
        }
    }
}
