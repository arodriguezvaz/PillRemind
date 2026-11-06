package com.andres.pillremind.ui.screens.medication

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.utils.copyImageToInternalStorage
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.ScheduleRule
import com.andres.pillremind.domain.model.nextIntake
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
import com.andres.pillremind.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
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
 * @param sessionManager Gestor de sesión que expone el perfil activo.
 */
@HiltViewModel
class MedicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val reminderScheduler: ReminderScheduler,
    private val intakeRepository: IntakeRepository,
    sessionManager: SessionManager,
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

    /**
     * Próxima toma futura del medicamento, calculada a partir de sus horarios.
     *
     * Se recalcula cuando cambia el medicamento o su pauta. Si no quedan tomas
     * futuras (tratamiento terminado o archivado), vale `null`.
     */
    val nextIntake: StateFlow<LocalDateTime?> = combine(medication, schedules) { med, horarios ->
        med?.let { horarios.nextIntake(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Crea un nuevo medicamento con sus horarios y programa las alarmas.
     *
     * La inserción del medicamento y de sus horarios se hace en una sola
     * transacción. La generación de tomas y alarmas se hace después del commit.
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

        val trimmedName = nombre.trim()
        val normalizedUnit = unit.trim()

        viewModelScope.launch {
            val profileId = profileId.firstOrNull() ?: return@launch
            val imagePath = photoUri?.let {
                copyImageToInternalStorage(context, it)
            }
            val medication = Medication(
                name = trimmedName,
                profileId = profileId,
                imageUrl = imagePath,
                dose = dose,
                unit = normalizedUnit,
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

            if (!saved) return@launch

            runCatching { reminderScheduler.generateWindow() }
                .onFailure {
                    Log.e("PillDebug", "No se pudieron generar las tomas al crear", it)
                }
            onSaved()
        }
    }

    /**
     * Actualiza un medicamento existente y reemplaza sus horarios.
     *
     * Primero se guardan los IDs de las tomas futuras para poder cancelar sus
     * alarmas. Después se actualiza la base de datos en una transacción y, solo
     * si el commit va bien, se cancelan las alarmas antiguas y se regeneran.
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
        val trimmedName = name.trim()
        val normalizedUnit = unit.trim()
        viewModelScope.launch {
            val id = medicationId ?: return@launch
            val currentMedication = medicationRepository.getMedicationByIdOnce(id) ?: return@launch
            val previousImagePath = currentImagePath ?: currentMedication.imageUrl
            val imagePath = if (newPhotoUri != null) {
                // Copia primero la nueva imagen: si falla, se conserva la anterior.
                val copiedPath = copyImageToInternalStorage(context, newPhotoUri)
                if (copiedPath != null) {
                    previousImagePath?.let { File(it).delete() }
                    copiedPath
                } else {
                    previousImagePath
                }
            } else {
                previousImagePath
            }
            val now = LocalDateTime.now()
            val futureIntakeIds = intakeRepository
                .getFutureIntakesByMedicationId(id, now)
                .map { it.id }

            val updatedMedication = currentMedication.copy(
                name = trimmedName,
                imageUrl = imagePath,
                dose = dose,
                unit = normalizedUnit,
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

            if (!saved) return@launch

            reminderScheduler.cancelAlarms(futureIntakeIds)
            runCatching { reminderScheduler.generateWindow() }
                .onFailure {
                    Log.e("PillDebug", "No se pudieron regenerar las tomas al editar", it)
                }
            onSaved()
        }
    }

    /**
     * Archiva el medicamento y cancela sus alarmas futuras.
     *
     * No se borra el historial de tomas, la imagen ni la pauta, para poder
     * recuperar el medicamento y conservar sus estadísticas.
     *
     * @param medication Medicamento que se desea archivar.
     * @param onArchived Callback ejecutado cuando el archivado se completa.
     */
    fun archiveMedication(medication: Medication, onArchived: () -> Unit = {}) {
        viewModelScope.launch {
            val now = LocalDateTime.now()
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

            if (!archived) return@launch

            reminderScheduler.cancelAlarms(futureIntakeIds)
            onArchived()
        }
    }

    /**
     * Desarchiva el medicamento y regenera su ventana de tomas.
     *
     * La pauta se conserva al archivar, así que tras limpiar `deletedAt` se
     * vuelven a generar las tomas y sus alarmas.
     *
     * @param medication Medicamento que se desea recuperar.
     * @param onRestored Callback ejecutado cuando el medicamento vuelve a estar activo.
     */
    fun restoreMedication(medication: Medication, onRestored: () -> Unit = {}) {
        viewModelScope.launch {
            val restored = runCatching {
                medicationRepository.unarchiveMedication(
                    medicationId = medication.id,
                    updatedAt = LocalDateTime.now()
                )
            }.onFailure {
                Log.e("PillDebug", "No se pudo desarchivar el medicamento", it)
            }.isSuccess

            if (!restored) return@launch

            runCatching { reminderScheduler.generateWindow() }
                .onFailure {
                    Log.e("PillDebug", "No se pudieron regenerar las tomas al desarchivar", it)
                }
            onRestored()
        }
    }

    /**
     * Elimina definitivamente el medicamento y todo su historial.
     *
     * Primero guarda las tomas futuras para cancelar sus alarmas, después
     * borra el medicamento (Room elimina en cascada su pauta y sus tomas) y
     * finalmente borra la imagen asociada.
     *
     * @param medication Medicamento que se desea eliminar.
     * @param onDeleted Callback ejecutado cuando el borrado se completa.
     */
    fun deleteMedicationPermanently(medication: Medication, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val futureIntakeIds = intakeRepository
                .getFutureIntakesByMedicationId(medication.id, now)
                .map { it.id }

            val deleted = runCatching {
                medicationRepository.deleteMedication(medication)
            }.onFailure {
                Log.e("PillDebug", "No se pudo eliminar el medicamento", it)
            }.isSuccess

            if (!deleted) return@launch

            medication.imageUrl?.let { imagePath ->
                runCatching { File(imagePath).delete() }
                    .onFailure {
                        Log.w("PillDebug", "No se pudo borrar la imagen del medicamento", it)
                    }
            }
            reminderScheduler.cancelAlarms(futureIntakeIds)
            onDeleted()
        }
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
