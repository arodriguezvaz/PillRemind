package com.andres.pillremind.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que expone la lista de perfiles disponibles.
 *
 * Utiliza [ProfileRepository] para obtener un Flow de perfiles y lo convierte en
 * [StateFlow] para que la UI pueda observar los cambios de forma cómoda.
 *
 * @property repository Repositorio usado para obtener los perfiles.
 */
@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun clearActiveProfile() {
        viewModelScope.launch {
            sessionManager.clearActiveProfile()
        }
    }

    /**
     * Flow con la lista de perfiles actualmente disponibles (initialValue = emptyList()).
     */
    val perfiles: StateFlow<List<Profile>> = repository.getProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun puedeCrearPerfil(onPuede: () -> Unit) {
        viewModelScope.launch {
            val user = userRepository.getUser() ?: return@launch
            val limite = if (user.esPremium) 3 else 1
            val perfilesActuales = perfiles.value

            if (perfilesActuales.size >= limite) {
                _snackbarMessage.value = if (user.esPremium)
                    "Has alcanzado el límite de 3 perfiles para tu plan"
                else
                    "Con el plan gratuito solo puedes tener 1 perfil. Hazte premium para más"
            } else {
                onPuede()
            }
        }
    }

}
