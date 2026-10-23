package com.andres.pillremind.ui.screens.medication


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.utils.copyImageToInternalStorage
import com.andres.pillremind.di.ApplicationScope
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.WeekDay
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
import com.andres.pillremind.domain.repository.SyncRepository
import com.andres.pillremind.domain.repository.UserRepository
import com.andres.pillremind.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
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
    private val syncRepository: SyncRepository,
    private val userRepository: UserRepository,
    @ApplicationScope private val appScope: CoroutineScope,
    sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    //Recupera el profileId del sessionManager
    val profileId = sessionManager.activeProfileId
    private val _snackbarMessage = MutableStateFlow<String?>(null)


    //Recupera el id del medicamento
    val medicationId = savedStateHandle.get<Int>("medicationId")


    private fun syncSiEsPremium() {
        appScope.launch {
            val user = userRepository.getUser() ?: return@launch
            if (user.esPremium && user.supabaseUid != null) {
                syncRepository.syncMedicamentos()
                    .onFailure { Log.e("Sync", "Error sync medicamentos: ${it.message}") }
                syncRepository.syncSchedules()
                    .onFailure { Log.e("Sync", "Error sync schedules: ${it.message}") }
                syncRepository.syncIntakes()
                    .onFailure { Log.e("Sync", "Error sync intakes: ${it.message}") }
            }
        }
    }

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

    val weekDays: StateFlow<Set<WeekDay>> = schedules
        .map { list -> list.map { it.weekDay }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val times: StateFlow<List<LocalTime>> = schedules
        .map { list -> list.map { it.time }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Crea un nuevo medicamento con sus horarios y programa las alarmas.
     *
     * Valida campos básicos y, si existe un perfil activo, inserta la medicación,
     * crea los schedules y programa las alarmas correspondientes.
     */
    fun addMedication(
        nombre: String,
        photoUri: Uri? = null,
        dose: Double,
        unit: String,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        weekDays: Set<WeekDay>,
        times: List<LocalTime>,
        isChronic: Boolean
    ) {

        val trimmedName = nombre.trim()
        val normalizedUnit = unit.trim()
        val imagePath = photoUri?.let {
            copyImageToInternalStorage(context, it)
        }

        viewModelScope.launch {
            val profileId = profileId.firstOrNull()
            if (profileId != null) {
                val medicationId = medicationRepository.insertMedication(
                    Medication(
                        name = trimmedName,
                        profileId = profileId,
                        imageUrl = imagePath,
                        dose = dose,
                        unit = normalizedUnit,
                        startDate = startDate,
                        endDate = endDate,
                        isChronic = isChronic
                    )
                )
                Log.d("PillDebug", "Medicamento creado con id: $medicationId")

                for (day in weekDays) {
                    for (time in times) {
                        scheduleRepository.insertSchedule(
                            Schedule(
                                medicationId = medicationId.toInt(),
                                weekDay = day,
                                time = time,
                            )
                        ).toInt()
                    }
                }
                Log.d(
                    "PillDebug",
                    "Generando ventana rodante de ${ReminderScheduler.WINDOW_DAYS} días"
                )

                val intakes = reminderScheduler.generateWindow()
                Log.d("PillDebug", "Intakes generadas: ${intakes.size}")
            }
            syncSiEsPremium()
        }
    }

    /**
     * Actualiza un medicamento existente y reemplaza sus horarios.
     */
    fun updateMedication(
        name: String,
        newPhotoUri: Uri? = null,
        currentImagePath: String? = null,
        dose: Double,
        unit: String,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        weekDays: Set<WeekDay>,
        times: List<LocalTime>,
        isChronic: Boolean
    ) {
        val trimmedName = name.trim()
        val normalizedUnit = unit.trim()
        val imagePath = if (newPhotoUri != null) {
            currentImagePath?.let { File(it).delete() }
            copyImageToInternalStorage(context, newPhotoUri)
        } else {
            currentImagePath
        }
        viewModelScope.launch {
            val profileId = profileId.firstOrNull()

            if (profileId != null && medicationId != null) {
                val medicamentoActual = medicationRepository.getMedicationByIdOnce(medicationId)

                val updatedMedication = Medication(
                    id = medicationId,
                    name = trimmedName,
                    profileId = profileId,
                    imageUrl = imagePath,
                    dose = dose,
                    unit = normalizedUnit,
                    startDate = startDate,
                    endDate = endDate,
                    updatedAt = LocalDateTime.now(),
                    supabaseId = medicamentoActual?.supabaseId,
                    isChronic = isChronic
                )

                medicationRepository.updateMedication(updatedMedication)
                reminderScheduler.cancelFutureForMedication(medicationId)
                intakeRepository.deleteFutureIntakesByMedicationId(
                    medicationId,
                    LocalDateTime.now()
                )

                scheduleRepository.deleteByMedicationId(medicationId)
                for (day in weekDays) {
                    for (time in times) {
                        scheduleRepository.insertSchedule(
                            Schedule(
                                medicationId = medicationId,
                                weekDay = day,
                                time = time
                            )
                        )
                    }
                }
                reminderScheduler.generateWindow()

            }
            syncSiEsPremium()
        }
    }

    /**
     * Elimina el medicamento y cancela las alarmas asociadas.
     */
    fun deleteMedication(medication: Medication) {
        medication.imageUrl?.let { File(it).delete() }

        val supabaseId = medication.supabaseId

        viewModelScope.launch {
            reminderScheduler.cancelFutureForMedication(medication.id)
            medicationRepository.deleteMedication(medication)
        }

        if (supabaseId != null) {
            appScope.launch {
                val user = userRepository.getUser() ?: return@launch
                if (user.esPremium && user.supabaseUid != null) {
                    syncRepository.deleteMedication(supabaseId)
                        .onFailure { Log.e("Sync", "Error borrando medicamento: ${it.message}") }
                }
            }
        }
    }

}
