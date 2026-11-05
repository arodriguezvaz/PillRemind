package com.andres.pillremind.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsStatus
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del ViewModel del historial.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val now = LocalDateTime.of(2026, 11, 6, 12, 0)
    private val today = now.toLocalDate()
    private val intakeRepository = mockk<IntakeRepository>()
    private val medicationRepository = mockk<MedicationRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)

    private val medications = listOf(
        medication(id = 1, name = "Ibuprofeno", archived = false),
        medication(id = 2, name = "Omeprazol", archived = true),
    )
    private val intakes = listOf(
        intake(
            id = 1,
            medicationId = 1,
            scheduledAt = now.minusHours(2),
            takenAt = now.minusHours(2),
        ),
        intake(
            id = 2,
            medicationId = 1,
            scheduledAt = now.plusMinutes(2),
        ),
        intake(
            id = 3,
            medicationId = 2,
            scheduledAt = now.minusDays(1).withHour(9),
            takenAt = null,
        ),
        intake(
            id = 4,
            medicationId = 2,
            scheduledAt = now.minusDays(1).withHour(21),
            takenAt = now.minusDays(1).withHour(21).plusMinutes(3),
        ),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { medicationRepository.getAllMedications(1) } returns flowOf(medications)
        every { intakeRepository.observeHistoryIntakes(1, any(), any()) } returns flowOf(intakes)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `expone el periodo por defecto y ordena de mas reciente a mas antigua`() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.hasMedications)
        assertEquals(4, state.totalInRange)
        assertEquals(listOf(2, 1, 4, 3), state.entries.map { it.intake.intakeId })
        assertEquals(2, state.medications.size)
        verify {
            intakeRepository.observeHistoryIntakes(
                profileId = 1,
                from = today.minusDays(29).atStartOfDay(),
                to = today.atTime(LocalTime.MAX),
            )
        }

        job.cancel()
    }

    @Test
    fun `la busqueda por nombre ignora mayusculas`() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("ibu")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.entries.all { it.intake.medicationName == "Ibuprofeno" })
        assertEquals(2, viewModel.uiState.value.entries.size)

        job.cancel()
    }

    @Test
    fun `la busqueda tambien encuentra el nombre actual del medicamento`() = runTest {
        every { intakeRepository.observeHistoryIntakes(1, any(), any()) } returns flowOf(
            listOf(
                intake(
                    id = 1,
                    medicationId = 1,
                    scheduledAt = now.minusHours(2),
                    takenAt = now.minusHours(2),
                ).copy(medicationName = "Antiinflamatorio")
            )
        )
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Ibuprofeno")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.entries.size)

        job.cancel()
    }

    @Test
    fun `el filtro de estado deja solo las tomas elegidas`() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectStatus(HistoryStatusFilter.TAKEN)
        advanceUntilIdle()

        assertTrue(
            viewModel.uiState.value.entries.all { it.status == StatisticsStatus.TAKEN }
        )
        assertEquals(2, viewModel.uiState.value.entries.size)

        job.cancel()
    }

    @Test
    fun `el filtro de medicamento y los archivados se combinan`() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectMedication(
            HistoryMedicationOption(id = 2, name = "Omeprazol", isArchived = true)
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.filters.value.medicationId)
        assertTrue(viewModel.filters.value.includeArchived)
        assertTrue(viewModel.uiState.value.entries.all { it.intake.medicationId == 2 })
        assertEquals(2, viewModel.uiState.value.entries.size)

        job.cancel()
    }

    @Test
    fun `desactivar los archivados oculta sus tomas`() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.setIncludeArchived(false)
        advanceUntilIdle()

        assertFalse(viewModel.filters.value.includeArchived)
        assertTrue(viewModel.uiState.value.entries.none { it.intake.isArchived })
        assertEquals(2, viewModel.uiState.value.entries.size)

        job.cancel()
    }

    @Test
    fun `el rango personalizado consulta las fechas elegidas`() = runTest {
        val from = LocalDate.of(2026, 10, 1)
        val to = LocalDate.of(2026, 10, 15)
        every { intakeRepository.observeHistoryIntakes(1, any(), any()) } returns flowOf(intakes)
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.applyCustomRange(from, to)
        advanceUntilIdle()

        assertEquals(
            HistoryDateFilter.Custom(from, to),
            viewModel.filters.value.dateFilter
        )
        verify {
            intakeRepository.observeHistoryIntakes(
                profileId = 1,
                from = from.atStartOfDay(),
                to = to.atTime(LocalTime.MAX),
            )
        }

        job.cancel()
    }

    @Test
    fun `el periodo todo consulta sin fecha inicial`() = runTest {
        every {
            intakeRepository.observeHistoryIntakes(1, null, any())
        } returns flowOf(intakes)
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectPeriod(HistoryPeriod.ALL)
        advanceUntilIdle()

        assertEquals(
            HistoryDateFilter.Preset(HistoryPeriod.ALL),
            viewModel.filters.value.dateFilter
        )
        verify {
            intakeRepository.observeHistoryIntakes(
                profileId = 1,
                from = null,
                to = today.atTime(LocalTime.MAX),
            )
        }

        job.cancel()
    }

    @Test
    fun `el medicamento de navegacion se preselecciona`() = runTest {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(mapOf("medicationId" to 2))
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(2, viewModel.filters.value.medicationId)
        assertTrue(viewModel.uiState.value.entries.all { it.intake.medicationId == 2 })

        job.cancel()
    }

    @Test
    fun `limpiar filtros vuelve al estado inicial`() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("ibu")
        viewModel.selectStatus(HistoryStatusFilter.MISSED)
        viewModel.selectMedication(
            HistoryMedicationOption(id = 2, name = "Omeprazol", isArchived = true)
        )
        viewModel.clearFilters()
        advanceUntilIdle()

        assertEquals(HistoryFilters(), viewModel.filters.value)
        assertNull(viewModel.filters.value.medicationId)

        job.cancel()
    }

    @Test
    fun `un perfil sin medicamentos mantiene el estado vacio`() = runTest {
        every { medicationRepository.getAllMedications(1) } returns flowOf(emptyList())
        every { intakeRepository.observeHistoryIntakes(1, any(), any()) } returns flowOf(emptyList())
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.hasMedications)
        assertTrue(state.entries.isEmpty())
        assertEquals(0, state.totalInRange)

        job.cancel()
    }

    @Test
    fun `refreshNow recalcula el estado de las tomas`() = runTest {
        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(StatisticsStatus.PENDING, viewModel.uiState.value.entries.first {
            it.intake.intakeId == 2
        }.status)

        viewModel.refreshNow(now.plusMinutes(20))
        advanceUntilIdle()

        assertEquals(now.plusMinutes(20), viewModel.uiState.value.now)
        assertEquals(StatisticsStatus.MISSED, viewModel.uiState.value.entries.first {
            it.intake.intakeId == 2
        }.status)

        job.cancel()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): HistoryViewModel = HistoryViewModel(
        intakeRepository = intakeRepository,
        medicationRepository = medicationRepository,
        sessionManager = sessionManager,
        savedStateHandle = savedStateHandle,
    ).also { it.refreshNow(now) }

    private fun medication(id: Int, name: String, archived: Boolean): Medication = Medication(
        id = id,
        profileId = 1,
        name = name,
        dose = 600.0,
        unit = "mg",
        startDate = today.minusDays(30),
        deletedAt = if (archived) now else null,
    )

    private fun intake(
        id: Int,
        medicationId: Int,
        scheduledAt: LocalDateTime,
        takenAt: LocalDateTime? = null,
    ): StatisticsIntake = StatisticsIntake(
        intakeId = id,
        medicationId = medicationId,
        scheduledAt = scheduledAt,
        takenAt = takenAt,
        medicationName = if (medicationId == 2) "Omeprazol" else "Ibuprofeno",
        dose = if (medicationId == 2) 20.0 else 600.0,
        unit = "mg",
        imageUrl = null,
        isArchived = medicationId == 2,
    )
}
