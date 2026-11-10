package com.andres.pillremind.ui.screens.importer

import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.R
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.pdf.MedicationImportFileStore
import com.andres.pillremind.di.IoDispatcher
import com.andres.pillremind.domain.model.ImportedMedication
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.MedicationImportError
import com.andres.pillremind.domain.model.MedicationImportResult
import com.andres.pillremind.domain.model.MedicationImportStatus
import com.andres.pillremind.domain.model.MedicationImportWarning
import com.andres.pillremind.domain.model.MedicationWithSchedules
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.ScheduleRule
import com.andres.pillremind.domain.parsing.MedicationNameKey
import com.andres.pillremind.domain.repository.MedicationImportRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Etapa actual de la importación.
 */
enum class MedicationImportStage {
    PICK_FILE,
    READING,
    REVIEW,
    SAVED
}

/**
 * Medicamento editable de la pantalla de revisión.
 *
 * @property key Identificador efímero de la fila.
 * @property existingName Nombre del medicamento que ya existe, si lo hay.
 * @property existingMedicationId ID del medicamento existente, si se puede abrir.
 * @property existingArchived Indica si el medicamento existente está archivado.
 * @property duplicateInPdf Indica que la misma línea aparece dos veces en el PDF.
 */
data class MedicationImportItemUi(
    val key: String,
    val name: String,
    val doseText: String,
    val unit: String,
    val scheduleRules: List<ScheduleRule>,
    val times: List<LocalTime>,
    val isChronic: Boolean,
    val status: MedicationImportStatus,
    val selected: Boolean,
    val existingName: String? = null,
    val existingMedicationId: Int? = null,
    val existingArchived: Boolean = false,
    val duplicateInPdf: Boolean = false,
    val forced: Boolean = false,
    val warnings: List<MedicationImportWarning> = emptyList()
)

/**
 * Estado de la pantalla de importación.
 */
data class MedicationImportUiState(
    val stage: MedicationImportStage = MedicationImportStage.PICK_FILE,
    val items: List<MedicationImportItemUi> = emptyList(),
    val startDate: LocalDate = LocalDate.now(),
    @StringRes val errorRes: Int? = null,
    val isSaving: Boolean = false,
    val savedCount: Int = 0
) {
    val selectedCount: Int
        get() = items.count { it.status == MedicationImportStatus.NEW && it.selected }
}

/**
 * Orquesta la lectura del PDF, la revisión y el alta masiva.
 */
@HiltViewModel
class MedicationImportViewModel @Inject constructor(
    private val importRepository: MedicationImportRepository,
    private val medicationRepository: MedicationRepository,
    private val fileStore: MedicationImportFileStore,
    private val reminderScheduler: ReminderScheduler,
    private val sessionManager: SessionManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(MedicationImportUiState())
    val state: StateFlow<MedicationImportUiState> = _state.asStateFlow()

    private var existingMedications: List<Medication> = emptyList()

    /**
     * Lee el PDF elegido y prepara la revisión.
     */
    fun onPdfSelected(uri: Uri) {
        if (_state.value.stage == MedicationImportStage.READING) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    stage = MedicationImportStage.READING,
                    items = emptyList(),
                    errorRes = null
                )
            }

            try {
                val size = withContext(ioDispatcher) { fileStore.size(uri) }
                if (size != null && size > MAX_PDF_BYTES) {
                    failReading(R.string.medication_import_error_too_large)
                    return@launch
                }

                val result = withContext(ioDispatcher) {
                    val input = fileStore.openInput(uri)
                    if (input == null) {
                        MedicationImportResult.Error(MedicationImportError.IO)
                    } else {
                        input.use { importRepository.parsePdf(it) }
                    }
                }

                when (result) {
                    is MedicationImportResult.Success -> loadReview(result.medications)
                    is MedicationImportResult.Error -> failReading(result.error.messageRes())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failReading(R.string.medication_import_error_generic)
            }
        }
    }

    /**
     * Carga el perfil y marca los medicamentos que ya existen.
     */
    private suspend fun loadReview(imported: List<ImportedMedication>) {
        val profileId = sessionManager.activeProfileId.firstOrNull()
        if (profileId == null) {
            failReading(R.string.medication_import_error_no_profile)
            return
        }

        existingMedications = medicationRepository.getAllMedications(profileId).firstOrNull()
            ?: emptyList()

        val items = buildItems(imported, existingMedications)
        _state.update {
            it.copy(
                stage = MedicationImportStage.REVIEW,
                items = items,
                errorRes = null
            )
        }
    }

    /**
     * Construye las filas de revisión y resuelve duplicados.
     */
    private fun buildItems(
        imported: List<ImportedMedication>,
        existing: List<Medication>
    ): List<MedicationImportItemUi> {
        val seenInPdf = mutableSetOf<String>()

        return imported.mapIndexed { index, medication ->
            val existingMatch = existing
                .firstOrNull { it.deletedAt == null && MedicationNameKey.matches(it.name, medication.name) }
                ?: existing.firstOrNull { MedicationNameKey.matches(it.name, medication.name) }

            val (brand, strength) = MedicationNameKey.brandAndStrength(medication.name)
            val pdfKey = "$brand|${strength.orEmpty()}"
            val duplicateInPdf = !seenInPdf.add(pdfKey)

            val status = when {
                existingMatch != null -> MedicationImportStatus.EXISTING
                duplicateInPdf -> MedicationImportStatus.EXISTING
                medication.doseText.isBlank() ||
                    medication.unit.isBlank() ||
                    medication.scheduleRules.isEmpty() ||
                    medication.times.isEmpty() -> MedicationImportStatus.INCOMPLETE

                else -> MedicationImportStatus.NEW
            }

            MedicationImportItemUi(
                key = "import-$index",
                name = medication.name,
                doseText = medication.doseText,
                unit = medication.unit,
                scheduleRules = medication.scheduleRules.ifEmpty {
                    listOf(ScheduleRule.EveryNDays(medication.everyNDays.coerceAtLeast(1)))
                },
                times = medication.times,
                isChronic = medication.isChronic,
                status = status,
                selected = status == MedicationImportStatus.NEW,
                existingName = existingMatch?.name,
                existingMedicationId = existingMatch?.id,
                existingArchived = existingMatch?.deletedAt != null,
                duplicateInPdf = duplicateInPdf && existingMatch == null,
                warnings = medication.warnings
            )
        }
    }

    /**
     * Marca o desmarca un medicamento nuevo.
     */
    fun onToggleItem(key: String, selected: Boolean) {
        _state.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.key == key && item.status == MedicationImportStatus.NEW) {
                        item.copy(selected = selected)
                    } else {
                        item
                    }
                }
            )
        }
    }

    /**
     * Fuerza la inclusión de un medicamento que ya existe.
     *
     * La UI lo pide después de confirmar el aviso que aparece al tocar el
     * checkbox de un duplicado.
     */
    fun forceSelect(key: String) {
        _state.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.key == key) {
                        item.copy(
                            status = MedicationImportStatus.NEW,
                            selected = true,
                            existingMedicationId = null,
                            existingArchived = false,
                            duplicateInPdf = false,
                            existingName = null,
                            forced = true,
                            warnings = item.warnings - MedicationImportWarning.DUPLICATE_IN_PDF
                        )
                    } else {
                        item
                    }
                }
            )
        }
    }

    /**
     * Aplica los cambios de la hoja de edición.
     */
    fun onEditItem(
        key: String,
        name: String,
        doseText: String,
        unit: String,
        scheduleRules: List<ScheduleRule>,
        times: List<LocalTime>,
        isChronic: Boolean
    ) {
        _state.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.key != key) return@map item

                    val normalizedName = name.trim()
                    val dose = doseText.trim()
                    val normalizedUnit = unit.trim()
                    val existingMatch = existingMedications.firstOrNull {
                        MedicationNameKey.matches(it.name, normalizedName)
                    }
                    val valid = dose.isNotBlank() &&
                        normalizedUnit.isNotBlank() &&
                        scheduleRules.isNotEmpty() &&
                        times.isNotEmpty()
                    val status = when {
                        item.forced && valid -> MedicationImportStatus.NEW
                        item.forced -> MedicationImportStatus.INCOMPLETE
                        existingMatch != null -> MedicationImportStatus.EXISTING
                        !valid -> MedicationImportStatus.INCOMPLETE
                        else -> MedicationImportStatus.NEW
                    }
                    val effectiveExisting = if (item.forced) null else existingMatch

                    item.copy(
                        name = normalizedName,
                        doseText = dose,
                        unit = normalizedUnit,
                        scheduleRules = scheduleRules,
                        times = times,
                        isChronic = isChronic,
                        status = status,
                        selected = status == MedicationImportStatus.NEW &&
                            (item.forced || item.selected ||
                                item.status == MedicationImportStatus.INCOMPLETE),
                        existingName = effectiveExisting?.name,
                        existingMedicationId = effectiveExisting?.id,
                        existingArchived = effectiveExisting?.deletedAt != null,
                        duplicateInPdf = false,
                        forced = item.forced
                    )
                }
            )
        }
    }

    fun onStartDateChange(date: LocalDate) {
        _state.update { it.copy(startDate = date) }
    }

    /**
     * Guarda los medicamentos nuevos en una sola transacción.
     */
    fun save() {
        val current = _state.value
        if (current.isSaving) return

        val selected = current.items.filter {
            it.status == MedicationImportStatus.NEW && it.selected
        }
        if (selected.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorRes = null) }

            val profileId = sessionManager.activeProfileId.firstOrNull()
            if (profileId == null) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorRes = R.string.medication_import_error_no_profile
                    )
                }
                return@launch
            }

            val invalid = selected.any { item ->
                val dose = item.doseText.replace(',', '.').toDoubleOrNull()
                dose == null || dose <= 0 || item.unit.isBlank() ||
                    item.scheduleRules.isEmpty() || item.times.isEmpty()
            }
            if (invalid) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorRes = R.string.medication_import_error_generic
                    )
                }
                return@launch
            }

            val now = LocalDateTime.now()
            val bundles = selected.map { item ->
                val dose = item.doseText.replace(',', '.').toDouble()

                MedicationWithSchedules(
                    medication = Medication(
                        profileId = profileId,
                        name = item.name.trim(),
                        dose = dose,
                        unit = item.unit.trim(),
                        isChronic = item.isChronic,
                        startDate = current.startDate,
                        endDate = null,
                        createdAt = now,
                        updatedAt = now
                    ),
                    schedules = item.scheduleRules.flatMap { rule ->
                        item.times.map { time ->
                            Schedule(
                                medicationId = 0,
                                rule = rule,
                                time = time
                            )
                        }
                    }
                )
            }

            if (bundles.isEmpty()) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorRes = R.string.medication_import_error_generic
                    )
                }
                return@launch
            }

            val saved = try {
                medicationRepository.insertMedicationsWithSchedules(bundles)
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("PillDebug", "No se pudieron importar los medicamentos", e)
                false
            }

            if (!saved) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorRes = R.string.medication_import_error_save
                    )
                }
                return@launch
            }

            try {
                reminderScheduler.generateWindow()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("PillDebug", "No se pudieron generar las tomas importadas", e)
            }

            _state.update {
                it.copy(
                    stage = MedicationImportStage.SAVED,
                    isSaving = false,
                    savedCount = bundles.size,
                    errorRes = null
                )
            }
        }
    }

    /**
     * Vuelve al selector de PDF tras un error.
     */
    fun dismissError() {
        _state.update { it.copy(errorRes = null) }
    }

    /**
     * Descarta la revisión y vuelve al inicio.
     */
    fun reset() {
        existingMedications = emptyList()
        _state.value = MedicationImportUiState()
    }

    private fun failReading(@StringRes messageRes: Int) {
        _state.update {
            it.copy(
                stage = MedicationImportStage.PICK_FILE,
                isSaving = false,
                errorRes = messageRes
            )
        }
    }

    private fun MedicationImportError.messageRes(): Int = when (this) {
        MedicationImportError.NO_TEXT -> R.string.medication_import_error_no_text
        MedicationImportError.NO_MEDICATIONS -> R.string.medication_import_error_no_medications
        MedicationImportError.ENCRYPTED -> R.string.medication_import_error_encrypted
        MedicationImportError.TOO_LARGE -> R.string.medication_import_error_too_large
        MedicationImportError.TOO_MANY_PAGES -> R.string.medication_import_error_too_many_pages
        MedicationImportError.CORRUPT -> R.string.medication_import_error_corrupt
        MedicationImportError.IO -> R.string.medication_import_error_io
    }

    private companion object {
        const val MAX_PDF_BYTES = 10L * 1024L * 1024L
    }
}
