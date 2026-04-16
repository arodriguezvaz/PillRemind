package com.andres.pillremind.ui.screens.createprofile

import androidx.lifecycle.ViewModel
import com.andres.pillremind.domain.repository.PerfilRepository

class CreateProfileViewModel(private val repository: PerfilRepository) : ViewModel() {
    val perfiles: State
}