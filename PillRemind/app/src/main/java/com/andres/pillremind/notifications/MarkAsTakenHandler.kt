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
 * @param intakeNotifications Cancela la notificación asociada a la toma.
 * @param intakeRepository Repositorio de tomas.
 */
class MarkAsTakenHandler @Inject constructor(
    private val intakeNotifications: IntakeNotifications,
    private val intakeRepository: IntakeRepository,
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
            intakeNotifications.cancel(intakeId)
        } catch (e: Exception) {
            Log.e("PillDebug", "MarkAsTaken - Error", e)
        }
    }
}
