package com.andres.pillremind.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.andres.pillremind.domain.repository.IntakeRepository
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * BroadcastReceiver encargado de marcar una toma de medicación como realizada
 * cuando el usuario pulsa la acción "Tomar" desde la notificación.
 *
 * Este receptor se ejecuta desde una PendingIntent asociada a la notificación
 * y actualiza la base de datos indicando que la toma ha sido completada.
 */
@AndroidEntryPoint
class MarkAsTakenReceiver : BroadcastReceiver() {

    @Inject
    lateinit var intakeRepository: IntakeRepository

    override fun onReceive(context: Context, intent: Intent) {
        val intakeId = intent.getIntExtra("intake_id", -1)
        if (intakeId == -1) return

        // Permite continuar la ejecución de forma asíncrona fuera de onReceive()
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {

                // Obtiene la toma desde la base de datos. Si ya no existe (por ejemplo,
                // porque se borró el medicamento), solo se retira la notificación.
                val intake = intakeRepository.getIntakeById(intakeId)
                if (intake != null) {
                    // Marca la toma como realizada actualizando la fecha de realización
                    intakeRepository.updateIntake(intake.copy(takenAt = LocalDateTime.now()))
                }
                // Elimina la notificación una vez marcada como tomada
                NotificationManagerCompat.from(context).cancel(intakeId)
            } catch (e: Exception) {
                Log.e("PillDebug", "MarkAsTaken - Error", e)
            } finally {
                // Finaliza el procesamiento asíncrono del BroadcastReceiver
                pendingResult.finish()
            }
        }
    }
}