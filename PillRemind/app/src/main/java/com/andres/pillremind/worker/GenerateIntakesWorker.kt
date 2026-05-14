package com.andres.pillremind.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andres.pillremind.domain.repository.IntakeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class GenerateIntakesWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val intakeRepository: IntakeRepository,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = try {
        val desde = intakeRepository.getLastGeneratedDate()
        val hasta = LocalDate.now().plusDays(1)

        intakeRepository.createIntakes(desde, hasta)
        intakeRepository.saveLastGeneratedDate(LocalDate.now())

        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}