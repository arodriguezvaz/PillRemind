package com.andres.pillremind.ui.screens.crearperfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Perfil
import com.andres.pillremind.domain.repository.PerfilRepository
import kotlinx.coroutines.launch

class CrearPerfilViewModel(private val repository: PerfilRepository) : ViewModel() {
    fun añadirPerfil(nombre: String, urlImagen: String? = null) {
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

    fun updatePerfil(perfil: Perfil) {
        viewModelScope.launch {
            repository.updatePerfil(perfil)
        }
    }

    fun deletePerfil(perfil: Perfil) {
        viewModelScope.launch {
            repository.deletePerfil(perfil)
        }
    }
}

class PerfilViewModelFactory(
    private val repository: PerfilRepository
): ViewModelProvider.Factory {

    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CrearPerfilViewModel::class.java)){
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return CrearPerfilViewModel(repository) as T
    }
}