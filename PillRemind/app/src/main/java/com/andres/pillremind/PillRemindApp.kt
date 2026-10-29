package com.andres.pillremind

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.andres.pillremind.notifications.DailyIntakeWorker
import com.andres.pillremind.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import java.util.concurrent.TimeUnit

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

    // WorkManager llama a esta propiedad antes de inicializarse.
    // Esta configuración conecta HiltWorkerFactory con WorkManager.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleDailyIntakeWorker()
        setupImageLoader()
    }

    /**
     * Programa un worker periódico que se ejecuta una vez al día.
     *
     * El worker se registra como trabajo único para evitar que se creen
     * múltiples instancias del mismo. Si ya existe un worker programado,
     * se mantiene el actual gracias a [ExistingPeriodicWorkPolicy.KEEP].
     */
    private fun scheduleDailyIntakeWorker() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyIntakeWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_intake_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    /**
     * Configura el [ImageLoader] global de Coil.
     *
     * Se habilita una caché en memoria para almacenar imágenes utilizadas
     * recientemente y una caché en disco para conservar imágenes entre
     * ejecuciones de la aplicación.
     */
    private fun setupImageLoader() {
        val imageLoader = ImageLoader.Builder(this)
            // Configuración de caché en memoria
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            // Configuración de caché en disco
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
        Coil.setImageLoader(imageLoader)
    }

    /**
     * Crea el canal de notificaciones para los recordatorios de medicamentos.
     * Requerido para mostrar notificaciones en Android 8+.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotificationChannels.REMINDERS,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}