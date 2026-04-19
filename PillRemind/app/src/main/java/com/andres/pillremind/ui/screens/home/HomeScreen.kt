package com.andres.pillremind.ui.screens.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.andres.pillremind.data.repository.PerfilRepositoryImpl
import com.andres.pillremind.ui.session.SessionViewModel

@Composable
fun HomeScreen(
    profileId: Int,
    sessionViewModel: SessionViewModel,
    perfilRepository: PerfilRepositoryImpl
) {
    //val nombrePerfil = perfilRepository.getPerfil(1)
    Text(text = "Hola {$profileId}")
}