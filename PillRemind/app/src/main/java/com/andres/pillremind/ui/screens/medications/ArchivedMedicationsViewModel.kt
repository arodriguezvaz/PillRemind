package com.andres.pillremind.ui.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel que expone los medicamentos archivados del perfil activo.
 *
 * Observa el perfil actual desde [SessionManager] y carga los medicamentos
 * archivados usando [MedicationRepository]. Si no hay perfil activo, la lista
 * queda vacía.
 *
 * @property medicationRepository Repositorio para leer los medicamentos.
 * @property sessionManager Gestor de sesión que expone el perfil activo.
 */
@HiltViewModel
class ArchivedMedicationsViewModel @Inject constructor(
    medicationRepository: MedicationRepository,
    sessionManager: SessionManager
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val archivedMedications = sessionManager.activeProfileId.flatMapLatest { id ->
        if (id != null) medicationRepository.getArchivedMedications(id)
        else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
