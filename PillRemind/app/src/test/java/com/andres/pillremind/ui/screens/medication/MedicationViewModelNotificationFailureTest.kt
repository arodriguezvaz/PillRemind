package com.andres.pillremind.ui.screens.medication

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
import com.andres.pillremind.notifications.IntakeNotifications
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de los caminos de error de las operaciones que cancelan notificaciones.
 *
 * Se ejecutan con Robolectric porque el ViewModel registra los fallos con
 * `android.util.Log`, que en un test JVM puro es un stub que lanza
 * `RuntimeException`. El invariante que se comprueba es que una operación de
 * base de datos fallida no cancele notificaciones ni invoque el callback de
 * éxito: los datos siguen intactos y sus avisos deben seguir siendo válidos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MedicationViewModelNotificationFailureTest {

    private val medicationRepository = mockk<MedicationRepository>(relaxed = true)
    private val scheduleRepository = mockk<ScheduleRepository>(relaxed = true)
    private val intakeRepository = mockk<IntakeRepository>()
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val intakeNotifications = mockk<IntakeNotifications>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private lateinit var viewModel: MedicationViewModel

    private val medication = Medication(
        id = 3,
        profileId = 1,
        name = "Enalapril",
        dose = 10.0,
        unit = "mg",
        startDate = LocalDate.of(2026, 3, 12)
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
            intakeNotifications = intakeNotifications,
            sessionManager = sessionManager,
            context = context,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `si falla el archivado no cancela notificaciones ni confirma el exito`() = runTest {
        coEvery {
            intakeRepository.getFutureIntakesByMedicationId(medication.id, any())
        } returns emptyList()
        coEvery { intakeRepository.getIntakeIdsByMedicationId(medication.id) } returns listOf(5)
        coEvery {
            medicationRepository.softDeleteMedication(medication.id, any(), any())
        } throws IllegalStateException("fallo de base de datos")
        var archived = false

        viewModel.archiveMedication(medication) { archived = true }

        coVerify(exactly = 0) { intakeNotifications.cancelAll(any<Collection<Int>>()) }
        verify(exactly = 0) { reminderScheduler.cancelAlarms(any()) }
        assertFalse(archived)
    }

    @Test
    fun `si falla el borrado definitivo no cancela notificaciones ni borra la imagen`() = runTest {
        val image = File.createTempFile("medication", ".jpg").apply { writeText("foto") }
        val medicationWithImage = medication.copy(imageUrl = image.absolutePath)
        coEvery {
            intakeRepository.getFutureIntakesByMedicationId(medication.id, any())
        } returns emptyList()
        coEvery { intakeRepository.getIntakeIdsByMedicationId(medication.id) } returns listOf(5)
        coEvery {
            medicationRepository.deleteMedication(medicationWithImage)
        } throws IllegalStateException("fallo de base de datos")
        var deleted = false

        viewModel.deleteMedicationPermanently(medicationWithImage) { deleted = true }

        coVerify(exactly = 0) { intakeNotifications.cancelAll(any<Collection<Int>>()) }
        assertTrue(image.exists())
        assertFalse(deleted)
    }

    @Test
    fun `si falla la actualizacion no cancela las notificaciones anteriores`() = runTest {
        val editViewModel = MedicationViewModel(
            savedStateHandle = SavedStateHandle(mapOf("medicationId" to medication.id)),
            medicationRepository = medicationRepository,
            scheduleRepository = scheduleRepository,
            reminderScheduler = reminderScheduler,
            intakeRepository = intakeRepository,
            intakeNotifications = intakeNotifications,
            sessionManager = sessionManager,
            context = context,
        )
        coEvery { medicationRepository.getMedicationByIdOnce(medication.id) } returns medication
        coEvery {
            intakeRepository.getFutureIntakesByMedicationId(medication.id, any())
        } returns emptyList()
        coEvery { intakeRepository.getIntakeIdsByMedicationId(medication.id) } returns listOf(5)
        coEvery {
            medicationRepository.updateMedicationWithSchedules(any(), any(), any())
        } throws IllegalStateException("fallo de base de datos")
        var saved = false

        editViewModel.updateMedication(
            name = medication.name,
            dose = medication.dose,
            unit = medication.unit,
            startDate = medication.startDate,
            scheduleRules = emptyList(),
            times = emptyList(),
            isChronic = true,
        ) { saved = true }

        coVerify(exactly = 0) { intakeNotifications.cancelAll(any<Collection<Int>>()) }
        verify(exactly = 0) { reminderScheduler.cancelAlarms(any()) }
        assertFalse(saved)
    }

    @Test
    fun `si falla la cancelacion de notificaciones el archivado se completa igualmente`() = runTest {
        coEvery {
            intakeRepository.getFutureIntakesByMedicationId(medication.id, any())
        } returns emptyList()
        coEvery { intakeRepository.getIntakeIdsByMedicationId(medication.id) } returns listOf(5)
        coEvery {
            medicationRepository.softDeleteMedication(medication.id, any(), any())
        } just Runs
        coEvery {
            intakeNotifications.cancelAll(any<Collection<Int>>())
        } throws IllegalStateException("fallo del gestor de notificaciones")
        var archived = false

        viewModel.archiveMedication(medication) { archived = true }

        coVerify { intakeNotifications.cancelAll(listOf(5)) }
        assertTrue(archived)
    }
}
