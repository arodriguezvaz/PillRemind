package com.andres.pillremind.ui.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn


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
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Obtiene la lista de medicamentos asociados al perfil actual.
     */
    val medications = profileId.flatMapLatest { id ->
        if (id != null) medicationRepository.getMedications(id)
        else flowOf(emptyList())
    }.combine(_searchQuery) { meds, query ->
        if (query.isBlank()) meds
        else meds.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

}