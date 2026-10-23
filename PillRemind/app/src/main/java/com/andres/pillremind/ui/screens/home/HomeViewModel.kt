package com.andres.pillremind.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.projections.IntakeWithMedication
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.domain.repository.SyncRepository
import com.andres.pillremind.domain.repository.UserRepository
import com.andres.pillremind.notifications.ReminderScheduler
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
 * @param intakeRepository Repositorio de tomas.
 * @param sessionManager Gestor de sesión que expone el perfil activo.
 * @param reminderScheduler Coordina la generación de tomas y la programación de alarmas.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    private val intakeRepository: IntakeRepository,
    private val sessionManager: SessionManager,
    private val syncRepository: SyncRepository,
    private val userRepository: UserRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {
    init {
        viewModelScope.launch {
            val desde = intakeRepository.getLastGeneratedDate()
            val ahora = LocalDate.now()
            reminderScheduler.generateAndSchedule(desde, ahora)
            intakeRepository.saveLastGeneratedDate(ahora)
        }
    }

    private suspend fun syncSiEsPremium() {
        val user = userRepository.getUser() ?: return
        if (user.esPremium && user.supabaseUid != null) {
            syncRepository.syncIntakes()
                .onFailure { Log.e("Sync", "Error sync intakes: ${it.message}") }
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
            val nuevaTomadoHoy = if (intake.takenAt == null) hoy else null
            val updatedIntake = Intake(
                id = intake.intakeId,
                scheduleId = intake.scheduleId,
                supabaseId = intake.supabaseId,
                scheduledAt = intake.scheduledAt,
                takenAt = nuevaTomadoHoy,
                updatedAt = hoy
            )
            intakeRepository.updateIntake(updatedIntake)
            syncSiEsPremium()
        }
    }

}
