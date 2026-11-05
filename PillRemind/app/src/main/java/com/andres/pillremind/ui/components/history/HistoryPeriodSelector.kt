package com.andres.pillremind.ui.components.history

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.ui.screens.history.HistoryPeriod
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Selector de periodo rápido del historial.
 *
 * Los cuatro periodos se reparten en dos filas para que las etiquetas no se
 * recorten con texto grande.
 *
 * @param selectedPeriod Periodo activo, o `null` si hay un rango personalizado.
 * @param onPeriodSelected Acción ejecutada al elegir un periodo.
 * @param modifier Modificador opcional para el grupo.
 */
@Composable
fun HistoryPeriodSelector(
    selectedPeriod: HistoryPeriod?,
    onPeriodSelected: (HistoryPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        HistoryPeriod.entries.chunked(2).forEach { rowPeriods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                rowPeriods.forEach { period ->
                    PeriodChip(
                        period = period,
                        selected = period == selectedPeriod,
                        onClick = { onPeriodSelected(period) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Chip de un periodo rápido.
 */
@Composable
private fun PeriodChip(
    period: HistoryPeriod,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        shadowElevation = AppElevation,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.xxs, vertical = AppSpacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(period.labelRes()),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
    }
}

/**
 * Etiqueta visible de cada periodo.
 */
@StringRes
private fun HistoryPeriod.labelRes(): Int = when (this) {
    HistoryPeriod.SEVEN -> R.string.statistics_period_seven
    HistoryPeriod.THIRTY -> R.string.statistics_period_thirty
    HistoryPeriod.NINETY -> R.string.statistics_period_ninety
    HistoryPeriod.ALL -> R.string.history_period_all
}

@Preview(showBackground = true)
@Composable
private fun HistoryPeriodSelectorPreview() {
    PillRemindTheme {
        HistoryPeriodSelector(
            selectedPeriod = HistoryPeriod.THIRTY,
            onPeriodSelected = {},
            modifier = Modifier.padding(AppSpacing.md),
        )
    }
}
