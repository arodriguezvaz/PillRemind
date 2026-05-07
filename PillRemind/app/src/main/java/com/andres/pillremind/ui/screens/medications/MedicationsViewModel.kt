package com.andres.pillremind.ui.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlin.collections.emptyList

/**
 * ViewModel que expone la lista de medicamentos del perfil activo.
 *
 * Observa el perfil actual desde [SessionManager] y carga los medicamentos
 * correspondientes usando [MedicationRepository].
 *
 * @property medicationRepository Repositorio para leer los medicamentos.
 * @property sessionManager Gestor de sesión que expone el perfil activo.
 */
@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    sessionManager: SessionManager
) : ViewModel() {
    val profileId = sessionManager.activeProfileId

    /**
     * Obtiene la lista de medicamentos asociados al perfil actual.
     */
    val medications = profileId.flatMapLatest { id ->
        if (id != null) {
            medicationRepository.getMedications(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}