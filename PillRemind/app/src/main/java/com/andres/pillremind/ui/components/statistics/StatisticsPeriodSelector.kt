package com.andres.pillremind.ui.components.statistics

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Selector de periodo para la pantalla de estadísticas.
 *
 * Los tres periodos comparten el ancho disponible. El estilo sigue el mismo
 * patrón que los chips de días de la semana: superficie elevada y color
 * primario cuando están seleccionados.
 *
 * @param selectedPeriod Periodo activo.
 * @param onPeriodSelected Acción ejecutada al elegir un periodo.
 * @param modifier Modificador opcional para el grupo.
 */
@Composable
fun StatisticsPeriodSelector(
    selectedPeriod: StatisticsPeriod,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        StatisticsPeriod.entries.forEach { period ->
            val selected = period == selectedPeriod
            Surface(
                selected = selected,
                onClick = { onPeriodSelected(period) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp),
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
                shadowElevation = AppElevation
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppSpacing.xxs, vertical = AppSpacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(period.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Etiqueta visible de cada periodo.
 */
@StringRes
private fun StatisticsPeriod.labelRes(): Int = when (this) {
    StatisticsPeriod.SEVEN -> R.string.statistics_period_seven
    StatisticsPeriod.THIRTY -> R.string.statistics_period_thirty
    StatisticsPeriod.NINETY -> R.string.statistics_period_ninety
}

@Preview(showBackground = true)
@Composable
private fun StatisticsPeriodSelectorPreview() {
    StatisticsPeriodSelector(
        selectedPeriod = StatisticsPeriod.SEVEN,
        onPeriodSelected = {}
    )
}
