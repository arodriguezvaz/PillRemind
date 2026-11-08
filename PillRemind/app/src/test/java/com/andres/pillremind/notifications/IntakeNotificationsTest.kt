package com.andres.pillremind.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.andres.pillremind.R
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Pruebas del punto único de cancelación de notificaciones de tomas.
 *
 * El identificador de notificación es el `intakeId`; estas pruebas comprueban
 * que solo se cancela lo indicado y que los IDs inválidos no tocan el gestor.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class IntakeNotificationsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var notificationManager: NotificationManager
    private lateinit var intakeNotifications: IntakeNotifications

    @Before
    fun setUp() {
        notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.REMINDERS,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        intakeNotifications = IntakeNotifications(context)
    }

    @Test
    fun `cancel solo elimina la notificacion de la toma indicada`() {
        postNotification(intakeId = 11)
        postNotification(intakeId = 12)

        intakeNotifications.cancel(11)

        val shadow = shadowOf(notificationManager)
        assertEquals(1, shadow.size())
        assertNull(shadow.getNotification(11))
        assertNotNull(shadow.getNotification(12))
    }

    @Test
    fun `cancel ignora identificadores no positivos`() {
        postNotification(intakeId = 11)

        intakeNotifications.cancel(0)
        intakeNotifications.cancel(-5)

        assertEquals(1, shadowOf(notificationManager).size())
    }

    @Test
    fun `cancelAll de una coleccion elimina solo esas notificaciones`() = runBlocking {
        postNotification(intakeId = 11)
        postNotification(intakeId = 12)
        postNotification(intakeId = 13)

        intakeNotifications.cancelAll(listOf(11, 13))

        val shadow = shadowOf(notificationManager)
        assertEquals(1, shadow.size())
        assertNull(shadow.getNotification(11))
        assertNull(shadow.getNotification(13))
        assertNotNull(shadow.getNotification(12))
    }

    @Test
    fun `cancelAll sin argumentos elimina todas las notificaciones de la app`() {
        postNotification(intakeId = 11)
        postNotification(intakeId = 12)

        intakeNotifications.cancelAll()

        assertEquals(0, shadowOf(notificationManager).size())
    }

    private fun postNotification(intakeId: Int) {
        notificationManager.notify(
            intakeId,
            NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle("Recordatorio")
                .build()
        )
    }
}
