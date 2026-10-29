package com.andres.pillremind.notifications

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.andres.pillremind.domain.repository.IntakeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.time.LocalDateTime

/**
 * Marca una toma de medicación como realizada cuando el usuario pulsa la acción
 * "Tomar" desde la notificación.
 *
 * También elimina la notificación asociada una vez registrada la toma.
 *
 * @param context Contexto utilizado para cancelar la notificación.
 * @param intakeRepository Repositorio de tomas.
 */
class MarkAsTakenHandler @Inject constructor(
    @ApplicationContext private val context: Context,
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
            NotificationManagerCompat.from(context).cancel(intakeId)
        } catch (e: Exception) {
            Log.e("PillDebug", "MarkAsTaken - Error", e)
        }
    }
}
