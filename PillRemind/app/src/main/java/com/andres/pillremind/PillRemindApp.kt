package com.andres.pillremind

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.andres.pillremind.worker.GenerateIntakesWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Clase Application de la app.
 *
 * Inicializa Hilt y crea el canal de notificaciones necesario
 * para los recordatorios de medicación.
 */
@HiltAndroidApp
class PillRemindApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Configuración personalizada de WorkManager.
     *
     * Se utiliza para indicar a WorkManager que debe usar `HiltWorkerFactory`
     * al crear los Workers de la aplicación. Esto permite que los Workers
     * puedan recibir dependencias mediante inyección con Hilt.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /**
     * Método llamado al iniciar la aplicación.
     *
     * Aquí se realizan inicializaciones globales necesarias antes de que
     * el usuario interactúe con la app, como la creación del canal de
     * notificaciones utilizado por los recordatorios de medicación.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Crea el canal de notificaciones para los recordatorios de medicamentos.
     * Requerido para mostrar notificaciones en Android 8+.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "pill_remind_channel",
            "Recordatorios de medicamentos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos para tomar la medicación"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun registrarWorkerDiario(context: Context) {
        val ahora = LocalTime.now()
        val objetivo = LocalTime.of(6, 0)
        val minutosHasta6am = if (ahora.isBefore(objetivo))
            ChronoUnit.MINUTES.between(ahora, objetivo)
        else
            ChronoUnit.MINUTES.between(ahora, objetivo.plusHours(24))

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "generar-tomas",
            ExistingPeriodicWorkPolicy.KEEP,  // no sobreescribe si ya existe
            PeriodicWorkRequestBuilder<GenerateIntakesWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(minutosHasta6am, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        )
    }

}