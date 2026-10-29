package com.andres.pillremind.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas del worker que mantiene generada la ventana rodante de tomas.
 *
 * Se comprueba que un fallo del scheduler se traduzca en un reintento de
 * WorkManager y que una ejecución correcta termine con éxito.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DailyIntakeWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val params = mockk<WorkerParameters>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>()
    private val worker = DailyIntakeWorker(context, params, reminderScheduler)

    @Test
    fun `doWork devuelve success cuando se genera la ventana`() = runTest {
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `doWork devuelve retry cuando falla la generacion`() = runTest {
        coEvery { reminderScheduler.generateWindow(any(), any()) } throws IllegalStateException("fallo")

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }
}
