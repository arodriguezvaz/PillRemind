package com.andres.pillremind.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de selección de perfiles.
 *
 * @property profiles Perfiles disponibles.
 * @property isLoading Indica si aún no se ha recibido la primera emisión.
 */
data class ProfilesUiState(
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * ViewModel que expone la lista de perfiles disponibles.
 *
 * Utiliza [ProfileRepository] para obtener un Flow de perfiles y lo convierte en
 * [StateFlow] para que la UI pueda observar los cambios de forma cómoda.
 *
 * @property repository Repositorio usado para obtener los perfiles.
 * @property sessionManager Gestor de sesión para obtener el perfil activo.
 */
@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    fun clearActiveProfile() {
        viewModelScope.launch {
            sessionManager.clearActiveProfile()
        }
    }

    /**
     * Estado con los perfiles disponibles.
     *
     * Arranca en [ProfilesUiState.isLoading] para no mostrar el estado vacío
     * antes de la primera emisión de Room.
     */
    val uiState: StateFlow<ProfilesUiState> = repository.getProfiles()
        .map { profiles -> ProfilesUiState(profiles = profiles, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            // Se mantiene la observación activa mientras viva el ViewModel para
            // que la lista se actualice al volver de crear o editar un perfil.
            started = SharingStarted.Eagerly,
            initialValue = ProfilesUiState()
        )
}
