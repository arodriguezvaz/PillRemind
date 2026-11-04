package com.andres.pillremind.ui.screens.medication

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.R
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.utils.copyImageToInternalStorage
import com.andres.pillremind.data.utils.deleteInternalImage
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.ScheduleRule
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
import com.andres.pillremind.notifications.ReminderNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * ViewModel para la pantalla de medicación.
 *
 * Maneja la creación, actualización y eliminación de `Medication` y sus `Schedule`s,
 * además de programar/cancelar alarmas mediante [ReminderScheduler].
 *
 * @param savedStateHandle Handle para recuperar parámetros de navegación (por ejemplo `medicationId`).
 * @param medicationRepository Repositorio para operaciones CRUD sobre medicaciones.
 * @param scheduleRepository Repositorio para los horarios asociados a medicamentos.
 * @param reminderScheduler Coordina la generación de tomas y la programación de alarmas.
 * @param intakeRepository Repositorio de tomas.
 * @param sessionManager Gestor de sesión que expone el perfil activo.
 * @param notifications Gestor de notificaciones de recordatorio.
 * @param context Contexto usado para copiar y borrar imágenes.
 */
@HiltViewModel
class MedicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val reminderScheduler: ReminderScheduler,
    private val intakeRepository: IntakeRepository,
    sessionManager: SessionManager,
    private val notifications: ReminderNotifications,
    @ApplicationContext private val context: Context
) : ViewModel() {
    //Recupera el profileId del sessionManager
    val profileId = sessionManager.activeProfileId

    //Recupera el id del medicamento
    val medicationId = savedStateHandle.get<Int>("medicationId")

    //Si el id no es null recupera la información del medicamento
    val medication: StateFlow<Medication?> = medicationId?.let { id ->
        medicationRepository.getMedicationById(id)
    }?.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    ) ?: MutableStateFlow(null)

    val schedules: StateFlow<List<Schedule>> =
        savedStateHandle.get<Int>("medicationId")?.let { id ->
            scheduleRepository.getSchedules(id)
        }?.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        ) ?: MutableStateFlow(emptyList())

    private val _isSaving = MutableStateFlow(false)

    /** Indica que hay una operación de guardado o archivado en curso. */
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorRes = MutableStateFlow<Int?>(null)

    /** Error pendiente de mostrar en la pantalla. */
    val errorRes: StateFlow<Int?> = _errorRes.asStateFlow()

    /**
     * Crea un nuevo medicamento con sus horarios y programa las alarmas.
     *
     * La imagen se copia antes de insertar; si la inserción falla se elimina la
     * copia nueva para no dejar archivos huérfanos.
     *
     * @param onSaved Callback ejecutado cuando la medicación y sus tomas ya están guardadas.
     */
    fun addMedication(
        nombre: String,
        photoUri: Uri? = null,
        dose: Double,
        unit: String,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        scheduleRules: List<ScheduleRule>,
        times: List<LocalTime>,
        isChronic: Boolean,
        onSaved: () -> Unit = {}
    ) {
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            var newImagePath: String? = null

            try {
                if (photoUri != null) {
                    newImagePath = copyImageToInternalStorage(context, photoUri)
                    if (newImagePath == null) {
                        _errorRes.value = R.string.medication_image_error
                        return@launch
                    }
                }

                val profileId = profileId.firstOrNull()
                if (profileId == null) {
                    _errorRes.value = R.string.medication_save_error
                    return@launch
                }

                val medication = Medication(
                    name = nombre.trim(),
                    profileId = profileId,
                    imageUrl = newImagePath,
                    dose = dose,
                    unit = unit.trim(),
                    startDate = startDate,
                    endDate = endDate,
                    isChronic = isChronic
                )
                val newSchedules = buildSchedules(scheduleRules, times, medicationId = 0)

                val saved = runCatching {
                    medicationRepository.insertMedicationWithSchedules(medication, newSchedules)
                }.onFailure {
                    Log.e("PillDebug", "No se pudo guardar el medicamento", it)
                }.isSuccess

                if (!saved) {
                    deleteImage(newImagePath)
                    _errorRes.value = R.string.medication_save_error
                    return@launch
                }

                runCatching { reminderScheduler.generateWindow() }
                    .onFailure {
                        Log.e("PillDebug", "No se pudieron generar las tomas al crear", it)
                    }
                onSaved()
            } catch (e: Exception) {
                deleteImage(newImagePath)
                Log.e("PillDebug", "Error inesperado al crear el medicamento", e)
                _errorRes.value = R.string.medication_save_error
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Actualiza un medicamento existente y reemplaza sus horarios.
     *
     * La imagen nueva se copia antes del commit y la antigua solo se borra
     * cuando la base de datos ya se ha actualizado correctamente.
     *
     * @param onSaved Callback ejecutado cuando la actualización y la generación
     * de tomas ya han terminado.
     */
    fun updateMedication(
        name: String,
        newPhotoUri: Uri? = null,
        currentImagePath: String? = null,
        dose: Double,
        unit: String,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        scheduleRules: List<ScheduleRule>,
        times: List<LocalTime>,
        isChronic: Boolean,
        onSaved: () -> Unit = {}
    ) {
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            var newImagePath: String? = null

            try {
                if (newPhotoUri != null) {
                    newImagePath = copyImageToInternalStorage(context, newPhotoUri)
                    if (newImagePath == null) {
                        _errorRes.value = R.string.medication_image_error
                        return@launch
                    }
                }

                val id = medicationId
                if (id == null) {
                    deleteImage(newImagePath)
                    _errorRes.value = R.string.medication_update_error
                    return@launch
                }

                val currentMedication = medicationRepository.getMedicationByIdOnce(id)
                if (currentMedication == null) {
                    deleteImage(newImagePath)
                    _errorRes.value = R.string.medication_update_error
                    return@launch
                }

                val now = LocalDateTime.now()
                val allIntakeIds = intakeRepository.getIntakeIdsByMedicationId(id)
                val futureIntakeIds = intakeRepository
                    .getFutureIntakesByMedicationId(id, now)
                    .map { it.id }

                val updatedMedication = currentMedication.copy(
                    name = name.trim(),
                    imageUrl = newImagePath ?: currentImagePath ?: currentMedication.imageUrl,
                    dose = dose,
                    unit = unit.trim(),
                    startDate = startDate,
                    endDate = endDate,
                    isChronic = isChronic,
                    updatedAt = now
                )
                val newSchedules = buildSchedules(scheduleRules, times, medicationId = id)

                val saved = runCatching {
                    medicationRepository.updateMedicationWithSchedules(
                        medication = updatedMedication,
                        schedules = newSchedules,
                        deleteFutureFrom = now
                    )
                }.onFailure {
                    Log.e("PillDebug", "No se pudo actualizar el medicamento", it)
                }.isSuccess

                if (!saved) {
                    deleteImage(newImagePath)
                    _errorRes.value = R.string.medication_update_error
                    return@launch
                }

                // A partir de aquí el commit ya es correcto: la imagen antigua
                // se puede borrar sin riesgo para los datos.
                if (newImagePath != null && currentMedication.imageUrl != null) {
                    deleteImage(currentMedication.imageUrl)
                }

                reminderScheduler.cancelAlarms(futureIntakeIds)
                notifications.cancelAll(allIntakeIds)
                runCatching { reminderScheduler.generateWindow() }
                    .onFailure {
                        Log.e("PillDebug", "No se pudieron regenerar las tomas al editar", it)
                    }
                onSaved()
            } catch (e: Exception) {
                deleteImage(newImagePath)
                Log.e("PillDebug", "Error inesperado al actualizar el medicamento", e)
                _errorRes.value = R.string.medication_update_error
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Archiva el medicamento y cancela sus alarmas y notificaciones.
     *
     * No se borra el historial de tomas ni la imagen. Los horarios se conservan
     * para que una restauración recupere la pauta.
     *
     * @param medication Medicamento que se desea archivar.
     * @param onArchived Callback ejecutado cuando el archivado se completa.
     */
    fun archiveMedication(medication: Medication, onArchived: () -> Unit = {}) {
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val now = LocalDateTime.now()
                val allIntakeIds = intakeRepository.getIntakeIdsByMedicationId(medication.id)
                val futureIntakeIds = intakeRepository
                    .getFutureIntakesByMedicationId(medication.id, now)
                    .map { it.id }

                val archived = runCatching {
                    medicationRepository.softDeleteMedication(
                        medicationId = medication.id,
                        deletedAt = now,
                        deleteFutureFrom = now
                    )
                }.onFailure {
                    Log.e("PillDebug", "No se pudo archivar el medicamento", it)
                }.isSuccess

                if (!archived) {
                    _errorRes.value = R.string.medication_archive_error
                    return@launch
                }

                reminderScheduler.cancelAlarms(futureIntakeIds)
                notifications.cancelAll(allIntakeIds)
                onArchived()
            } catch (e: Exception) {
                Log.e("PillDebug", "Error inesperado al archivar el medicamento", e)
                _errorRes.value = R.string.medication_archive_error
            } finally {
                _isSaving.value = false
            }
        }
    }

    /** Descarta el error actual para que no se vuelva a mostrar. */
    fun clearError() {
        _errorRes.value = null
    }

    private fun deleteImage(path: String?) {
        deleteInternalImage(context, path)
    }

    private fun buildSchedules(
        scheduleRules: List<ScheduleRule>,
        times: List<LocalTime>,
        medicationId: Int
    ): List<Schedule> = scheduleRules.flatMap { rule ->
        times.map { time ->
            Schedule(
                medicationId = medicationId,
                rule = rule,
                time = time
            )
        }
    }
}
