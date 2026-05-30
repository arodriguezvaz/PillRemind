// Añadido KDoc en español para SessionViewModel y su factory
package com.andres.pillremind.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.repository.UserRepositoryImpl
import com.andres.pillremind.domain.model.TipoUsuario
import com.andres.pillremind.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * ViewModel encargado de gestionar la sesión del usuario.
 *
 * Proporciona operaciones relacionadas con el perfil activo almacenado en
 * [SessionManager]. Se usa principalmente para establecer o cambiar el perfil
 * activo desde la UI.
 *
 * @property sessionManager Gestor de sesión que persiste el perfil activo.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val usuarioRepository: UserRepositoryImpl
) : ViewModel() {


    init {
        inicializarSesion()
    }

    /**
     * Inicializa la sesión. Si no hay usuario, crea uno invitado.
     */
    private fun inicializarSesion() {
        viewModelScope.launch {
            // Intenta obtener el usuario actual de Room
            val user = usuarioRepository.getUser()
            if (user == null) {
                val nuevoUsuario = User(
                    tipoUsuario = TipoUsuario.INVITADO,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                val newUserId = usuarioRepository.insertUser(nuevoUsuario)
                sessionManager.setActiveUser(newUserId.toInt())
            } else {
                sessionManager.setActiveUser(user.id)
            }
        }
    }


    /**
     * Establece el perfil activo por su identificador.
     *
     * Esta operación se ejecuta en el scope del ViewModel y delega la persistencia
     * en [SessionManager].
     *
     * @param id Identificador del perfil que se desea activar.
     */
    fun setActiveProfile(id: Int) {
        viewModelScope.launch {
            sessionManager.setActiveProfile(id)
        }
    }

    fun setActiveUser(user: User) {
        viewModelScope.launch { sessionManager.setActiveUser(user.id) }
    }
}
