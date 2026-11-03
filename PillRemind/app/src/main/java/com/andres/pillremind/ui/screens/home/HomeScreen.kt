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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.visualState
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.IntakeCard
import com.andres.pillremind.ui.screens.main.TopBarState
import com.andres.pillremind.ui.theme.AppSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    onTopBarChange: (TopBarState) -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel()
) {

    val todayIntakes by homeViewModel.todayIntakes.collectAsState()
    val today = LocalDate.now()
    val datePattern = stringResource(R.string.home_date_pattern)
    val formattedDate = remember(today, datePattern) {
        today.format(DateTimeFormatter.ofPattern(datePattern, Locale.forLanguageTag("es")))
    }
    val title = stringResource(R.string.home_title_today, formattedDate)
    LaunchedEffect(title) {
        onTopBarChange(
            TopBarState(
                title = title,
                showLogo = true
            )
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md)
    ) {

        if (todayIntakes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BlankCard(stringResource(R.string.home_empty_today))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), // importante
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