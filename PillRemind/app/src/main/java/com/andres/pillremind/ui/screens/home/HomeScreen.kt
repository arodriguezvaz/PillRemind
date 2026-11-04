package com.andres.pillremind.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.visualState
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.IntakeCard
import com.andres.pillremind.ui.theme.AppSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Fecha que encabeza la pantalla de inicio.
 *
 * Vive en el contenido, no en la top bar: la barra usa una sola línea y la
 * fecha puede envolver a cualquier escala de fuente.
 */
@Composable
private fun homeDateTitle(): String {
    val today = LocalDate.now()
    val datePattern = stringResource(R.string.home_date_pattern)
    val formattedDate = remember(today, datePattern) {
        today.format(DateTimeFormatter.ofPattern(datePattern, Locale.forLanguageTag("es")))
    }
    return stringResource(R.string.home_title_today, formattedDate)
}

/**
 * Pantalla principal que muestra las tomas del día.
 *
 * Obtiene el [HomeViewModel] vía Hilt y observa la lista de tomas.
 *
 * @param modifier Modificador opcional para la pantalla.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel()
) {

    val todayIntakes by homeViewModel.todayIntakes.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md)
    ) {
        Text(
            text = homeDateTitle(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = AppSpacing.sm)
                .semantics { heading() }
        )

        if (todayIntakes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BlankCard(stringResource(R.string.home_empty_today))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
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