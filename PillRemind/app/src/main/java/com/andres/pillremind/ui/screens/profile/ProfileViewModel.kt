package com.andres.pillremind.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la creación de perfiles
 * @param profileRepository Repositorio de perfiles
 * @param profileId Id del perfil a editar
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    /**
     * Agrega un nuevo perfil usando la función del repositorio
     * @param nombre Nombre del perfil
     * @param urlImagen URL de la imagen del perfil
     */
    fun addProfile(nombre: String, urlImagen: String? = null) {
        val trimmedNombre = nombre.trim()
        if (trimmedNombre.isNotEmpty()) {
            viewModelScope.launch {
                profileRepository.insertPerfil(
                    Profile(
                        name = trimmedNombre,
                        urlImagen = urlImagen
                    )
                )
            }
        }
    }

    val profileId = sessionManager.activeProfileId
    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.updatePerfil(profile)
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.deletePerfil(profile)
        }
    }

    /**
     * Obtiene el perfil actual en el ViewModel que se muestra en la pantalla
     */
    val profile: StateFlow<Profile?> =
        sessionManager.activeProfileId
            .filterNotNull()
            .flatMapLatest { id ->
                profileRepository.getPerfilById(id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

}
