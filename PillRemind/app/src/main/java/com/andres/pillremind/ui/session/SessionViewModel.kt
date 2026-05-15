// Añadido KDoc en español para SessionViewModel y su factory
package com.andres.pillremind.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

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
    private val sessionManager: SessionManager
) : ViewModel() {


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
