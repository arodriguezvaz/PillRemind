package com.andres.pillremind.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {
    

    // StateFlow para que la UI reaccione automáticamente
    val activeProfileId: StateFlow<Int?> = sessionManager.activeProfileId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun setActiveProfile(id: Int) {
        viewModelScope.launch {
            sessionManager.setActiveProfile(id)
        }
    }

    fun clearActiveProfile() {
        viewModelScope.launch {
            sessionManager.clearActiveProfile()
        }
    }
}

// Factory para instanciarlo con SessionManager
class SessionViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SessionViewModel(sessionManager) as T
    }
}