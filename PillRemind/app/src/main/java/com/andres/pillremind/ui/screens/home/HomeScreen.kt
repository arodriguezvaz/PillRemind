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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import com.andres.pillremind.ui.theme.AppSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Cada cuánto se refresca el reloj de la pantalla de inicio. */
private const val CLOCK_TICK_MILLIS = 30_000L

/**
 * Título de la top bar de la pantalla de inicio, con la fecha del día.
 *
 * Lo comparten [HomeScreen] y `MainScreen` para que el título se derive de la
 * ruta actual y no dependa de que la pantalla vuelva a componerse. Se mantiene
 * actualizado con un ticker para que el cambio de día también se refleje en la
 * barra superior.
 */
@Composable
fun homeTopBarTitle(): String {
    val today by produceState(initialValue = LocalDate.now()) {
        while (true) {
            delay(60_000L)
            value = LocalDate.now()
        }
    }
    val datePattern = stringResource(R.string.home_date_pattern)
    val formattedDate = remember(today, datePattern) {
        today.format(DateTimeFormatter.ofPattern(datePattern, Locale.forLanguageTag("es")))
    }
    return stringResource(R.string.home_title_today, formattedDate)
}

/**
 * Pantalla principal que muestra las tomas del día.
 *
 * Obtiene el [HomeViewModel] vía Hilt, observa la lista de tomas y refresca el
 * reloj al volver a primer plano para no quedarse en el día anterior.
 *
 * @param modifier Modificador opcional para la pantalla.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel()
) {

    val todayIntakes by homeViewModel.todayIntakes.collectAsStateWithLifecycle()
    val now by homeViewModel.now.collectAsStateWithLifecycle()
    val remindersError by homeViewModel.remindersError.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        homeViewModel.refreshClock()
        homeViewModel.refreshReminders()
    }

    LaunchedEffect(homeViewModel) {
        while (isActive) {
            delay(CLOCK_TICK_MILLIS)
            homeViewModel.refreshClock()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md)
    ) {
        if (remindersError) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.sm),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Column(modifier = Modifier.padding(AppSpacing.md)) {
                    Text(
                        text = stringResource(R.string.home_reminders_error),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = homeViewModel::refreshReminders) {
                        Text(text = stringResource(R.string.home_reminders_retry))
                    }
                }
            }
        }

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
                        visualState = intake.visualState(now),
                        onTake = {
                            homeViewModel.tomarIntake(intake)
                        }
                    )
                }
            }
        }
    }
}
