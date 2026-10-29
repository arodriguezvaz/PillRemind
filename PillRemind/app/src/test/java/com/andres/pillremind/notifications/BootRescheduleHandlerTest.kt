package com.andres.pillremind.notifications

import android.content.Intent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de la reprogramación de recordatorios tras eventos del sistema.
 *
 * Se comprueba que solo se regenere la ventana con las acciones relevantes y
 * que un fallo del scheduler no se propague.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BootRescheduleHandlerTest {

    private val reminderScheduler = mockk<ReminderScheduler>()
    private lateinit var handler: BootRescheduleHandler

    @Before
    fun setUp() {
        handler = BootRescheduleHandler(reminderScheduler)
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()
    }

    @Test
    fun `reprograma al arrancar el dispositivo`() = runTest {
        handler.handle(Intent.ACTION_BOOT_COMPLETED)

        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `reprograma al actualizar la aplicacion`() = runTest {
        handler.handle(Intent.ACTION_MY_PACKAGE_REPLACED)

        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `reprograma al conceder el permiso de alarmas exactas`() = runTest {
        handler.handle(BootRescheduleHandler.ACTION_EXACT_ALARM_PERMISSION_CHANGED)

        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `ignora acciones que no requieren reprogramacion`() = runTest {
        handler.handle(Intent.ACTION_TIME_CHANGED)
        handler.handle(null)

        coVerify(exactly = 0) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `no propaga errores del scheduler`() = runTest {
        coEvery { reminderScheduler.generateWindow(any(), any()) } throws IllegalStateException("fallo")

        handler.handle(Intent.ACTION_BOOT_COMPLETED)

        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }
}
