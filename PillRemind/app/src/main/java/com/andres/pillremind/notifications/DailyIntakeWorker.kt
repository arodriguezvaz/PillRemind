package com.andres.pillremind.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker encargado de mantener generada la ventana rodante de tomas de medicación
 * y de programar sus alarmas asociadas.
 *
 * Este Worker se ejecuta en segundo plano mediante WorkManager. En cada ejecución
 * amplía la ventana hasta los próximos [ReminderScheduler.WINDOW_DAYS] días.
 *
 * En caso de error durante el proceso, se solicita reintento mediante WorkManager.
 */
@HiltWorker
class DailyIntakeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderScheduler: ReminderScheduler,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Genera la ventana rodante y programa las alarmas de sus tomas futuras.
            reminderScheduler.generateWindow()
            Result.success()
        } catch (e: Exception) {
            Log.e(
                "DailyIntakeWorker",
                "No se pudo generar la ventana de tomas; se reintentará",
                e
            )
            Result.retry()
        }
    }
}