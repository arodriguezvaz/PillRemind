package com.andres.pillremind.worker

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andres.pillremind.domain.repository.IntakeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

/**
 * Worker encargado de generar automáticamente las tomas pendientes
 * a partir de los horarios configurados en la aplicación.
 *
 * El proceso:
 * - Genera las tomas faltantes hasta el día siguiente.
 * - Guarda la nueva fecha de última generación.
 *
 * La generación utiliza inserciones idempotentes, por lo que ejecutar
 * el Worker varias veces no produce tomas duplicadas.
 *
 * Este Worker está pensado para ejecutarse periódicamente mediante
 * WorkManager.
 */
@HiltWorker
class GenerateIntakesWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val intakeRepository: IntakeRepository,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = try {
        // Obtiene la última fecha para la que se generaron tomas.

        val desde = intakeRepository.getLastGeneratedDate()
        val hasta = LocalDate.now().plusDays(1)

        intakeRepository.createIntakes(desde, hasta)
        intakeRepository.saveLastGeneratedDate(LocalDate.now())

        Result.success()
    } catch (e: SQLiteException) {
        Result.retry()
    } catch (e: Exception) {
        Result.failure()
    }
}