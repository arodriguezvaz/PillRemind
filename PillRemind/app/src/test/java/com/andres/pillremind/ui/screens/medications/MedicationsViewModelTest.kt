package com.andres.pillremind.ui.screens.medications

import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.MedicationRepository
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del estado que decide entre el estado vacío de la pantalla y la card
 * de "sin resultados" durante una búsqueda.
 *
 * [MedicationsUiState.hasMedications] debe describir el perfil completo, no el
 * resultado filtrado: si el perfil tiene medicamentos pero la búsqueda no
 * encuentra ninguno, la pantalla debe mostrar `medication_search_no_results` y
 * no el estado vacío de "aún no tienes medicamentos".
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MedicationsViewModelTest {

    private val medicationRepository = mockk<MedicationRepository>()
    private val sessionManager = mockk<SessionManager>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sin medicamentos termina de cargar con la lista vacia`() = runTest {
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { medicationRepository.getMedications(1) } returns flowOf(emptyList())

        val state = createViewModel().uiState.first { !it.isLoading }

        assertEquals(emptyList<Medication>(), state.medications)
        assertFalse(state.hasMedications)
    }

    @Test
    fun `una busqueda sin coincidencias conserva hasMedications`() = runTest {
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { medicationRepository.getMedications(1) } returns
            flowOf(listOf(medication(name = "Ibuprofeno")))
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("paracetamol")

        val state = viewModel.uiState.first { !it.isLoading && it.medications.isEmpty() }
        assertEquals("paracetamol", viewModel.searchQuery.value)
        assertTrue(
            "El perfil sí tiene medicamentos; la UI debe mostrar 'No hay resultados'",
            state.hasMedications
        )
    }

    @Test
    fun `la busqueda filtra por nombre sin distinguir mayusculas`() = runTest {
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { medicationRepository.getMedications(1) } returns flowOf(
            listOf(
                medication(id = 1, name = "Ibuprofeno"),
                medication(id = 2, name = "Paracetamol")
            )
        )
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("IBU")

        val state = viewModel.uiState.first { !it.isLoading && it.medications.size == 1 }
        assertEquals(listOf("Ibuprofeno"), state.medications.map { it.name })
        assertTrue(state.hasMedications)
    }

    @Test
    fun `sin perfil activo termina de cargar sin medicamentos`() = runTest {
        every { sessionManager.activeProfileId } returns flowOf(null)

        val state = createViewModel().uiState.first { !it.isLoading }

        assertEquals(emptyList<Medication>(), state.medications)
        assertFalse(state.hasMedications)
    }

    private fun createViewModel(): MedicationsViewModel = MedicationsViewModel(
        medicationRepository = medicationRepository,
        sessionManager = sessionManager
    )

    private fun medication(id: Int = 1, name: String): Medication = Medication(
        id = id,
        profileId = 1,
        name = name,
        dose = 600.0,
        unit = "mg",
        startDate = LocalDate.of(2026, 11, 1)
    )
}
