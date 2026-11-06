package com.andres.pillremind.ui.screens.medication

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del archivo, la recuperación y el borrado definitivo de medicamentos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MedicationViewModelTest {

    private val medicationRepository = mockk<MedicationRepository>(relaxed = true)
    private val scheduleRepository = mockk<ScheduleRepository>(relaxed = true)
    private val intakeRepository = mockk<IntakeRepository>()
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private lateinit var viewModel: MedicationViewModel

    private val archivedAt = LocalDateTime.of(2026, 11, 2, 18, 30)
    private val medication = Medication(
        id = 3,
        profileId = 1,
        name = "Enalapril",
        dose = 10.0,
        unit = "mg",
        startDate = LocalDate.of(2026, 3, 12),
        deletedAt = archivedAt,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.activeProfileId } returns flowOf(1)
        coEvery { reminderScheduler.generateWindow() } returns emptyList()
        viewModel = MedicationViewModel(
            savedStateHandle = SavedStateHandle(),
            medicationRepository = medicationRepository,
            scheduleRepository = scheduleRepository,
            reminderScheduler = reminderScheduler,
            intakeRepository = intakeRepository,
            sessionManager = sessionManager,
            context = context,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `restoreMedication desarchiva y regenera la ventana de tomas`() = runTest {
        coEvery { medicationRepository.unarchiveMedication(3, any()) } just Runs
        var restored = false

        viewModel.restoreMedication(medication) { restored = true }

        coVerify { medicationRepository.unarchiveMedication(3, any()) }
        coVerify { reminderScheduler.generateWindow(any(), any()) }
        assertTrue(restored)
    }

    @Test
    fun `deleteMedicationPermanently borra el medicamento, su imagen y sus alarmas`() = runTest {
        val image = File.createTempFile("medication", ".jpg").apply { writeText("foto") }
        val medicationWithImage = medication.copy(imageUrl = image.absolutePath)
        val futureIntake = Intake(
            id = 7,
            medicationId = medication.id,
            scheduledAt = LocalDateTime.now().plusHours(2),
        )
        coEvery {
            intakeRepository.getFutureIntakesByMedicationId(medication.id, any())
        } returns listOf(futureIntake)
        coEvery { medicationRepository.deleteMedication(medicationWithImage) } just Runs
        var deleted = false

        viewModel.deleteMedicationPermanently(medicationWithImage) { deleted = true }

        coVerify { medicationRepository.deleteMedication(medicationWithImage) }
        verify { reminderScheduler.cancelAlarms(listOf(futureIntake.id)) }
        assertFalse(image.exists())
        assertTrue(deleted)
    }
}
