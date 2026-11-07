package com.andres.pillremind.ui.screens.home

import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del resumen semanal que alimenta el banner de Inicio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val now = LocalDateTime.of(2026, 11, 5, 12, 0)
    private val profileRepository = mockk<ProfileRepository>(relaxed = true)
    private val intakeRepository = mockk<IntakeRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { intakeRepository.getIntakesForToday(any(), 1) } returns flowOf(emptyList())
        every { intakeRepository.observeStatisticsIntakes(1, any(), any()) } returns flowOf(
            listOf(
                intake(id = 1, scheduledAt = now.minusHours(2), takenAt = now.minusHours(2)),
                intake(id = 2, scheduledAt = now.minusHours(1), takenAt = now.minusMinutes(50)),
                intake(id = 3, scheduledAt = now.minusMinutes(30))
            )
        )
        viewModel = HomeViewModel(
            profileRepository = profileRepository,
            intakeRepository = intakeRepository,
            sessionManager = sessionManager,
            reminderScheduler = reminderScheduler
        )
        viewModel.refreshNow(now)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `el resumen semanal calcula tomadas y omitidas`() = runTest {
        val summary = viewModel.weeklySummary.first { it != null }

        assertNotNull(summary)
        assertEquals(2, summary?.taken)
        assertEquals(1, summary?.missed)
        assertEquals(0, summary?.pending)
        assertEquals(3, summary?.evaluated)
        assertEquals(67, summary?.adherencePercent)
    }

    @Test
    fun `el resumen semanal usa el rango de los ultimos siete dias`() = runTest {
        viewModel.weeklySummary.first { it != null }

        io.mockk.verify {
            intakeRepository.observeStatisticsIntakes(
                profileId = 1,
                from = now.toLocalDate().minusDays(6).atStartOfDay(),
                to = now.toLocalDate().atTime(LocalTime.MAX)
            )
        }
    }

    @Test
    fun `el resumen semanal puede quedar sin tomas evaluadas`() = runTest {
        every { intakeRepository.observeStatisticsIntakes(1, any(), any()) } returns flowOf(
            listOf(intake(id = 1, scheduledAt = now.plusHours(1)))
        )

        val summary = viewModel.weeklySummary.first { it != null }

        assertEquals(0, summary?.evaluated)
        assertEquals(1, summary?.pending)
    }

    @Test
    fun `tomarIntake marca la toma pendiente`() = runTest {
        coEvery { intakeRepository.markTaken(any(), any()) } returns Unit

        viewModel.tomarIntake(intakeWithMedication(takenAt = null))

        val takenAt = slot<LocalDateTime>()
        coVerify(exactly = 1) { intakeRepository.markTaken(1, capture(takenAt)) }
        assertNotNull(takenAt.captured)
    }

    @Test
    fun `tomarIntake deshace la toma ya registrada`() = runTest {
        coEvery { intakeRepository.markTaken(any(), any()) } returns Unit

        viewModel.tomarIntake(intakeWithMedication(takenAt = now.minusHours(1)))

        coVerify(exactly = 1) { intakeRepository.markTaken(1, null) }
    }

    private fun intakeWithMedication(takenAt: LocalDateTime?): IntakeWithMedication =
        IntakeWithMedication(
            intakeId = 1,
            medicationId = 1,
            scheduledAt = now,
            scheduleId = 1,
            takenAt = takenAt,
            medicationName = "Ibuprofeno",
            dose = 600.0,
            unit = "mg",
            imageUrl = null,
            profileName = "Andrés"
        )

    private fun intake(
        id: Int,
        scheduledAt: LocalDateTime,
        takenAt: LocalDateTime? = null
    ): StatisticsIntake = StatisticsIntake(
        intakeId = id,
        medicationId = 1,
        scheduledAt = scheduledAt,
        takenAt = takenAt,
        medicationName = "Ibuprofeno",
        dose = 600.0,
        unit = "mg",
        imageUrl = null,
        isArchived = false
    )
}
