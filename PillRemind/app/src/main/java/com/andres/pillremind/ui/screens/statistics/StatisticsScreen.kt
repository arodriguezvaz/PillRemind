package com.andres.pillremind.ui.screens.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.domain.model.status
import com.andres.pillremind.domain.model.toStatisticsReport
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.statistics.MedicationStatisticsCard
import com.andres.pillremind.ui.components.statistics.StatisticsHistoryItem
import com.andres.pillremind.ui.components.statistics.StatisticsPeriodSelector
import com.andres.pillremind.ui.components.statistics.StatisticsSummaryCard
import com.andres.pillremind.ui.components.statistics.WeeklyStatisticsChart
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val CLOCK_TICK_MILLIS = 60_000L

/**
 * Pantalla con las estadísticas de tomas del perfil activo.
 *
 * Muestra el resumen de adherencia, el gráfico semanal, el desglose por
 * medicamento y el historial del periodo seleccionado. El cálculo se hace en
 * el dominio y esta pantalla solo presenta el resultado.
 *
 * @param modifier Modificador opcional.
 * @param statisticsViewModel ViewModel con el periodo y los datos calculados.
 */
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    statisticsViewModel: StatisticsViewModel = hiltViewModel()
) {
    val period by statisticsViewModel.period.collectAsStateWithLifecycle()
    val uiState by statisticsViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        statisticsViewModel.refreshNow()
    }

    LaunchedEffect(statisticsViewModel) {
        while (isActive) {
            delay(CLOCK_TICK_MILLIS)
            statisticsViewModel.refreshNow()
        }
    }

    StatisticsContent(
        period = period,
        uiState = uiState,
        onPeriodSelected = statisticsViewModel::selectPeriod,
        modifier = modifier
    )
}

/**
 * Contenido de la pantalla, separado del ViewModel para poder previsualizarlo.
 */
@Composable
private fun StatisticsContent(
    period: StatisticsPeriod,
    uiState: StatisticsUiState,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AppSpacing.md,
            end = AppSpacing.md,
            top = AppSpacing.md,
            bottom = AppSpacing.md
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        item(key = "period") {
            StatisticsPeriodSelector(
                selectedPeriod = period,
                onPeriodSelected = onPeriodSelected
            )
        }

        when {
            uiState.isLoading -> {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            !uiState.hasMedications -> {
                item(key = "empty-medications") {
                    BlankCard(stringResource(R.string.statistics_empty_no_medications))
                }
                item(key = "footnote") {
                    StatisticsFootnote()
                }
            }

            uiState.report.history.isEmpty() -> {
                item(key = "empty-intakes") {
                    BlankCard(stringResource(R.string.statistics_empty_no_intakes))
                }
                item(key = "footnote") {
                    StatisticsFootnote()
                }
            }

            else -> {
                item(key = "summary") {
                    StatisticsSummaryCard(summary = uiState.report.summary)
                }

                if (period == StatisticsPeriod.SEVEN) {
                    item(key = "chart") {
                        WeeklyStatisticsChart(days = uiState.report.lastSevenDays)
                    }
                }

                item(key = "medication-title") {
                    StatisticsSectionTitle(
                        text = stringResource(R.string.statistics_by_medication_title)
                    )
                }
                items(
                    items = uiState.report.byMedication,
                    key = { "medication-${it.medicationId}" }
                ) { medication ->
                    MedicationStatisticsCard(statistics = medication)
                }

                item(key = "history-title") {
                    StatisticsSectionTitle(
                        text = stringResource(R.string.statistics_history_title)
                    )
                }
                val historyByDay = uiState.report.history.groupBy {
                    it.scheduledAt.toLocalDate()
                }
                historyByDay.forEach { (date, intakes) ->
                    item(key = "history-day-$date") {
                        HistoryDayHeader(
                            date = date,
                            today = uiState.now.toLocalDate()
                        )
                    }
                    items(
                        items = intakes,
                        key = { "history-${it.intakeId}" }
                    ) { intake ->
                        StatisticsHistoryItem(
                            intake = intake,
                            status = intake.status(uiState.now)
                        )
                    }
                }

                item(key = "footnote") {
                    StatisticsFootnote()
                }
            }
        }
    }
}

/**
 * Título de una sección de la pantalla.
 */
@Composable
private fun StatisticsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = AppSpacing.sm)
    )
}

/**
 * Cabecera de un día del historial.
 *
 * @param date Día que se representa.
 * @param today Día actual según el momento de referencia del informe.
 */
@Composable
private fun HistoryDayHeader(date: LocalDate, today: LocalDate) {
    val text = when (date) {
        today -> stringResource(R.string.statistics_history_today)
        today.minusDays(1) -> stringResource(R.string.statistics_history_yesterday)
        else -> {
            val pattern = stringResource(R.string.statistics_history_date_pattern)
            date.format(DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("es")))
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = AppSpacing.xs)
    )
}

/**
 * Nota al pie con el criterio de cálculo.
 */
@Composable
private fun StatisticsFootnote() {
    Text(
        text = stringResource(R.string.statistics_footnote),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = AppSpacing.sm)
    )
}

@Preview(showBackground = true)
@Composable
private fun StatisticsContentPreview() {
    PillRemindTheme {
        PreviewStatisticsContent()
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
private fun StatisticsContentDarkPreview() {
    PillRemindTheme(darkTheme = true) {
        PreviewStatisticsContent()
    }
}

@Composable
private fun PreviewStatisticsContent() {
    val now = LocalDateTime.of(2026, 11, 5, 12, 0)
    val intakes = previewIntakes(now)
    StatisticsContent(
        period = StatisticsPeriod.SEVEN,
        uiState = StatisticsUiState(
            isLoading = false,
            hasMedications = true,
            report = intakes.toStatisticsReport(now),
            now = now
        ),
        onPeriodSelected = {}
    )
}

private fun previewIntakes(now: LocalDateTime): List<StatisticsIntake> = listOf(
    StatisticsIntake(
        intakeId = 1,
        medicationId = 1,
        scheduledAt = now.minusDays(1).withHour(9).withMinute(0),
        takenAt = now.minusDays(1).withHour(9).withMinute(5),
        medicationName = "Ibuprofeno",
        dose = 600.0,
        unit = "mg",
        imageUrl = null,
        isArchived = false
    ),
    StatisticsIntake(
        intakeId = 2,
        medicationId = 1,
        scheduledAt = now.minusDays(1).withHour(21).withMinute(0),
        takenAt = null,
        medicationName = "Ibuprofeno",
        dose = 600.0,
        unit = "mg",
        imageUrl = null,
        isArchived = false
    ),
    StatisticsIntake(
        intakeId = 3,
        medicationId = 2,
        scheduledAt = now.withHour(9).withMinute(0),
        takenAt = now.withHour(9).withMinute(2),
        medicationName = "Omeprazol",
        dose = 20.0,
        unit = "mg",
        imageUrl = null,
        isArchived = true
    ),
    StatisticsIntake(
        intakeId = 4,
        medicationId = 2,
        scheduledAt = now.withHour(21).withMinute(0),
        takenAt = null,
        medicationName = "Omeprazol",
        dose = 20.0,
        unit = "mg",
        imageUrl = null,
        isArchived = true
    )
)
