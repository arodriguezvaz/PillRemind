package com.andres.pillremind.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar la sesión local del usuario.
 *
 * Proporciona operaciones relacionadas con el perfil activo almacenado en
 * [SessionManager]. Se usa principalmente para establecer o cambiar el perfil
 * activo desde la UI.
 *
 * @property sessionManager Gestor de sesión que persiste el perfil activo.
 * @property usuarioRepository Repositorio del usuario local.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val usuarioRepository: UserRepository
) : ViewModel() {

    init {
        inicializarSesion()
    }

    /**
     * Inicializa la sesión. Si no hay usuario, crea uno local.
     */
    private fun inicializarSesion() {
        viewModelScope.launch {
            usuarioRepository.crearUserInvitado()
            usuarioRepository.getUser()?.let { user ->
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
}
