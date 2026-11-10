package com.andres.pillremind.ui.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Medication
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
 * Estado de la pantalla de medicamentos.
 *
 * @property medications Medicamentos que cumplen el filtro actual.
 * @property isLoading Indica si aún no se ha recibido la primera emisión.
 * @property hasMedications Indica si el perfil tiene algún medicamento, aunque
 * el filtro actual no devuelva resultados.
 */
data class MedicationsUiState(
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = true,
    val hasMedications: Boolean = false,
)

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
     * Obtiene los medicamentos asociados al perfil actual y aplica el filtro
     * de búsqueda.
     *
     * Arranca en [MedicationsUiState.isLoading] para no mostrar el estado
     * vacío antes de la primera emisión de Room.
     */
    val uiState = profileId.flatMapLatest { id ->
        if (id != null) medicationRepository.getMedications(id)
        else flowOf(emptyList())
    }.combine(_searchQuery) { meds, query ->
        val filtered = if (query.isBlank()) {
            meds
        } else {
            meds.filter { it.name.contains(query, ignoreCase = true) }
        }
        MedicationsUiState(
            medications = filtered,
            isLoading = false,
            hasMedications = meds.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MedicationsUiState()
    )

}