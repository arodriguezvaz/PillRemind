package com.andres.pillremind.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la creación de perfiles
 * @param profileRepository Repositorio de perfiles
 * @param profileId Id del perfil a editar
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val profileId: Int? = null
) : ViewModel() {
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

    val profile: StateFlow<Profile?> =
        try {
            if (profileId == null) {
                MutableStateFlow(null)
            } else {
                profileRepository.getPerfil(profileId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = null
                    )
            }
        } catch (error: Exception) {
            throw error
        }
}

class ProfileViewModelFactory(
    private val repository: ProfileRepository,
    private val profileId: Int?,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return ProfileViewModel(repository, profileId) as T
    }
}