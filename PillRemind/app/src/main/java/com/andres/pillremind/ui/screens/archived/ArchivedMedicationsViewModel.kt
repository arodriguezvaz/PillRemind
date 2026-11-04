package com.andres.pillremind.ui.screens.archived

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.R
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.utils.deleteInternalImage
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.notifications.ReminderNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * ViewModel de la lista de medicamentos archivados.
 *
 * Permite recuperar un medicamento y su pauta (si se conservó) o borrarlo
 * definitivamente junto con su historial y su imagen.
 *
 * @param medicationRepository Repositorio de medicamentos.
 * @param intakeRepository Repositorio de tomas.
 * @param sessionManager Gestor de sesión con el perfil activo.
 * @param reminderScheduler Coordinador de alarmas.
 * @param notifications Gestor de notificaciones.
 * @param context Contexto usado para borrar imágenes.
 */
@HiltViewModel
class ArchivedMedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val intakeRepository: IntakeRepository,
    sessionManager: SessionManager,
    private val reminderScheduler: ReminderScheduler,
    private val notifications: ReminderNotifications,
    @ApplicationContext private val context: Context
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val archivedMedications: StateFlow<List<Medication>> =
        sessionManager.activeProfileId
            .filterNotNull()
            .flatMapLatest { profileId ->
                medicationRepository.getArchivedMedications(profileId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _isSaving = MutableStateFlow(false)

    /** Indica que hay una restauración o borrado en curso. */
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorRes = MutableStateFlow<Int?>(null)

    /** Error pendiente de mostrar en la pantalla. */
    val errorRes: StateFlow<Int?> = _errorRes.asStateFlow()

    /**
     * Restaura un medicamento archivado y regenera sus recordatorios.
     *
     * Los medicamentos archivados antes de conservar las pautas pueden no tener
     * horarios; en ese caso habrá que añadirlos al editar el medicamento.
     */
    fun restoreMedication(medication: Medication, onRestored: () -> Unit = {}) {
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val restored = runCatching {
                    medicationRepository.restoreMedication(
                        medicationId = medication.id,
                        restoredAt = LocalDateTime.now()
                    )
                }.onFailure {
                    Log.e("PillDebug", "No se pudo restaurar el medicamento", it)
                }.isSuccess

                if (!restored) {
                    _errorRes.value = R.string.medication_restore_error
                    return@launch
                }

                runCatching { reminderScheduler.generateWindow() }
                    .onFailure {
                        Log.e("PillDebug", "No se regeneraron las tomas al restaurar", it)
                    }
                onRestored()
            } catch (e: Exception) {
                Log.e("PillDebug", "Error inesperado al restaurar el medicamento", e)
                _errorRes.value = R.string.medication_restore_error
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Borra definitivamente un medicamento archivado.
     *
     * Primero se cancelan alarmas y notificaciones; después se borra la fila y,
     * solo si el borrado funciona, la imagen.
     */
    fun deletePermanently(medication: Medication, onDeleted: () -> Unit = {}) {
        if (_isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val intakeIds = runCatching {
                    intakeRepository.getIntakeIdsByMedicationId(medication.id)
                }.getOrDefault(emptyList())

                runCatching { reminderScheduler.cancelFutureForMedication(medication.id) }
                    .onFailure {
                        Log.e("PillDebug", "No se cancelaron las alarmas al borrar", it)
                    }

                val deleted = runCatching {
                    medicationRepository.deleteMedicationPermanently(medication.id)
                }.onFailure {
                    Log.e("PillDebug", "No se pudo borrar el medicamento definitivamente", it)
                }.isSuccess

                if (!deleted) {
                    _errorRes.value = R.string.medication_delete_error
                    return@launch
                }

                notifications.cancelAll(intakeIds)
                deleteInternalImage(context, medication.imageUrl)
                onDeleted()
            } catch (e: Exception) {
                Log.e("PillDebug", "Error inesperado al borrar el medicamento", e)
                _errorRes.value = R.string.medication_delete_error
            } finally {
                _isSaving.value = false
            }
        }
    }

    /** Descarta el error actual para que no se vuelva a mostrar. */
    fun clearError() {
        _errorRes.value = null
    }
}
