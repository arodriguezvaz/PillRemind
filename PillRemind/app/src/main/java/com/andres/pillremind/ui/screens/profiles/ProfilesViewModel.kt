package com.andres.pillremind.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel que expone la lista de perfiles disponibles.
 *
 * Utiliza [ProfileRepository] para obtener un Flow de perfiles y lo convierte en
 * [StateFlow] para que la UI pueda observar los cambios de forma cómoda.
 *
 * @property repository Repositorio usado para obtener los perfiles.
 */
@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    /**
     * Flow con la lista de perfiles actualmente disponibles (initialValue = emptyList()).
     */
    val perfiles: StateFlow<List<Profile>> = repository.getProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
