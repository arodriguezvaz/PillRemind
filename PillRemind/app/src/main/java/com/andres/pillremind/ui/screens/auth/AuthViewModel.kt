package com.andres.pillremind.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

/**
 * ViewModel encargado de la autenticación de usuarios.
 *
 * Gestiona el login, registro, vinculación del usuario con el backend,
 * sincronización inicial de datos y resolución de conflictos de perfiles
 * entre almacenamiento local y remoto.
 *
 * Expone un estado UI reactivo mediante [AuthUiState].
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    // Estado interno mutable de la UI. Solo el ViewModel puede modificarlo.
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    // Estado expuesto a la UI como flujo inmutable para evitar modificaciones externas.
    val uiState = _uiState.asStateFlow()

    /**
     * Inicia el proceso de inicio de sesión del usuario.
     *
     * Realiza la autenticación con el repositorio, obtiene la sesión activa,
     * vincula el usuario con el sistema y lanza la sincronización de datos.
     * Actualiza el estado de la UI según el resultado.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Se notifica a la UI que el proceso está en curso
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, password)
                .onSuccess {
                    // Se notifica a la UI que el proceso está en curso
                    val session = authRepository.getCurrentSession()
                    val supabaseUid = session?.user?.id ?: run {
                        // Si no se puede obtener el usuario de la sesión, se lanza un error en la UI
                        _uiState.value = AuthUiState.Error("No se pudo obtener la sesión")
                        return@onSuccess
                    }
                    // Vincula el usuario y sincroniza datos con el backend
                    vincularUsuario(supabaseUid, email)
                    syncRepository.syncUsuario()
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(it.message ?: "Error al iniciar sesión")
                }
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * Realiza el registro en el backend, obtiene la sesión activa,
     * vincula el usuario con el sistema local y lanza la sincronización
     * de datos inicial.
     *
     * Actualiza el estado de la UI según el resultado del proceso.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     */
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

    /**
     * Vincula el usuario local con su cuenta remota y gestiona el estado inicial de sincronización.
     *
     * Comprueba si el usuario tiene perfiles locales:
     * - Si no tiene perfiles, realiza una descarga completa desde el servidor y finaliza con éxito.
     * - Si existen perfiles locales, se detecta un posible conflicto y se notifica a la UI
     *   para que el usuario decida cómo resolverlo.
     *
     * @param supabaseUid Identificador único del usuario en el backend.
     * @param email Correo electrónico asociado al usuario.
     */
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

    /**
     * Resuelve el conflicto entre datos locales y remotos tras la autenticación.
     *
     * Permite al usuario decidir si mantener los datos locales o reemplazarlos
     * con los datos del servidor. En función de la decisión, se ejecuta una
     * sincronización completa o una limpieza de datos locales seguida de descarga.
     *
     * Actualiza el estado de la UI según el resultado de la operación.
     *
     * @param mantenerLocales Indica si se deben conservar los datos locales (true)
     * o reemplazarlos por los datos remotos (false).
     */
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