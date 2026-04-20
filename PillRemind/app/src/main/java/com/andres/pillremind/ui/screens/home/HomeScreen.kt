package com.andres.pillremind.ui.screens.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
) {
    val perfil = homeViewModel.perfil.collectAsState().value

    if (perfil != null) {
        Text(text = "Hola ${perfil.nombre}")
    } else {
        Text(text = "Cargando...")
    }
}