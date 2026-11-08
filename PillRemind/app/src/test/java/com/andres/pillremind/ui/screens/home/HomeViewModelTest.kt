package com.andres.pillremind.ui.screens.home

import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.notifications.IntakeNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    private val intakeNotifications = mockk<IntakeNotifications>(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()
        every { sessionManager.activeProfileId } returns flowOf(1)
        every {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate())
        } returns flowOf(emptyList())
        every {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate().minusDays(1))
        } returns flowOf(emptyList())
        every { intakeRepository.observeStatisticsIntakes(1, any(), any()) } returns flowOf(
            listOf(
                intake(id = 1, scheduledAt = now.minusHours(2), takenAt = now.minusHours(2)),
                intake(id = 2, scheduledAt = now.minusHours(1), takenAt = now.minusMinutes(50)),
                intake(id = 3, scheduledAt = now.minusMinutes(30))
            )
        )
        viewModel = createViewModel()
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
    fun `las tomas de hoy se consultan con la fecha de referencia`() = runTest {
        val todayIntake = intakeWithMedication(id = 7, scheduledAt = now)
        every {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate())
        } returns flowOf(listOf(todayIntake))

        val result = viewModel.intakes.first { it.today.isNotEmpty() }.today

        assertEquals(7, result.single().intakeId)
        io.mockk.verify {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate())
        }
    }

    @Test
    fun `las tomas de ayer incluyen pendientes y tomadas de madrugada`() = runTest {
        val yesterday = now.toLocalDate().minusDays(1)
        val pending = intakeWithMedication(
            id = 10,
            scheduledAt = yesterday.atTime(23, 0)
        )
        val takenOnTime = intakeWithMedication(
            id = 11,
            scheduledAt = yesterday.atTime(9, 0),
            takenAt = yesterday.atTime(9, 5)
        )
        val takenAfterMidnight = intakeWithMedication(
            id = 12,
            scheduledAt = yesterday.atTime(23, 30),
            takenAt = now.toLocalDate().atTime(0, 25)
        )
        every {
            intakeRepository.observeIntakesForDay(1, yesterday)
        } returns flowOf(listOf(pending, takenOnTime, takenAfterMidnight))

        val result = viewModel.intakes.first { it.yesterday.isNotEmpty() }.yesterday

        assertEquals(listOf(10, 12), result.map { it.intakeId })
    }

    @Test
    fun `refreshNow cambia el dia consultado`() = runTest {
        val nextDay = now.toLocalDate().plusDays(1)
        every {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate())
        } returns flowOf(listOf(intakeWithMedication(id = 1)))
        every {
            intakeRepository.observeIntakesForDay(1, nextDay)
        } returns flowOf(listOf(intakeWithMedication(id = 2)))
        viewModel.intakes.first { it.today.isNotEmpty() }

        viewModel.refreshNow(now.plusDays(1))

        val result = viewModel.intakes
            .first { it.today.singleOrNull()?.intakeId == 2 }
            .today
        assertEquals(2, result.single().intakeId)
    }

    @Test
    fun `refreshNow actualiza el momento y consulta el ayer del nuevo dia`() = runTest {
        val nextDay = now.toLocalDate().plusDays(1)
        val pendingFromNow = intakeWithMedication(
            id = 21,
            scheduledAt = now.toLocalDate().atTime(23, 0)
        )
        every {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate())
        } returns flowOf(listOf(pendingFromNow))
        every {
            intakeRepository.observeIntakesForDay(1, nextDay)
        } returns flowOf(emptyList())
        every {
            intakeRepository.observeIntakesForDay(1, nextDay.minusDays(1))
        } returns flowOf(listOf(pendingFromNow))

        viewModel.refreshNow(now.plusDays(1))

        assertEquals(now.plusDays(1), viewModel.now.value)
        val yesterday = viewModel.intakes.first { state ->
            state.yesterday.any { it.intakeId == 21 }
        }.yesterday
        assertEquals(listOf(21), yesterday.map { it.intakeId })
    }

    @Test
    fun `refreshNow en el mismo dia no vuelve a consultar las tomas`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.intakes.collect()
        }

        viewModel.refreshNow(now.plusHours(1))

        io.mockk.verify(exactly = 1) {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate())
        }
        io.mockk.verify(exactly = 1) {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate().minusDays(1))
        }
    }

    @Test
    fun `cambiar de perfil vuelve a consultar hoy y ayer sin mezclar`() = runTest {
        val activeProfile = MutableStateFlow<Int?>(1)
        every { sessionManager.activeProfileId } returns activeProfile
        every {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate())
        } returns flowOf(listOf(intakeWithMedication(id = 101, scheduledAt = now)))
        every {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate().minusDays(1))
        } returns flowOf(
            listOf(intakeWithMedication(id = 102, scheduledAt = now.minusDays(1)))
        )
        every {
            intakeRepository.observeIntakesForDay(2, now.toLocalDate())
        } returns flowOf(listOf(intakeWithMedication(id = 201, scheduledAt = now)))
        every {
            intakeRepository.observeIntakesForDay(2, now.toLocalDate().minusDays(1))
        } returns flowOf(emptyList())
        val switchedViewModel = createViewModel()
        switchedViewModel.refreshNow(now)

        assertEquals(
            listOf(101),
            switchedViewModel.intakes.first { it.today.isNotEmpty() }.today.map { it.intakeId }
        )
        assertEquals(
            listOf(102),
            switchedViewModel.intakes.first { it.yesterday.isNotEmpty() }
                .yesterday.map { it.intakeId }
        )

        activeProfile.value = 2

        assertEquals(
            listOf(201),
            switchedViewModel.intakes
                .first { state -> state.today.singleOrNull()?.intakeId == 201 }
                .today.map { it.intakeId }
        )
        val yesterday = switchedViewModel.intakes.first { state ->
            state.yesterday.none { it.intakeId == 102 }
        }.yesterday
        assertEquals(emptyList<Int>(), yesterday.map { it.intakeId })
    }

    @Test
    fun `el estado combinado conserva las tomas de hoy que no estan en ayer`() = runTest {
        val todayA = intakeWithMedication(id = 1, scheduledAt = now)
        val todayB = intakeWithMedication(id = 2, scheduledAt = now.plusHours(1))
        val yesterday = intakeWithMedication(
            id = 3,
            scheduledAt = now.toLocalDate().minusDays(1).atTime(23, 0)
        )
        every {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate())
        } returns flowOf(listOf(todayA, todayB))
        every {
            intakeRepository.observeIntakesForDay(1, now.toLocalDate().minusDays(1))
        } returns flowOf(listOf(yesterday))

        val state = viewModel.intakes.first { it.today.size == 2 && it.yesterday.isNotEmpty() }

        assertEquals(listOf(1, 2), state.today.map { it.intakeId })
        assertEquals(listOf(3), state.yesterday.map { it.intakeId })
    }

    @Test
    fun `tomarIntake marca la toma pendiente`() = runTest {
        coEvery { intakeRepository.markTaken(any(), any()) } returns Unit
        val before = LocalDateTime.now()

        viewModel.tomarIntake(intakeWithMedication(takenAt = null))

        val after = LocalDateTime.now()
        val takenAt = slot<LocalDateTime>()
        coVerify(exactly = 1) { intakeRepository.markTaken(1, capture(takenAt)) }
        assertTrue(!takenAt.captured.isBefore(before))
        assertTrue(!takenAt.captured.isAfter(after))
        verify(exactly = 1) { intakeNotifications.cancel(1) }
    }

    @Test
    fun `tomarIntake deshace la toma ya registrada`() = runTest {
        coEvery { intakeRepository.markTaken(any(), any()) } returns Unit

        viewModel.tomarIntake(intakeWithMedication(takenAt = now.minusHours(1)))

        coVerify(exactly = 1) { intakeRepository.markTaken(1, null) }
        verify(exactly = 0) { intakeNotifications.cancel(any()) }
    }

    private fun createViewModel(): HomeViewModel = HomeViewModel(
        profileRepository = profileRepository,
        intakeRepository = intakeRepository,
        sessionManager = sessionManager,
        reminderScheduler = reminderScheduler,
        intakeNotifications = intakeNotifications
    )

    private fun intakeWithMedication(
        id: Int = 1,
        scheduledAt: LocalDateTime = now,
        takenAt: LocalDateTime? = null
    ): IntakeWithMedication =
        IntakeWithMedication(
            intakeId = id,
            medicationId = 1,
            scheduledAt = scheduledAt,
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
