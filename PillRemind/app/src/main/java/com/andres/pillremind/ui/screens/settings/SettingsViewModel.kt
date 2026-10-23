package com.andres.pillremind.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.User
import com.andres.pillremind.domain.repository.AuthRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.domain.repository.SyncRepository
import com.andres.pillremind.domain.repository.UserRepository
import com.andres.pillremind.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel encargado de la gestión de la pantalla de ajustes.
 *
 * Gestiona la configuración del usuario, modo oscuro, estado de sesión,
 * sincronización de datos y operaciones de cuenta como logout o premium.
 *
 * Expone estados reactivos de usuario, tema y UI mediante StateFlow.
 *
 * @property sessionManager Gestor de sesión del usuario activo.
 * @property medicationRepository Repositorio para la gestión de medicación.
 * @property profileRepository Repositorio para la gestión de perfiles.
 * @property userRepository Repositorio para la gestión del usuario local.
 * @property authRepository Repositorio encargado de autenticación.
 * @property syncRepository Repositorio encargado de la sincronización de datos.
 * @property reminderScheduler Coordinador de alarmas de recordatorio.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val medicationRepository: MedicationRepository,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = sessionManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val usuario: StateFlow<User?> = userRepository.getUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { sessionManager.setDarkMode(enabled) }
    }

    fun togglePremium(enabled: Boolean) {
        viewModelScope.launch {
            val user = usuario.value ?: return@launch
            userRepository.updateUser(
                user.copy(esPremium = enabled, updatedAt = LocalDateTime.now())
            )
            syncRepository.syncUsuario()
                .onSuccess { Log.d("Sync", "Usuario sync OK") }
                .onFailure { Log.e("Sync", "Error: ${it.message}", it) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            authRepository.logout()
                .onSuccess {
                    // Resetear usuario a invitado en Room
                    val user = usuario.value ?: return@onSuccess

                    // Cancelar las alarmas antes de borrar las tomas locales.
                    runCatching { reminderScheduler.cancelFutureForUser(user.id) }
                        .onFailure {
                            Log.e(
                                "PillDebug",
                                "No se pudieron cancelar las alarmas del usuario",
                                it
                            )
                        }

                    // Borrar todos los datos locales
                    medicationRepository.deleteAllByUsuario(user.id)
                    profileRepository.deleteAllByUsuario(user.id)
                    userRepository.deleteUser(user)

                    // Limpiar sesión y la fecha de generación del usuario anterior
                    sessionManager.clearActiveUser()
                    sessionManager.clearActiveProfile()
                    sessionManager.clearLastGeneratedDate()
                    _uiState.value = SettingsUiState.LoggedOut
                }
                .onFailure {
                    _uiState.value = SettingsUiState.Error(it.message ?: "Error al cerrar sesión")
                }
        }
    }

    fun resetState() {
        _uiState.value = SettingsUiState.Idle
    }
}

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    object LoggedOut : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}