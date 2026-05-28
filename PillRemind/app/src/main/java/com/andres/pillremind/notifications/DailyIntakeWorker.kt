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
 * Worker para generar intakes diarios.
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
            val tomorrow = LocalDate.now().plusDays(1)
            val intakes = intakeRepository.createIntakes(tomorrow, tomorrow)

            for (intake in intakes) {
                val millis = intake.scheduledAt
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                // Solo programa si la hora no ha pasado
                if (millis > System.currentTimeMillis()) {
                    alarmScheduler.schedule(intake.id, millis)
                }
            }

            intakeRepository.saveLastGeneratedDate(tomorrow)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}