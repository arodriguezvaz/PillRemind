package com.andres.pillremind

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PillRemindApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

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
