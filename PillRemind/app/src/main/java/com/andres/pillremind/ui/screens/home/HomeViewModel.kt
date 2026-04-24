package com.andres.pillremind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val profileRepository: ProfileRepository,
    profileId: Int? = null,
) : ViewModel() {


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