package com.andres.pillremind.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.TipoUsuario
import com.andres.pillremind.domain.model.User
import com.andres.pillremind.domain.repository.AuthRepository
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
    private val sessionManager: SessionManager
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
                    _uiState.value = AuthUiState.Success
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
                    _uiState.value = AuthUiState.Success
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(it.message ?: "Error al registrarse")
                }
        }
    }

    private suspend fun vincularUsuario(supabaseUid: String, email: String) {
        val usuarioActual = userRepository.getUser()
        if (usuarioActual != null) {
            userRepository.updateUser(
                usuarioActual.copy(
                    supabaseUid = supabaseUid,
                    email = email,
                    tipoUsuario = TipoUsuario.REGISTRADO,
                    syncPendiente = true,
                    updatedAt = LocalDateTime.now()
                )
            )
        } else {
            val id = userRepository.insertUser(
                User(
                    supabaseUid = supabaseUid,
                    email = email,
                    tipoUsuario = TipoUsuario.REGISTRADO,
                    syncPendiente = true
                )
            )
            sessionManager.setActiveUser(id.toInt())
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
    data class Error(val message: String) : AuthUiState()
}