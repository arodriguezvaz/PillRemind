package com.andres.pillremind.ui.screens.statistics

import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del ViewModel de estadísticas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private val now = LocalDateTime.of(2026, 11, 5, 12, 0)
    private val intakeRepository = mockk<IntakeRepository>()
    private val medicationRepository = mockk<MedicationRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)

    private lateinit var viewModel: StatisticsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { medicationRepository.hasMedications(1) } returns flowOf(true)
        every { intakeRepository.observeStatisticsIntakes(any(), any(), any()) } returns flowOf(
            listOf(
                intake(
                    id = 1,
                    scheduledAt = now.minusHours(2),
                    takenAt = now.minusHours(2)
                ),
                intake(
                    id = 2,
                    scheduledAt = now.minusMinutes(30)
                )
            )
        )
        viewModel = StatisticsViewModel(
            intakeRepository = intakeRepository,
            medicationRepository = medicationRepository,
            sessionManager = sessionManager
        )
        viewModel.refreshNow(now)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `expone el resumen y el estado cargado`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.hasMedications)
        assertEquals(1, state.report.summary.taken)
        assertEquals(1, state.report.summary.missed)
        assertEquals(50, state.report.summary.adherencePercent)
        assertEquals(1, state.report.byMedication.size)

        job.cancel()
    }

    @Test
    fun `el periodo inicial es siete y consulta el rango correcto`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(StatisticsPeriod.SEVEN, viewModel.period.value)
        verify {
            intakeRepository.observeStatisticsIntakes(
                profileId = 1,
                from = now.toLocalDate().minusDays(6).atStartOfDay(),
                to = now.toLocalDate().atTime(LocalTime.MAX)
            )
        }

        job.cancel()
    }

    @Test
    fun `cambiar a treinta dias vuelve a consultar el periodo`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectPeriod(StatisticsPeriod.THIRTY)
        advanceUntilIdle()

        assertEquals(StatisticsPeriod.THIRTY, viewModel.period.value)
        verify {
            intakeRepository.observeStatisticsIntakes(
                profileId = 1,
                from = now.toLocalDate().minusDays(29).atStartOfDay(),
                to = now.toLocalDate().atTime(LocalTime.MAX)
            )
        }

        job.cancel()
    }

    @Test
    fun `sin medicamentos mantiene el estado vacio`() = runTest {
        every { medicationRepository.hasMedications(1) } returns flowOf(false)
        every { intakeRepository.observeStatisticsIntakes(any(), any(), any()) } returns flowOf(emptyList())

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.hasMedications)
        assertEquals(0, state.report.summary.total)
        assertTrue(state.report.history.isEmpty())

        job.cancel()
    }

    @Test
    fun `un perfil con medicamentos pero sin tomas conserva el informe vacio`() = runTest {
        every { intakeRepository.observeStatisticsIntakes(any(), any(), any()) } returns flowOf(emptyList())

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.hasMedications)
        assertEquals(0, state.report.summary.total)
        assertTrue(state.report.history.isEmpty())

        job.cancel()
    }

    @Test
    fun `refreshNow actualiza el momento del estado`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val newNow = now.plusMinutes(20)
        viewModel.refreshNow(newNow)

        assertEquals(newNow, viewModel.uiState.value.now)

        job.cancel()
    }

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
