package com.andres.pillremind

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application de la app.
 *
 * Inicializa Hilt y crea el canal de notificaciones necesario
 * para los recordatorios de medicación.
 */
@HiltAndroidApp
class PillRemindApp : Application() {

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


}