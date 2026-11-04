package com.andres.pillremind.ui.screens.home

import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.notifications.ReminderNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del marcado de tomas desde Home.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val intakeRepository = mockk<IntakeRepository>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val notifications = mockk<ReminderNotifications>(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    private val scheduledAt = LocalDateTime.of(2026, 10, 31, 9, 0)
    private val intake = IntakeWithMedication(
        intakeId = 5,
        medicationId = 1,
        scheduledAt = scheduledAt,
        scheduleId = 10,
        takenAt = null,
        medicationName = "Ibuprofeno",
        dose = 600.0,
        unit = "mg",
        imageUrl = null,
        profileName = "Ana",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.activeProfileId } returns flowOf(1)
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()
        viewModel = HomeViewModel(
            intakeRepository = intakeRepository,
            sessionManager = sessionManager,
            reminderScheduler = reminderScheduler,
            notifications = notifications,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tomarIntake registra la toma y cancela su notificacion`() = runTest {
        coEvery { intakeRepository.markTaken(5, any()) } just Runs

        viewModel.tomarIntake(intake)

        coVerify(exactly = 1) { intakeRepository.markTaken(5, any()) }
        verify(exactly = 1) { notifications.cancel(5) }
    }

    @Test
    fun `tomarIntake vuelve a dejar pendiente una toma ya registrada`() = runTest {
        coEvery { intakeRepository.markTaken(5, null) } just Runs

        viewModel.tomarIntake(intake.copy(takenAt = scheduledAt))

        coVerify(exactly = 1) { intakeRepository.markTaken(5, null) }
        verify(exactly = 1) { notifications.cancel(5) }
    }
}
