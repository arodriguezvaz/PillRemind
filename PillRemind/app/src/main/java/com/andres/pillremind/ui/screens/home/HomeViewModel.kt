package com.andres.pillremind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Perfil
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.ui.screens.profile.ProfileViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val profileRepository: ProfileRepository,
    profileId: Int,
) : ViewModel() {


    val perfil: StateFlow<Perfil?> =
        try {
            profileRepository.getPerfil(profileId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                )
        } catch (error: Exception) {
            throw error
        }
}

class HomeViewModelFactory(
    private val repository: ProfileRepository,
    private val profileId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return HomeViewModel(repository, profileId = profileId) as T
    }
}