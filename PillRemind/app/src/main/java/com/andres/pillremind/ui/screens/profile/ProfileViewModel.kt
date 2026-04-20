package com.andres.pillremind.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Perfil
import com.andres.pillremind.domain.repository.ProfileRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la creación de perfiles
 * @property repository Repositorio de perfiles
 */
class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    fun addProfile(nombre: String, urlImagen: String? = null) {
        val trimmedNombre = nombre.trim()
        if (trimmedNombre.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertPerfil(
                    Perfil(
                        nombre = trimmedNombre,
                        urlImagen = urlImagen
                    )
                )
            }
        }
    }

    fun updateProfile(perfil: Perfil) {
        viewModelScope.launch {
            repository.updatePerfil(perfil)
        }
    }

    fun deleteProfile(perfil: Perfil) {
        viewModelScope.launch {
            repository.deletePerfil(perfil)
        }
    }
}

class ProfileViewModelFactory(
    private val repository: ProfileRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return ProfileViewModel(repository) as T
    }
}