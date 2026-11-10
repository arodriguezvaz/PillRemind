package com.andres.pillremind.ui.screens.importer

import android.net.Uri
import android.util.Log
import com.andres.pillremind.R
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.pdf.MedicationImportFileStore
import com.andres.pillremind.domain.model.ImportedMedication
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.MedicationImportError
import com.andres.pillremind.domain.model.MedicationImportResult
import com.andres.pillremind.domain.model.MedicationImportStatus
import com.andres.pillremind.domain.model.MedicationWithSchedules
import com.andres.pillremind.domain.model.ScheduleRule
import com.andres.pillremind.domain.model.WeekDay
import com.andres.pillremind.domain.repository.MedicationImportRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.time.LocalDate
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
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de la revisión y el alta masiva de la importación desde PDF.
 *
 * Los colaboradores se declaran sin `relaxed` para que cualquier llamada fuera
 * del contrato esperado haga fallar el test en lugar de devolver un valor vacío.
 * La excepción es [ReminderScheduler]: sus argumentos por defecto confunden a
 * MockK al verificar, así que se relaja y se comprueban sus llamadas de forma
 * explícita. [Uri] también se relaja porque solo se usa como identificador.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MedicationImportViewModelTest {

    private val importRepository = mockk<MedicationImportRepository>()
    private val medicationRepository = mockk<MedicationRepository>()
    private val fileStore = mockk<MedicationImportFileStore>()
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val sessionManager = mockk<SessionManager>()
    private val uri = mockk<Uri>(relaxed = true)
    private val ioDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: MedicationImportViewModel

    private val existing = Medication(
        id = 1,
        profileId = 1,
        name = "ENALAPRIL 5 MG",
        dose = 5.0,
        unit = "mg",
        startDate = LocalDate.of(2026, 1, 1)
    )

    private val imported = listOf(
        ImportedMedication(
            name = "ENALAPRIL 5MG",
            doseText = "1",
            unit = "comprimidos",
            times = listOf(LocalTime.of(9, 0)),
            isChronic = true
        ),
        ImportedMedication(
            name = "IBUPROFENO 600 MG",
            doseText = "1",
            unit = "comprimidos",
            times = listOf(LocalTime.of(9, 0)),
            isChronic = false
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        // android.util.Log es un stub que lanza en tests JVM: se neutraliza para
        // poder comprobar las ramas de error que registran el fallo.
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { fileStore.size(uri) } returns 1000L
        every { fileStore.openInput(uri) } returns ByteArrayInputStream(byteArrayOf(1))
        every { medicationRepository.getAllMedications(1) } returns flowOf(listOf(existing))
        coEvery { importRepository.parsePdf(any()) } returns
            MedicationImportResult.Success(imported)
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()

        viewModel = MedicationImportViewModel(
            importRepository = importRepository,
            medicationRepository = medicationRepository,
            fileStore = fileStore,
            reminderScheduler = reminderScheduler,
            sessionManager = sessionManager,
            ioDispatcher = ioDispatcher
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        Dispatchers.resetMain()
    }

    @Test
    fun `marca como existente el medicamento que ya esta en el perfil`() = runTest {
        viewModel.onPdfSelected(uri)
        val state = awaitStage(MedicationImportStage.REVIEW)
        assertEquals(MedicationImportStage.REVIEW, state.stage)
        assertEquals(2, state.items.size)

        val existingItem = state.items.first { it.name == "ENALAPRIL 5MG" }
        assertEquals(MedicationImportStatus.EXISTING, existingItem.status)
        assertFalse(existingItem.selected)
        assertEquals(1, existingItem.existingMedicationId)
        assertFalse(existingItem.existingArchived)

        val newItem = state.items.first { it.name == "IBUPROFENO 600 MG" }
        assertEquals(MedicationImportStatus.NEW, newItem.status)
        assertTrue(newItem.selected)
    }

    @Test
    fun `guarda solo los nuevos y regenera las alarmas una vez`() = runTest {
        val bundles = slot<List<MedicationWithSchedules>>()
        coEvery {
            medicationRepository.insertMedicationsWithSchedules(capture(bundles))
        } returns listOf(10)

        val startDate = LocalDate.of(2026, 12, 1)
        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        viewModel.onStartDateChange(startDate)
        viewModel.save()
        val state = awaitStage(MedicationImportStage.SAVED)
        assertEquals(MedicationImportStage.SAVED, state.stage)
        assertEquals(1, state.savedCount)

        val saved = bundles.captured
        assertEquals(1, saved.size)
        assertEquals("IBUPROFENO 600 MG", saved.single().medication.name)
        assertEquals(1.0, saved.single().medication.dose, 0.001)
        assertEquals("comprimidos", saved.single().medication.unit)
        assertEquals(startDate, saved.single().medication.startDate)
        assertEquals(listOf(LocalTime.of(9, 0)), saved.single().schedules.map { it.time })
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
        coVerify(exactly = 0) { medicationRepository.updateMedication(any()) }
    }

    @Test
    fun `guarda una pauta semanal con varios dias`() = runTest {
        val bundles = slot<List<MedicationWithSchedules>>()
        coEvery {
            medicationRepository.insertMedicationsWithSchedules(capture(bundles))
        } returns listOf(10)

        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        val newItem = viewModel.state.value.items.first { it.name == "IBUPROFENO 600 MG" }
        viewModel.onEditItem(
            key = newItem.key,
            name = newItem.name,
            doseText = newItem.doseText,
            unit = newItem.unit,
            scheduleRules = listOf(
                ScheduleRule.Weekly(WeekDay.MONDAY),
                ScheduleRule.Weekly(WeekDay.WEDNESDAY)
            ),
            times = listOf(LocalTime.of(9, 0)),
            isChronic = false
        )
        viewModel.save()
        awaitStage(MedicationImportStage.SAVED)

        assertEquals(
            listOf(
                ScheduleRule.Weekly(WeekDay.MONDAY),
                ScheduleRule.Weekly(WeekDay.WEDNESDAY)
            ),
            bundles.captured.single().schedules.map { it.rule }
        )
    }

    @Test
    fun `forzar un duplicado lo selecciona para guardarlo`() = runTest {
        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)

        val existingItem = viewModel.state.value.items.first { it.name == "ENALAPRIL 5MG" }
        viewModel.forceSelect(existingItem.key)

        val forced = viewModel.state.value.items.first { it.key == existingItem.key }
        assertEquals(MedicationImportStatus.NEW, forced.status)
        assertTrue(forced.selected)
        assertNull(forced.existingMedicationId)
        assertNull(forced.existingName)
    }

    @Test
    fun `un pdf sin texto vuelve al selector con un error legible`() = runTest {
        coEvery { importRepository.parsePdf(any()) } returns
            MedicationImportResult.Error(MedicationImportError.NO_TEXT)

        viewModel.onPdfSelected(uri)
        val state = awaitError()
        assertEquals(MedicationImportStage.PICK_FILE, state.stage)
        assertEquals(R.string.medication_import_error_no_text, state.errorRes)
    }

    @Test
    fun `si el proveedor no abre el archivo muestra un error de lectura`() = runTest {
        every { fileStore.openInput(uri) } returns null

        viewModel.onPdfSelected(uri)

        val state = awaitError()
        assertEquals(MedicationImportStage.PICK_FILE, state.stage)
        assertEquals(R.string.medication_import_error_io, state.errorRes)
        coVerify(exactly = 0) { importRepository.parsePdf(any()) }
    }

    @Test
    fun `editar un nuevo con el nombre de uno existente lo marca como duplicado`() = runTest {
        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)

        val newItem = viewModel.state.value.items.first { it.name == "IBUPROFENO 600 MG" }
        viewModel.onEditItem(
            key = newItem.key,
            name = "ENALAPRIL 5MG",
            doseText = "1",
            unit = "comprimidos",
            scheduleRules = listOf(ScheduleRule.EveryNDays(1)),
            times = listOf(LocalTime.of(9, 0)),
            isChronic = true
        )

        val edited = viewModel.state.value.items.first { it.key == newItem.key }
        assertEquals(MedicationImportStatus.EXISTING, edited.status)
        assertFalse(edited.selected)
        assertNotNull(edited.existingMedicationId)
    }

    @Test
    fun `marca como existente un medicamento archivado y lo deja desmarcado`() = runTest {
        val archived = existing.copy(
            id = 7,
            deletedAt = LocalDateTime.of(2026, 9, 1, 12, 0)
        )
        every { medicationRepository.getAllMedications(1) } returns flowOf(listOf(archived))

        viewModel.onPdfSelected(uri)
        val state = awaitStage(MedicationImportStage.REVIEW)

        val item = state.items.first { it.name == "ENALAPRIL 5MG" }
        assertEquals(MedicationImportStatus.EXISTING, item.status)
        assertFalse(item.selected)
        assertTrue(item.existingArchived)
        assertEquals(7, item.existingMedicationId)
        assertEquals("ENALAPRIL 5 MG", item.existingName)
    }

    @Test
    fun `prefiere el medicamento activo frente al archivado con la misma marca`() = runTest {
        val archived = existing.copy(
            id = 7,
            deletedAt = LocalDateTime.of(2026, 9, 1, 12, 0)
        )
        every { medicationRepository.getAllMedications(1) } returns
            flowOf(listOf(archived, existing))

        viewModel.onPdfSelected(uri)
        val state = awaitStage(MedicationImportStage.REVIEW)

        val item = state.items.first { it.name == "ENALAPRIL 5MG" }
        assertEquals(MedicationImportStatus.EXISTING, item.status)
        assertEquals(1, item.existingMedicationId)
        assertFalse(item.existingArchived)
    }

    @Test
    fun `forzar un archivado limpia la marca de archivado`() = runTest {
        val archived = existing.copy(
            id = 7,
            deletedAt = LocalDateTime.of(2026, 9, 1, 12, 0)
        )
        every { medicationRepository.getAllMedications(1) } returns flowOf(listOf(archived))

        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        val item = viewModel.state.value.items.first { it.name == "ENALAPRIL 5MG" }
        viewModel.forceSelect(item.key)

        val forced = viewModel.state.value.items.first { it.key == item.key }
        assertEquals(MedicationImportStatus.NEW, forced.status)
        assertTrue(forced.selected)
        assertFalse(forced.existingArchived)
        assertNull(forced.existingMedicationId)
    }

    @Test
    fun `marca como repetido dentro del pdf el segundo medicamento identico`() = runTest {
        every { medicationRepository.getAllMedications(1) } returns flowOf(emptyList())
        coEvery { importRepository.parsePdf(any()) } returns MedicationImportResult.Success(
            listOf(
                imported[0],
                imported[0].copy(name = "Enalapril 5 mg")
            )
        )

        viewModel.onPdfSelected(uri)
        val state = awaitStage(MedicationImportStage.REVIEW)

        assertEquals(2, state.items.size)
        val first = state.items[0]
        val second = state.items[1]
        assertEquals(MedicationImportStatus.NEW, first.status)
        assertTrue(first.selected)
        assertFalse(first.duplicateInPdf)
        assertEquals(MedicationImportStatus.EXISTING, second.status)
        assertFalse(second.selected)
        assertTrue(second.duplicateInPdf)
        assertNull(second.existingName)
    }

    @Test
    fun `forzar un repetido del pdf lo convierte en nuevo`() = runTest {
        every { medicationRepository.getAllMedications(1) } returns flowOf(emptyList())
        coEvery { importRepository.parsePdf(any()) } returns MedicationImportResult.Success(
            listOf(imported[0], imported[0].copy(name = "Enalapril 5 mg"))
        )

        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        val second = viewModel.state.value.items[1]
        viewModel.forceSelect(second.key)

        val forced = viewModel.state.value.items[1]
        assertEquals(MedicationImportStatus.NEW, forced.status)
        assertTrue(forced.selected)
        assertFalse(forced.duplicateInPdf)
    }

    @Test
    fun `guarda el duplicado forzado y desmarca el resto`() = runTest {
        val bundles = slot<List<MedicationWithSchedules>>()
        coEvery {
            medicationRepository.insertMedicationsWithSchedules(capture(bundles))
        } returns listOf(20)

        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        val existingItem = viewModel.state.value.items.first { it.name == "ENALAPRIL 5MG" }
        val newItem = viewModel.state.value.items.first { it.name == "IBUPROFENO 600 MG" }

        viewModel.onToggleItem(newItem.key, false)
        viewModel.forceSelect(existingItem.key)
        viewModel.save()

        val state = awaitStage(MedicationImportStage.SAVED)
        assertEquals(1, state.savedCount)
        assertEquals(1, bundles.captured.size)
        assertEquals("ENALAPRIL 5MG", bundles.captured.single().medication.name)
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `rechaza un pdf que supera el limite sin abrirlo`() = runTest {
        every { fileStore.size(uri) } returns MAX_PDF_BYTES + 1

        viewModel.onPdfSelected(uri)
        val state = awaitError()
        assertEquals(MedicationImportStage.PICK_FILE, state.stage)
        assertEquals(R.string.medication_import_error_too_large, state.errorRes)
        assertTrue(state.items.isEmpty())
        verify(exactly = 0) { fileStore.openInput(uri) }
        coVerify(exactly = 0) { importRepository.parsePdf(any()) }
    }

    @Test
    fun `acepta un pdf justo en el limite de tamano`() = runTest {
        every { fileStore.size(uri) } returns MAX_PDF_BYTES

        viewModel.onPdfSelected(uri)
        val state = awaitStage(MedicationImportStage.REVIEW)

        assertEquals(2, state.items.size)
        coVerify(exactly = 1) { importRepository.parsePdf(any()) }
    }

    @Test
    fun `sin perfil activo falla sin consultar los medicamentos`() = runTest {
        every { sessionManager.activeProfileId } returns flowOf(null)

        viewModel.onPdfSelected(uri)
        val state = awaitError()
        assertEquals(MedicationImportStage.PICK_FILE, state.stage)
        assertEquals(R.string.medication_import_error_no_profile, state.errorRes)
        coVerify(exactly = 0) { medicationRepository.getAllMedications(any()) }
    }

    @Test
    fun `si el perfil desaparece antes de guardar no inserta nada`() = runTest {
        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        every { sessionManager.activeProfileId } returns flowOf(null)

        viewModel.save()

        val state = awaitError()
        assertEquals(R.string.medication_import_error_no_profile, state.errorRes)
        assertFalse(state.isSaving)
        assertEquals(MedicationImportStage.REVIEW, state.stage)
        coVerify(exactly = 0) { medicationRepository.insertMedicationsWithSchedules(any()) }
        coVerify(exactly = 0) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `si falla el guardado no regenera alarmas y mantiene la revision`() = runTest {
        coEvery { medicationRepository.insertMedicationsWithSchedules(any()) } answers {
            throw IllegalStateException("fallo simulado de base de datos")
        }

        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        val before = viewModel.state.value
        viewModel.save()

        val state = awaitError()
        assertEquals(R.string.medication_import_error_save, state.errorRes)
        assertEquals(MedicationImportStage.REVIEW, state.stage)
        assertFalse(state.isSaving)
        assertEquals(before.items.size, state.items.size)
        assertEquals(1, state.selectedCount)
        coVerify(exactly = 0) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `descartar el error conserva los elementos revisados`() = runTest {
        coEvery { medicationRepository.insertMedicationsWithSchedules(any()) } answers {
            throw IllegalStateException("fallo simulado de base de datos")
        }

        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        viewModel.save()
        awaitError()
        val before = viewModel.state.value.items

        viewModel.dismissError()

        val after = viewModel.state.value
        assertNull(after.errorRes)
        assertEquals(MedicationImportStage.REVIEW, after.stage)
        assertEquals(before.map { it.key }, after.items.map { it.key })
        assertEquals(before.map { it.selected }, after.items.map { it.selected })
    }

    @Test
    fun `una dosis no numerica bloquea el guardado`() = runTest {
        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        val newItem = viewModel.state.value.items.first { it.name == "IBUPROFENO 600 MG" }
        viewModel.onEditItem(
            key = newItem.key,
            name = newItem.name,
            doseText = "media",
            unit = newItem.unit,
            scheduleRules = listOf(ScheduleRule.EveryNDays(1)),
            times = newItem.times,
            isChronic = newItem.isChronic
        )

        viewModel.save()

        val state = awaitError()
        assertEquals(R.string.medication_import_error_generic, state.errorRes)
        assertFalse(state.isSaving)
        coVerify(exactly = 0) { medicationRepository.insertMedicationsWithSchedules(any()) }
        coVerify(exactly = 0) { reminderScheduler.generateWindow(any(), any()) }
    }

    @Test
    fun `un fallo al generar alarmas no impide dar el alta por guardada`() = runTest {
        coEvery { medicationRepository.insertMedicationsWithSchedules(any()) } returns listOf(10)
        coEvery { reminderScheduler.generateWindow(any(), any()) } answers {
            throw IllegalStateException("fallo simulado de alarmas")
        }

        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)
        viewModel.save()

        val state = awaitStage(MedicationImportStage.SAVED)
        assertEquals(1, state.savedCount)
        assertNull(state.errorRes)
        coVerify(exactly = 1) { medicationRepository.insertMedicationsWithSchedules(any()) }
        coVerify(exactly = 1) { reminderScheduler.generateWindow(any(), any()) }
        verify(exactly = 1) { Log.e(any(), any(), any()) }
    }

    @Test
    fun `editar un duplicado forzado no lo vuelve a marcar como existente`() = runTest {
        viewModel.onPdfSelected(uri)
        awaitStage(MedicationImportStage.REVIEW)

        val existingItem = viewModel.state.value.items.first { it.name == "ENALAPRIL 5MG" }
        viewModel.forceSelect(existingItem.key)
        viewModel.onEditItem(
            key = existingItem.key,
            name = "ENALAPRIL 5MG",
            doseText = "2",
            unit = "comprimidos",
            scheduleRules = listOf(ScheduleRule.EveryNDays(2)),
            times = listOf(LocalTime.of(9, 0)),
            isChronic = true
        )

        val edited = viewModel.state.value.items.first { it.key == existingItem.key }
        assertEquals(MedicationImportStatus.NEW, edited.status)
        assertTrue(edited.selected)
        assertNull(edited.existingMedicationId)
    }

    private suspend fun awaitStage(stage: MedicationImportStage): MedicationImportUiState =
        withTimeout(5_000) {
            viewModel.state.first { it.stage == stage }
        }

    private suspend fun awaitError(): MedicationImportUiState =
        withTimeout(5_000) {
            viewModel.state.first { it.errorRes != null }
        }

    private companion object {
        const val MAX_PDF_BYTES = 10L * 1024L * 1024L
    }
}
