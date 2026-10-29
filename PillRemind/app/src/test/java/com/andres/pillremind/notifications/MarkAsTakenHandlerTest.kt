package com.andres.pillremind.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.andres.pillremind.R
import com.andres.pillremind.domain.repository.IntakeRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Pruebas del marcado de tomas desde la acción de la notificación.
 *
 * Se comprueba que se actualice la toma, que se cancele su notificación y que
 * un identificador inválido o un error del repositorio no rompan el receiver.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MarkAsTakenHandlerTest {

    private companion object {
        const val INTAKE_ID = 1
    }

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val intakeRepository = mockk<IntakeRepository>()

    private lateinit var handler: MarkAsTakenHandler
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        handler = MarkAsTakenHandler(context, intakeRepository)
        notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.REMINDERS,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    @Test
    fun `marca la toma y cancela la notificacion`() = runTest {
        postNotification()
        coEvery { intakeRepository.markTaken(INTAKE_ID, any()) } just Runs

        handler.handle(INTAKE_ID)

        coVerify(exactly = 1) { intakeRepository.markTaken(INTAKE_ID, any()) }
        assertEquals(0, shadowOf(notificationManager).size())
    }

    @Test
    fun `ignora un identificador invalido`() = runTest {
        handler.handle(-1)

        coVerify(exactly = 0) { intakeRepository.markTaken(any(), any()) }
    }

    @Test
    fun `no propaga errores del repositorio`() = runTest {
        postNotification()
        coEvery { intakeRepository.markTaken(INTAKE_ID, any()) } throws
            IllegalStateException("fallo")

        handler.handle(INTAKE_ID)

        coVerify(exactly = 1) { intakeRepository.markTaken(INTAKE_ID, any()) }
        assertEquals(1, shadowOf(notificationManager).size())
    }

    private fun postNotification() {
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("Recordatorio")
            .build()
        notificationManager.notify(INTAKE_ID, notification)
    }
}
