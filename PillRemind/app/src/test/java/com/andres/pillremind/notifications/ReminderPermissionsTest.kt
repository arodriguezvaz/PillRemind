package com.andres.pillremind.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager

/**
 * Pruebas de la lógica pura que decide el estado y la acción de los permisos.
 */
class ReminderPermissionsLogicTest {

    @Test
    fun `sin permiso runtime queda solicitable`() {
        assertEquals(
            NotificationStatus.REQUESTABLE,
            resolveNotificationStatus(
                permissionGranted = false,
                notificationsEnabled = true,
                channelBlocked = false,
            ),
        )
    }

    @Test
    fun `notificaciones desactivadas quedan bloqueadas`() {
        assertEquals(
            NotificationStatus.BLOCKED,
            resolveNotificationStatus(
                permissionGranted = true,
                notificationsEnabled = false,
                channelBlocked = false,
            ),
        )
    }

    @Test
    fun `el canal bloqueado deja las notificaciones bloqueadas`() {
        assertEquals(
            NotificationStatus.BLOCKED,
            resolveNotificationStatus(
                permissionGranted = true,
                notificationsEnabled = true,
                channelBlocked = true,
            ),
        )
    }

    @Test
    fun `permiso y canal activos quedan concedidos`() {
        assertEquals(
            NotificationStatus.GRANTED,
            resolveNotificationStatus(
                permissionGranted = true,
                notificationsEnabled = true,
                channelBlocked = false,
            ),
        )
    }

    @Test
    fun `antes de pedir el permiso se solicita`() {
        assertEquals(
            NotificationAction.REQUEST,
            resolveNotificationAction(
                status = NotificationStatus.REQUESTABLE,
                requested = false,
                showRationale = false,
            ),
        )
    }

    @Test
    fun `una denegacion definitiva abre ajustes`() {
        assertEquals(
            NotificationAction.OPEN_SETTINGS,
            resolveNotificationAction(
                status = NotificationStatus.REQUESTABLE,
                requested = true,
                showRationale = false,
            ),
        )
    }

    @Test
    fun `mientras haya rationale se vuelve a solicitar`() {
        assertEquals(
            NotificationAction.REQUEST,
            resolveNotificationAction(
                status = NotificationStatus.REQUESTABLE,
                requested = true,
                showRationale = true,
            ),
        )
    }

    @Test
    fun `un permiso concedido no necesita accion`() {
        assertEquals(
            NotificationAction.NONE,
            resolveNotificationAction(
                status = NotificationStatus.GRANTED,
                requested = true,
                showRationale = false,
            ),
        )
    }
}

/**
 * Pruebas de las comprobaciones que dependen de la versión de Android.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [33])
class ReminderPermissionsTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val reminderPermissions = ReminderPermissions(application)

    @Before
    fun setUp() {
        shadowOf(application.getSystemService(NotificationManager::class.java))
            .setNotificationsEnabled(true)
    }

    @Test
    fun `sin permiso en android 13 queda solicitable`() {
        assertEquals(
            NotificationStatus.REQUESTABLE,
            reminderPermissions.notificationStatus(),
        )
    }

    @Test
    fun `con permiso concedido queda concedido`() {
        shadowOf(application).grantPermissions(POST_NOTIFICATIONS_PERMISSION)

        assertEquals(
            NotificationStatus.GRANTED,
            reminderPermissions.notificationStatus(),
        )
    }

    @Test
    fun `el canal bloqueado deja las notificaciones bloqueadas`() {
        shadowOf(application).grantPermissions(POST_NOTIFICATIONS_PERMISSION)
        val manager = application.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.REMINDERS,
                "Prueba",
                NotificationManager.IMPORTANCE_NONE,
            ),
        )

        assertEquals(
            NotificationStatus.BLOCKED,
            reminderPermissions.notificationStatus(),
        )
    }

    @Test
    @Config(sdk = [26])
    fun `android 11 no necesita alarmas exactas`() {
        assertEquals(
            ExactAlarmStatus.NOT_REQUIRED,
            reminderPermissions.exactAlarmStatus(),
        )
    }

    @Test
    fun `android 13 con permiso concede alarmas exactas`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)

        assertEquals(
            ExactAlarmStatus.GRANTED,
            reminderPermissions.exactAlarmStatus(),
        )
    }

    @Test
    fun `android 13 sin permiso queda pendiente o no disponible`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)

        assertTrue(
            reminderPermissions.exactAlarmStatus() in
                setOf(ExactAlarmStatus.PENDING, ExactAlarmStatus.UNAVAILABLE),
        )
    }
}
