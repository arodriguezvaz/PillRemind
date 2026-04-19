package com.andres.pillremind.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Perfil
import com.andres.pillremind.domain.repository.PerfilRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProfilesViewModel(
    private val repository: PerfilRepository
) : ViewModel() {
    val perfiles: StateFlow<List<Perfil>> = repository.getPerfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

class ProfilesViewModelFactory(
    private val repository: PerfilRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProfilesViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return ProfilesViewModel(repository) as T
    }
}