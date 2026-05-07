// Añadido KDoc en español para SessionViewModel y su factory
package com.andres.pillremind.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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

// Factory para instanciarlo con SessionManager
/**
 * Factory para crear instancias de [SessionViewModel] cuando no se usa la
 * inyección automática (por ejemplo, en pruebas). Recibe un [SessionManager]
 * que será pasado al ViewModel.
 *
 * @property sessionManager Gestor de sesión usado para crear el ViewModel.
 */
class SessionViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SessionViewModel(sessionManager) as T
    }
}