package com.andres.pillremind.ui.screens.archived

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.andres.pillremind.R
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.notifications.ReminderNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de restauración y borrado definitivo de medicamentos archivados.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ArchivedMedicationsViewModelTest {

    private val medicationRepository = mockk<MedicationRepository>(relaxed = true)
    private val intakeRepository = mockk<IntakeRepository>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val notifications = mockk<ReminderNotifications>(relaxed = true)

    private lateinit var context: Context
    private lateinit var viewModel: ArchivedMedicationsViewModel

    private val medication = Medication(
        id = 1,
        profileId = 1,
        name = "Ibuprofeno",
        dose = 600.0,
        unit = "mg",
        startDate = LocalDate.of(2026, 1, 1),
        deletedAt = LocalDateTime.of(2026, 10, 1, 12, 0),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        context = ApplicationProvider.getApplicationContext()
        every { sessionManager.activeProfileId } returns flowOf(1)
        viewModel = ArchivedMedicationsViewModel(
            medicationRepository = medicationRepository,
            intakeRepository = intakeRepository,
            sessionManager = sessionManager,
            reminderScheduler = reminderScheduler,
            notifications = notifications,
            context = context,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `restaurar recupera el medicamento y regenera recordatorios`() = runTest {
        coEvery { medicationRepository.restoreMedication(1, any()) } just Runs
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()
        var restored = false

        viewModel.restoreMedication(medication) { restored = true }

        assertTrue(restored)
        coVerify(exactly = 1) { medicationRepository.restoreMedication(1, any()) }
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
        assertNull(viewModel.errorRes.value)
    }

    @Test
    fun `borrar definitivamente limpia notificaciones y la imagen`() = runTest {
        val image = File(context.filesDir, "archived_image.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3))
        }
        coEvery { intakeRepository.getIntakeIdsByMedicationId(1) } returns listOf(3, 4)
        coEvery { medicationRepository.deleteMedicationPermanently(1) } just Runs
        coEvery { reminderScheduler.cancelFutureForMedication(1, any()) } just Runs

        viewModel.deletePermanently(medication.copy(imageUrl = image.absolutePath))

        assertFalse(image.exists())
        coVerify(exactly = 1) { medicationRepository.deleteMedicationPermanently(1) }
        verify(exactly = 1) { notifications.cancelAll(listOf(3, 4)) }
        coVerify(exactly = 1) { reminderScheduler.cancelFutureForMedication(1, any()) }
    }

    @Test
    fun `si el borrado falla conserva la imagen y muestra error`() = runTest {
        val image = File(context.filesDir, "kept_image.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3))
        }
        coEvery { intakeRepository.getIntakeIdsByMedicationId(1) } returns emptyList()
        coEvery { medicationRepository.deleteMedicationPermanently(1) } throws
            IllegalStateException("fallo")

        viewModel.deletePermanently(medication.copy(imageUrl = image.absolutePath))

        assertTrue(image.exists())
        assertEquals(R.string.medication_delete_error, viewModel.errorRes.value)
    }
}
