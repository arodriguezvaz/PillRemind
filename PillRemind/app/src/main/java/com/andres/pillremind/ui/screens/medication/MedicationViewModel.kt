package com.andres.pillremind.ui.screens.medication


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.WeekDay
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
import com.andres.pillremind.notifications.AlarmScheduler
import com.andres.pillremind.notifications.nextTriggerMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@HiltViewModel
class MedicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val alarmScheduler: AlarmScheduler,
    sessionManager: SessionManager
) : ViewModel() {
    //Recupera el profileId del sessionManager
    val profileId = sessionManager.activeProfileId
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

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

    val weekDays: StateFlow<Set<WeekDay>> = schedules
        .map { list -> list.map { it.weekDay }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val times: StateFlow<List<LocalTime>> = schedules
        .map { list -> list.map { it.time }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMedication(
        nombre: String,
        imageUrl: String? = null,
        dose: Double,
        unit: String,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        weekDays: Set<WeekDay>,
        times: List<LocalTime>
    ) {

        val trimmedName = nombre.trim()
        val normalizedUnit = unit.trim()

        if (trimmedName.isBlank()) {
            showMessage("El nombre no puede estar vacío")
            return
        }

        if (dose <= 0) {
            showMessage("La dosis debe ser mayor que 0")
            return
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            showMessage("La fecha de fin no puede ser anterior a la de inicio")
            return
        }

        viewModelScope.launch {
            val profileId = profileId.firstOrNull()
            if (profileId != null) {
                val medicationId = medicationRepository.insertMedication(
                    Medication(
                        name = trimmedName,
                        profileId = profileId,
                        imageUrl = imageUrl,
                        dose = dose,
                        unit = normalizedUnit,
                        startDate = startDate,
                        endDate = endDate
                    )
                )

                for (day in weekDays) {
                    for (time in times) {
                        val scheduleId = scheduleRepository.insertSchedule(
                            Schedule(
                                medicationId = medicationId.toInt(),
                                weekDay = day,
                                time = time,
                            )
                        ).toInt()
                        val millis = nextTriggerMillis(day, time)
                        alarmScheduler.schedule(scheduleId, millis)
                    }
                }
            } else {
                showMessage("No hay perfil activo")
            }
        }
    }

    fun updateMedication(
        name: String,
        imageUrl: String? = null,
        dose: Double,
        unit: String,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        weekDays: Set<WeekDay>,
        times: List<LocalTime>
    ) {
        val trimmedName = name.trim()
        val normalizedUnit = unit.trim()
        viewModelScope.launch {
            val profileId = profileId.firstOrNull()

            if (profileId != null && medicationId != null) {

                val updatedMedication = Medication(
                    id = medicationId,
                    name = trimmedName,
                    profileId = profileId,
                    imageUrl = imageUrl,
                    dose = dose,
                    unit = normalizedUnit,
                    startDate = startDate,
                    endDate = endDate
                )

                medicationRepository.updateMedication(updatedMedication)

                scheduleRepository.deleteByMedicationId(medicationId.toInt())

                for (day in weekDays) {
                    for (time in times) {
                        scheduleRepository.insertSchedule(
                            Schedule(
                                medicationId = medicationId.toInt(),
                                weekDay = day,
                                time = time
                            )
                        )
                    }
                }

            } else {
                showMessage("No hay perfil activo")
            }
        }
    }

    private fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }
}

