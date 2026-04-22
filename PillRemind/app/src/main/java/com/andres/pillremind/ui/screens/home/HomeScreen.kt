package com.andres.pillremind.ui.screens.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
) {
    val perfil = homeViewModel.profile.collectAsState().value

    if (perfil != null) {
        Text(text = "Hola ${perfil.name}")
    } else {
        Text(text = "Cargando...")
    }
}