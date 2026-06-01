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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val medicationRepository: MedicationRepository,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
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
                    // Borrar todos los datos locales
                    medicationRepository.deleteAllByUsuario(user.id)
                    profileRepository.deleteAllByUsuario(user.id)
                    userRepository.deleteUser(user)

                    // Limpiar sesión
                    sessionManager.clearActiveUser()
                    sessionManager.clearActiveProfile()
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