package com.andres.pillremind.ui.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.ui.screens.home.HomeViewModel
import com.andres.pillremind.ui.screens.medication.MedicationViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class MedicationsViewModel(
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    profileId: Int? = null
) : ViewModel() {
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

class MedicationsViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    private val profileId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MedicationsViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return MedicationsViewModel(profileRepository, medicationRepository, profileId) as T
    }
}