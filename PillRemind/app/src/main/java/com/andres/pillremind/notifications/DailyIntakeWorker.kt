package com.andres.pillremind.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andres.pillremind.domain.repository.IntakeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId

/**
 * Worker encargado de generar automáticamente las tomas de medicación del día siguiente
 * y programar sus alarmas asociadas.
 *
 * Este Worker se ejecuta en segundo plano mediante WorkManager y realiza el siguiente flujo:

 * En caso de error durante el proceso, se solicita reintento mediante WorkManager.
 */
@HiltWorker
class DailyIntakeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val intakeRepository: IntakeRepository,
    private val alarmScheduler: AlarmScheduler,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            //Calcula la fecha del día siguiente.
            val tomorrow = LocalDate.now().plusDays(1)
            // Genera en base de datos las tomas programadas para esa fecha.
            val intakes = intakeRepository.createIntakes(tomorrow, tomorrow)
            // Convierte cada hora programada a formato de milisegundos compatible con AlarmManager.
            for (intake in intakes) {
                val millis = intake.scheduledAt
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                //Solo progrma la alarma si la fecha de la intake no pasó
                if (millis > System.currentTimeMillis()) {
                    alarmScheduler.schedule(intake.id, millis)
                }
            }
            //Registra la última fecha generada para evitar duplicados en futuras ejecuciones.
            intakeRepository.saveLastGeneratedDate(tomorrow)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}