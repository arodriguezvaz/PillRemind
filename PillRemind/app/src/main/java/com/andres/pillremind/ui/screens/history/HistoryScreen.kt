package com.andres.pillremind.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsStatus
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.AppSearchField
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.history.HistoryDayHeader
import com.andres.pillremind.ui.components.history.HistoryFiltersButton
import com.andres.pillremind.ui.components.history.HistoryFiltersSheet
import com.andres.pillremind.ui.components.history.HistoryIntakeItem
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val CLOCK_TICK_MILLIS = 60_000L

/**
 * Pantalla con el historial de tomas del perfil activo.
 *
 * Permite buscar por medicamento y filtrar por estado, rango de fechas,
 * medicamento y tomas de medicamentos archivados. Se abre desde el perfil o
 * desde la ficha de un medicamento.
 *
 * @param modifier Modificador opcional.
 * @param historyViewModel ViewModel con los filtros y las tomas.
 */
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel = hiltViewModel(),
) {
    val filters by historyViewModel.filters.collectAsStateWithLifecycle()
    val uiState by historyViewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showFilters by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        historyViewModel.refreshNow()
    }

    LaunchedEffect(historyViewModel) {
        while (isActive) {
            delay(CLOCK_TICK_MILLIS)
            historyViewModel.refreshNow()
        }
    }

    HistoryContent(
        filters = filters,
        uiState = uiState,
        onSearchQueryChange = historyViewModel::onSearchQueryChange,
        onFiltersClick = {
            focusManager.clearFocus()
            showFilters = true
        },
        onClearFilters = historyViewModel::clearFilters,
        modifier = modifier,
    )

    if (showFilters) {
        HistoryFiltersSheet(
            filters = filters,
            medications = uiState.medications,
            today = uiState.now.toLocalDate(),
            onDismiss = { showFilters = false },
            onStatusSelected = historyViewModel::selectStatus,
            onMedicationSelected = historyViewModel::selectMedication,
            onIncludeArchivedChange = historyViewModel::setIncludeArchived,
            onApplyCustomRange = historyViewModel::applyCustomRange,
            onClearFilters = historyViewModel::clearFilters,
        )
    }
}

/**
 * Contenido de la pantalla, separado del ViewModel para poder previsualizarlo.
 */
@Composable
private fun HistoryContent(
    filters: HistoryFilters,
    uiState: HistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onFiltersClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val datePattern = stringResource(R.string.common_date_pattern)
    val dateFormatter = remember(datePattern) { DateTimeFormatter.ofPattern(datePattern) }
    val dateRangeSummary = when (val dateFilter = filters.dateFilter) {
        HistoryDateFilter.Default -> stringResource(R.string.history_default_range_summary)
        is HistoryDateFilter.Custom -> stringResource(
            R.string.history_custom_range_summary,
            dateFilter.from.format(dateFormatter),
            dateFilter.to.format(dateFormatter),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        AppSearchField(
            value = filters.query,
            onValueChange = onSearchQueryChange,
            placeholder = stringResource(R.string.history_search_placeholder),
            modifier = Modifier.fillMaxWidth(),
        )

        HistoryFiltersButton(
            activeFilterCount = filters.activeFilterCount,
            dateRangeSummary = dateRangeSummary,
            onClick = onFiltersClick,
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.entries.isEmpty() -> {
                EmptyHistory(
                    hasMedications = uiState.hasMedications,
                    filters = filters,
                    onClearFilters = onClearFilters,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                val filteredCount = filters.hasNarrowingFilters ||
                    filters.dateFilter is HistoryDateFilter.Custom
                Text(
                    text = if (filteredCount && uiState.totalInRange > uiState.entries.size) {
                        pluralStringResource(
                            R.plurals.history_result_count_filtered,
                            uiState.totalInRange,
                            uiState.entries.size,
                            uiState.totalInRange,
                        )
                    } else {
                        pluralStringResource(
                            R.plurals.history_result_count,
                            uiState.entries.size,
                            uiState.entries.size,
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                HistoryList(
                    entries = uiState.entries,
                    today = uiState.now.toLocalDate(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Estado vacío del historial.
 */
@Composable
private fun EmptyHistory(
    hasMedications: Boolean,
    filters: HistoryFilters,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when {
        !hasMedications -> stringResource(R.string.history_empty_no_medications)
        filters.hasNarrowingFilters -> stringResource(R.string.history_empty_no_results)
        else -> stringResource(R.string.history_empty_no_intakes)
    }
    val showClear = filters != HistoryFilters()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BlankCard(message)
        if (showClear) {
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            AppButton(
                text = stringResource(R.string.history_filter_clear),
                onClick = onClearFilters,
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Large,
            )
        }
    }
}

/**
 * Lista de tomas agrupada por día.
 */
@Composable
private fun HistoryList(
    entries: List<HistoryEntry>,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        val entriesByDay = entries.groupBy { it.intake.scheduledAt.toLocalDate() }
        entriesByDay.forEach { (date, dayEntries) ->
            item(key = "history-day-$date") {
                HistoryDayHeader(date = date, today = today)
            }
            items(
                items = dayEntries,
                key = { it.intake.intakeId },
            ) { entry ->
                HistoryIntakeItem(
                    intake = entry.intake,
                    status = entry.status,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Historial")
@Composable
private fun HistoryContentPreview() {
    PillRemindTheme {
        PreviewHistoryContent()
    }
}

@Preview(showBackground = true, fontScale = 1.5f, name = "Historial oscuro")
@Composable
private fun HistoryContentDarkPreview() {
    PillRemindTheme(darkTheme = true) {
        PreviewHistoryContent()
    }
}

@Composable
private fun PreviewHistoryContent() {
    val now = LocalDateTime.of(2026, 11, 6, 12, 0)
    val entries = listOf(
        HistoryEntry(
            intake = previewIntake(
                id = 1,
                scheduledAt = now.withHour(9).withMinute(0),
                takenAt = now.withHour(9).withMinute(5),
            ),
            status = StatisticsStatus.TAKEN,
        ),
        HistoryEntry(
            intake = previewIntake(
                id = 2,
                scheduledAt = now.withHour(21).withMinute(0),
                takenAt = null,
            ),
            status = StatisticsStatus.PENDING,
        ),
        HistoryEntry(
            intake = previewIntake(
                id = 3,
                scheduledAt = now.minusDays(1).withHour(9).withMinute(0),
                takenAt = null,
            ),
            status = StatisticsStatus.MISSED,
        ),
        HistoryEntry(
            intake = previewIntake(
                id = 4,
                scheduledAt = now.minusDays(1).withHour(21).withMinute(0),
                takenAt = now.minusDays(1).withHour(21).withMinute(3),
                archived = true,
            ),
            status = StatisticsStatus.TAKEN,
        ),
    )

    HistoryContent(
        filters = HistoryFilters(),
        uiState = HistoryUiState(
            isLoading = false,
            hasMedications = true,
            entries = entries,
            medications = listOf(
                HistoryMedicationOption(1, "Ibuprofeno", false),
                HistoryMedicationOption(2, "Omeprazol", true),
            ),
            totalInRange = entries.size,
            now = now,
        ),
        onSearchQueryChange = {},
        onFiltersClick = {},
        onClearFilters = {},
    )
}

private fun previewIntake(
    id: Int,
    scheduledAt: LocalDateTime,
    takenAt: LocalDateTime?,
    archived: Boolean = false,
): StatisticsIntake = StatisticsIntake(
    intakeId = id,
    medicationId = if (archived) 2 else 1,
    scheduledAt = scheduledAt,
    takenAt = takenAt,
    medicationName = if (archived) "Omeprazol" else "Ibuprofeno",
    dose = if (archived) 20.0 else 600.0,
    unit = "mg",
    imageUrl = null,
    isArchived = archived,
)
