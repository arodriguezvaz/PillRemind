package com.andres.pillremind.ui.screens.medication


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate


class MedicationViewModel(
    private val medicationRepository: MedicationRepository,
    private val profileId: Int
) : ViewModel() {
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()


    fun addMedication(
        nombre: String,
        imageUrl: String? = null,
        dose: Double,
        unit: String,
        startDate: String,
        endDate: String? = null
    ) {
        val trimmedName = nombre.trim()
        val normalizedUnit = unit.trim()
        val parsedStartDate = runCatching { LocalDate.parse(startDate) }
            .getOrElse { LocalDate.now() }

        val parsedEndDate = endDate
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (trimmedName.isBlank()) {
            showMessage("El nombre no puede estar vacío")
            return
        }

        if (dose <= 0) {
            showMessage("La dosis debe ser mayor que 0")
            return
        }

        if (parsedEndDate != null && parsedEndDate.isBefore(parsedStartDate)) {
            showMessage("La fecha de fin no puede ser anterior a la de inicio")
            return
        }

        viewModelScope.launch {
            medicationRepository.insertMedication(
                Medication(
                    name = trimmedName,
                    profileId = profileId,
                    imageUrl = imageUrl,
                    dose = dose,
                    unit = normalizedUnit,
                    startDate = parsedStartDate,
                    endDate = parsedEndDate
                )
            )
        }
    }

    private fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }
}

class MedicationViewModelFactory(
    private val medicationRepository: MedicationRepository,
    private val profileId: Int,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MedicationViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return MedicationViewModel(medicationRepository, profileId) as T
    }
}