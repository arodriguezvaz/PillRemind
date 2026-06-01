package com.andres.pillremind.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.TipoUsuario
import com.andres.pillremind.domain.repository.AuthRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.domain.repository.SyncRepository
import com.andres.pillremind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, password)
                .onSuccess {
                    val session = authRepository.getCurrentSession()
                    val supabaseUid = session?.user?.id ?: run {
                        _uiState.value = AuthUiState.Error("No se pudo obtener la sesión")
                        return@onSuccess
                    }
                    vincularUsuario(supabaseUid, email)
                    syncRepository.syncUsuario()
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(it.message ?: "Error al iniciar sesión")
                }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(email, password)
                .onSuccess {
                    val session = authRepository.getCurrentSession()
                    val supabaseUid = session?.user?.id ?: run {
                        _uiState.value = AuthUiState.Error("No se pudo obtener la sesión")
                        return@onSuccess
                    }
                    vincularUsuario(supabaseUid, email)
                    syncRepository.syncUsuario()
                    _uiState.value = AuthUiState.Success
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(it.message ?: "Error al registrarse")
                }
        }
    }


    private suspend fun vincularUsuario(supabaseUid: String, email: String) {
        val usuarioActual = userRepository.getUser()
        val perfilesLocales =
            usuarioActual?.id?.let { profileRepository.getAllByUser(it) } ?: emptyList()
        // lo comprobamos luego en syncRepository

        if (perfilesLocales.isEmpty()) {
            // Sin perfiles locales → vincular y descargar Supabase
            vincularYActualizar(supabaseUid, email)
            syncRepository.downloadCompleto()
            _uiState.value = AuthUiState.Success
        } else {
            // Hay perfiles locales → preguntar
            vincularYActualizar(supabaseUid, email)
            _uiState.value = AuthUiState.ConflictoPerfiles(supabaseUid, email)
        }
    }

    private suspend fun vincularYActualizar(supabaseUid: String, email: String) {
        val usuarioActual = userRepository.getUser() ?: return
        val usuarioRemoto = syncRepository.getUsuarioRemoto(supabaseUid)

        userRepository.updateUser(
            usuarioActual.copy(
                supabaseUid = supabaseUid,
                email = email,
                tipoUsuario = TipoUsuario.REGISTRADO,
                esPremium = usuarioRemoto?.esPremium ?: false,
                aceptaComunicaciones = usuarioRemoto?.aceptaComunicaciones ?: false,
                syncPendiente = true,
                updatedAt = LocalDateTime.now()
            )
        )
    }

    fun resolverConflicto(mantenerLocales: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            if (mantenerLocales) {
                syncRepository.syncCompleto()
                    .onSuccess { _uiState.value = AuthUiState.Success }
                    .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error") }
            } else {
                syncRepository.borrarLocalYDescargar()
                    .onSuccess { _uiState.value = AuthUiState.Success }
                    .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error") }
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class ConflictoPerfiles(val supabaseUid: String, val email: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}