package com.andres.pillremind.ui.screens.home

import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.notifications.IntakeNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de [HomeViewModel.hasActiveMedications], el estado que decide si
 * Inicio muestra el estado vacío de "sin medicamentos" o la card de "sin tomas
 * hoy".
 *
 * El valor debe ser `null` hasta la primera emisión para que la pantalla no
 * confunda "cargando" con "sin medicamentos"; a partir de ahí debe reflejar los
 * medicamentos activos del perfil y reaccionar a sus cambios.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelHasActiveMedicationsTest {

    private val profileRepository = mockk<ProfileRepository>(relaxed = true)
    private val intakeRepository = mockk<IntakeRepository>(relaxed = true)
    private val medicationRepository = mockk<MedicationRepository>()
    private val sessionManager = mockk<SessionManager>()
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val intakeNotifications = mockk<IntakeNotifications>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `hasActiveMedications es null antes de la primera emision`() {
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { medicationRepository.getMedications(1) } returns flowOf(emptyList())

        val viewModel = createViewModel()

        assertNull(
            "Sin suscriptor el estado aún no debe decidir entre vacío y tomas",
            viewModel.hasActiveMedications.value
        )
    }

    @Test
    fun `hasActiveMedications es false cuando el perfil no tiene medicamentos`() = runTest {
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { medicationRepository.getMedications(1) } returns flowOf(emptyList())

        val viewModel = createViewModel()

        assertFalse(viewModel.hasActiveMedications.first { it != null }!!)
    }

    @Test
    fun `hasActiveMedications es true cuando el perfil tiene un medicamento activo`() = runTest {
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { medicationRepository.getMedications(1) } returns flowOf(listOf(medication()))

        val viewModel = createViewModel()

        assertTrue(viewModel.hasActiveMedications.first { it != null }!!)
    }

    @Test
    fun `hasActiveMedications cambia al crear o archivar el ultimo medicamento`() = runTest {
        val medications = MutableStateFlow(emptyList<Medication>())
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { medicationRepository.getMedications(1) } returns medications
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hasActiveMedications.collect()
        }
        assertEquals(false, viewModel.hasActiveMedications.value)

        medications.value = listOf(medication())

        assertEquals(
            "Al crear el primer medicamento Inicio debe abandonar el estado vacío",
            true,
            viewModel.hasActiveMedications.value
        )

        medications.value = emptyList()

        assertEquals(
            "Al archivar el último medicamento Inicio debe volver al estado vacío",
            false,
            viewModel.hasActiveMedications.value
        )
    }

    @Test
    fun `hasActiveMedications consulta el perfil nuevo al cambiar de perfil`() = runTest {
        val activeProfile = MutableStateFlow<Int?>(1)
        every { sessionManager.activeProfileId } returns activeProfile
        every { medicationRepository.getMedications(1) } returns flowOf(listOf(medication()))
        every { medicationRepository.getMedications(2) } returns flowOf(emptyList())
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hasActiveMedications.collect()
        }
        assertEquals(true, viewModel.hasActiveMedications.value)

        activeProfile.value = 2

        assertEquals(
            "El perfil 2 no tiene medicamentos y no debe heredar el estado del 1",
            false,
            viewModel.hasActiveMedications.value
        )
        verify(exactly = 1) { medicationRepository.getMedications(1) }
        verify(exactly = 1) { medicationRepository.getMedications(2) }
    }

    private fun createViewModel(): HomeViewModel = HomeViewModel(
        profileRepository = profileRepository,
        intakeRepository = intakeRepository,
        medicationRepository = medicationRepository,
        sessionManager = sessionManager,
        reminderScheduler = reminderScheduler,
        intakeNotifications = intakeNotifications
    )

    private fun medication(profileId: Int = 1): Medication = Medication(
        id = 1,
        profileId = profileId,
        name = "Ibuprofeno",
        dose = 600.0,
        unit = "mg",
        startDate = LocalDate.of(2026, 11, 1)
    )
}
