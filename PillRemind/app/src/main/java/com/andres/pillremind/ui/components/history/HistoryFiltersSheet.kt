package com.andres.pillremind.ui.components.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.andres.pillremind.R
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.AppDatePickerField
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.settings.SettingsToggleItem
import com.andres.pillremind.ui.screens.history.DEFAULT_HISTORY_DAYS
import com.andres.pillremind.ui.screens.history.HistoryDateFilter
import com.andres.pillremind.ui.screens.history.HistoryFilters
import com.andres.pillremind.ui.screens.history.HistoryMedicationOption
import com.andres.pillremind.ui.screens.history.HistoryStatusFilter
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDate

/**
 * Hoja modal con los filtros avanzados del historial.
 *
 * El rango de fechas se guarda al cerrar la hoja: el botón «Ver resultados»,
 * la X, el gesto de arrastre y el botón atrás aplican antes las fechas
 * editadas si son válidas. «Limpiar filtros» restablece todo y cierra la hoja.
 *
 * @param filters Filtros actuales.
 * @param medications Medicamentos disponibles, incluidos los archivados.
 * @param today Día actual usado para validar el rango personalizado.
 * @param onDismiss Acción para cerrar la hoja.
 * @param onStatusSelected Acción al elegir un estado.
 * @param onMedicationSelected Acción al elegir un medicamento.
 * @param onIncludeArchivedChange Acción al activar o desactivar archivados.
 * @param onApplyCustomRange Acción al aplicar un rango personalizado.
 * @param onClearFilters Acción al limpiar todos los filtros.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFiltersSheet(
    filters: HistoryFilters,
    medications: List<HistoryMedicationOption>,
    today: LocalDate,
    onDismiss: () -> Unit,
    onStatusSelected: (HistoryStatusFilter) -> Unit,
    onMedicationSelected: (HistoryMedicationOption?) -> Unit,
    onIncludeArchivedChange: (Boolean) -> Unit,
    onApplyCustomRange: (LocalDate, LocalDate) -> Unit,
    onClearFilters: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialRange = remember(filters.dateFilter, today) {
        filters.dateFilter.prefill(today)
    }
    var fromDate by rememberSaveable { mutableStateOf<LocalDate?>(initialRange.first) }
    var toDate by rememberSaveable { mutableStateOf<LocalDate?>(initialRange.second) }
    var datesEdited by rememberSaveable { mutableStateOf(false) }

    val from = fromDate
    val to = toDate
    val rangeIsValid = from != null && to != null && !from.isAfter(to) && !to.isAfter(today)

    val dismissWithPendingRange = {
        if (datesEdited && from != null && to != null && rangeIsValid) {
            onApplyCustomRange(from, to)
        }
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = dismissWithPendingRange,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        HistoryFiltersSheetContent(
            filters = filters,
            medications = medications,
            today = today,
            fromDate = fromDate,
            toDate = toDate,
            rangeIsValid = rangeIsValid,
            onFromDateSelected = {
                fromDate = it
                datesEdited = true
            },
            onToDateSelected = {
                toDate = it
                datesEdited = true
            },
            onDismiss = dismissWithPendingRange,
            onStatusSelected = onStatusSelected,
            onMedicationSelected = onMedicationSelected,
            onIncludeArchivedChange = onIncludeArchivedChange,
            onClearFilters = {
                onClearFilters()
                datesEdited = false
                onDismiss()
            },
        )
    }
}

/**
 * Contenido de la hoja de filtros, separado para poder previsualizarlo.
 */
@Composable
fun HistoryFiltersSheetContent(
    filters: HistoryFilters,
    medications: List<HistoryMedicationOption>,
    today: LocalDate,
    fromDate: LocalDate?,
    toDate: LocalDate?,
    rangeIsValid: Boolean,
    onFromDateSelected: (LocalDate?) -> Unit,
    onToDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit,
    onStatusSelected: (HistoryStatusFilter) -> Unit,
    onMedicationSelected: (HistoryMedicationOption?) -> Unit,
    onIncludeArchivedChange: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusAll = stringResource(R.string.history_filter_status_all)
    val statusTaken = stringResource(R.string.history_filter_status_taken)
    val statusMissed = stringResource(R.string.history_filter_status_missed)
    val statusPending = stringResource(R.string.history_filter_status_pending)
    val statusLabels = listOf(statusAll, statusTaken, statusMissed, statusPending)
    val statusByLabel = mapOf(
        statusAll to HistoryStatusFilter.ALL,
        statusTaken to HistoryStatusFilter.TAKEN,
        statusMissed to HistoryStatusFilter.MISSED,
        statusPending to HistoryStatusFilter.PENDING,
    )
    val statusValue = when (filters.status) {
        HistoryStatusFilter.ALL -> statusAll
        HistoryStatusFilter.TAKEN -> statusTaken
        HistoryStatusFilter.MISSED -> statusMissed
        HistoryStatusFilter.PENDING -> statusPending
    }

    val medicationAll = stringResource(R.string.history_filter_medication_all)
    val medicationLabels = mutableListOf(medicationAll)
    val medicationByLabel = mutableMapOf<String, HistoryMedicationOption?>(medicationAll to null)
    medications.forEach { option ->
        val label = if (option.isArchived) {
            stringResource(R.string.history_medication_archived_option, option.name)
        } else {
            option.name
        }
        medicationLabels += label
        medicationByLabel[label] = option
    }
    val selectedMedication = medications.firstOrNull { it.id == filters.medicationId }
    val medicationValue = when {
        selectedMedication == null -> medicationAll
        selectedMedication.isArchived -> stringResource(
            R.string.history_medication_archived_option,
            selectedMedication.name,
        )
        else -> selectedMedication.name
    }

    val showRangeError = fromDate != null && toDate != null && !rangeIsValid

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = AppSpacing.md)
            .padding(top = AppSpacing.md, bottom = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.history_filter_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.common_close),
                )
            }
        }

        AppFormField(label = stringResource(R.string.history_filter_status)) {
            AppDropdownField(
                value = statusValue,
                options = statusLabels,
                onOptionSelected = { label ->
                    statusByLabel[label]?.let(onStatusSelected)
                },
            )
        }

        AppFormField(label = stringResource(R.string.history_filter_medication)) {
            AppDropdownField(
                value = medicationValue,
                options = medicationLabels,
                onOptionSelected = { label ->
                    onMedicationSelected(medicationByLabel[label])
                },
            )
        }

        SettingsToggleItem(
            label = stringResource(R.string.history_filter_include_archived),
            checked = filters.includeArchived,
            onCheckedChange = onIncludeArchivedChange,
        )

        Text(
            text = stringResource(R.string.history_filter_custom_dates),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        AppFormField(label = stringResource(R.string.history_filter_date_from)) {
            AppDatePickerField(
                value = fromDate,
                onDateSelected = onFromDateSelected,
                isError = showRangeError,
                maxDate = toDate ?: today,
            )
        }

        AppFormField(label = stringResource(R.string.history_filter_date_to)) {
            AppDatePickerField(
                value = toDate,
                onDateSelected = onToDateSelected,
                isError = showRangeError,
                minDate = fromDate,
                maxDate = today,
            )
        }

        if (showRangeError) {
            Text(
                text = stringResource(R.string.history_filter_date_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        AppButton(
            text = stringResource(R.string.history_filter_show_results),
            onClick = onDismiss,
            enabled = rangeIsValid,
            variant = ButtonVariant.Primary,
            size = ButtonSize.Large,
        )

        AppButton(
            text = stringResource(R.string.history_filter_clear),
            onClick = onClearFilters,
            variant = ButtonVariant.Secondary,
            size = ButtonSize.Large,
        )
    }
}

/**
 * Rango inicial de los campos de fecha a partir del filtro actual.
 */
private fun HistoryDateFilter.prefill(today: LocalDate): Pair<LocalDate, LocalDate> = when (this) {
    HistoryDateFilter.Default -> today.minusDays(DEFAULT_HISTORY_DAYS - 1) to today
    is HistoryDateFilter.Custom -> from to to
}

@Preview(showBackground = true, name = "Hoja de filtros")
@Composable
private fun HistoryFiltersSheetContentPreview() {
    PillRemindTheme {
        HistoryFiltersSheetContent(
            filters = HistoryFilters(
                status = HistoryStatusFilter.TAKEN,
                medicationId = 2,
            ),
            medications = listOf(
                HistoryMedicationOption(1, "Ibuprofeno", false),
                HistoryMedicationOption(2, "Omeprazol", true),
            ),
            today = LocalDate.of(2026, 11, 6),
            fromDate = LocalDate.of(2026, 10, 8),
            toDate = LocalDate.of(2026, 11, 6),
            rangeIsValid = true,
            onFromDateSelected = {},
            onToDateSelected = {},
            onDismiss = {},
            onStatusSelected = {},
            onMedicationSelected = {},
            onIncludeArchivedChange = {},
            onClearFilters = {},
            modifier = Modifier.padding(vertical = AppSpacing.md),
        )
    }
}
