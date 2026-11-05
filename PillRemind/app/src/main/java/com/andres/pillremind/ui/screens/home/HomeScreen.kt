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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.visualState
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.IntakeCard
import com.andres.pillremind.ui.components.statistics.StatisticsBanner
import com.andres.pillremind.ui.theme.AppSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Título de la top bar de la pantalla de inicio, con la fecha del día.
 *
 * Lo comparten [HomeScreen] y `MainScreen` para que el título se derive de la
 * ruta actual y no dependa de que la pantalla vuelva a componerse.
 */
@Composable
fun homeTopBarTitle(): String {
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
 * @param onStatisticsClick Acción ejecutada al pulsar el banner de resumen.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onStatisticsClick: () -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel()
) {

    val todayIntakes by homeViewModel.todayIntakes.collectAsStateWithLifecycle()
    val weeklySummary by homeViewModel.weeklySummary.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        homeViewModel.refreshNow()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {

        weeklySummary
            ?.takeIf { it.evaluated > 0 }
            ?.let { summary ->
                StatisticsBanner(
                    summary = summary,
                    onClick = onStatisticsClick
                )
            }

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