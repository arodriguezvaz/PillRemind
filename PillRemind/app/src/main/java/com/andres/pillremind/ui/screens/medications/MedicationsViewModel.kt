package com.andres.pillremind.ui.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.ui.screens.home.HomeViewModel
import com.andres.pillremind.ui.screens.medication.MedicationViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    val profileId = sessionManager.activeProfileId

    /**
     * Obtiene la lista de medicamentos asociados al perfil actual si el id del perfil no es nulo
     */
    val medications = profileId.flatMapLatest { id ->
        if (id != null) {
            medicationRepository.getMedication(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}