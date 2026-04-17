package com.andres.pillremind.ui.screens.perfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Perfil
import com.andres.pillremind.domain.repository.PerfilRepository
import com.andres.pillremind.ui.screens.crearperfil.CrearPerfilViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PerfilesViewModel(
    private val repository: PerfilRepository
) : ViewModel() {
    val perfiles: StateFlow<List<Perfil>> = repository.getPerfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

class PerfilesViewModelFactory(
    private val repository: PerfilRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PerfilesViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return PerfilesViewModel(repository) as T
    }
}