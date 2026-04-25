package com.andres.pillremind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    profileId: Int? = null,
) : ViewModel() {


    val profile: StateFlow<Profile?> =
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


    val medications: StateFlow<List<Medication>> =
        (if (profileId == null) {
            flowOf(emptyList())
        } else {
            medicationRepository.getMedication(profileId)
        }).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

class HomeViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    private val profileId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return HomeViewModel(profileRepository, medicationRepository, profileId) as T
    }
}