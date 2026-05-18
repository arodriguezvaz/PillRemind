package com.andres.pillremind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.projections.IntakeWithMedication
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ViewModel de la pantalla principal (`HomeScreen`).
 *
 * Expone el perfil activo y la lista de medicamentos del perfil usando flows
 * convertidos a `StateFlow` para su observación en la UI.
 *
 * @param profileRepository Repositorio para obtener datos de perfil.
 * @param medicationRepository Repositorio para obtener medicamentos.
 * @param sessionManager Gestor de sesión que expone el perfil activo.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    private val intakeRepository: IntakeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    init {
        viewModelScope.launch {
            val desde = intakeRepository.getLastGeneratedDate()
            val ahora = LocalDate.now()
            intakeRepository.createIntakes(desde, ahora)
            intakeRepository.saveLastGeneratedDate(ahora)
        }
    }


    val profileId = sessionManager.activeProfileId

    @OptIn(ExperimentalCoroutinesApi::class)
    val profile: StateFlow<Profile?> =
        sessionManager.activeProfileId
            .filterNotNull()
            .flatMapLatest { id ->
                profileRepository.getProfileById(id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )


    @OptIn(ExperimentalCoroutinesApi::class)
    val todayIntakes: StateFlow<List<IntakeWithMedication>> = profileId
        .filterNotNull()
        .flatMapLatest { id ->
            intakeRepository.getIntakesForToday(LocalDate.now(), id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun tomarIntake(intake: IntakeWithMedication) {
        viewModelScope.launch {
            val hoy = LocalDateTime.now()
            val updatedIntake = Intake(
                intake.intakeId,
                intake.scheduleId,
                intake.scheduledAt,
                hoy,
                updatedAt = hoy
            )
            intakeRepository.updateIntake(updatedIntake)
        }
    }

}
