package com.andres.pillremind.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.andres.pillremind.data.local.projections.visualState
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.IntakeCard
import com.andres.pillremind.ui.screens.main.TopBarState

/**
 * Pantalla principal que muestra la lista de medicamentos del perfil activo.
 *
 * Obtiene el [HomeViewModel] vía Hilt y observa la lista de medicamentos.
 *
 * @param modifier Modificador opcional para la pantalla.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onTopBarChange: (TopBarState) -> Unit = {}
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val todayIntakes by homeViewModel.todayIntakes.collectAsState()
    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = "Medicamentos",
                showLogo = true
            )
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if (todayIntakes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BlankCard("No hay medicamentos para tomar hoy")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), // importante
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = todayIntakes,
                    key = { it.intakeId }
                ) { intake ->
                    IntakeCard(
                        intake = intake,
                        visualState = intake.visualState(),
                        onTake = {
                            homeViewModel.tomarIntake(intake)
                        }
                    )
                }
            }
        }
    }
}