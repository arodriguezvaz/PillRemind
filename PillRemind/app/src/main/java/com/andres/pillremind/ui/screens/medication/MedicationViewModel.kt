package com.andres.pillremind.ui.screens.medication


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    val profileId = sessionManager.activeProfileId
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()


    fun addMedication(
        nombre: String,
        imageUrl: String? = null,
        dose: Double,
        unit: String,
        startDate: LocalDate,
        endDate: LocalDate? = null
    ) {

        val trimmedName = nombre.trim()
        val normalizedUnit = unit.trim()

        if (trimmedName.isBlank()) {
            showMessage("El nombre no puede estar vacío")
            return
        }

        if (dose <= 0) {
            showMessage("La dosis debe ser mayor que 0")
            return
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            showMessage("La fecha de fin no puede ser anterior a la de inicio")
            return
        }

        viewModelScope.launch {
            val profileId = profileId.firstOrNull()
            if (profileId != null) {
                medicationRepository.insertMedication(
                    Medication(
                        name = trimmedName,
                        profileId = profileId,
                        imageUrl = imageUrl,
                        dose = dose,
                        unit = normalizedUnit,
                        startDate = startDate,
                        endDate = endDate
                    )
                )
            } else {
                showMessage("No hay perfil activo")
            }
        }
    }

    private fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }
}

