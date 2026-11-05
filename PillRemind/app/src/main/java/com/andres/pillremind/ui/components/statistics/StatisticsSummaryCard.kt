package com.andres.pillremind.ui.components.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.StatisticsSummary
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Card con el resumen de adherencia de un periodo.
 *
 * Muestra el porcentaje sobre las tomas evaluadas y separa las tomadas, las
 * que se quedaron sin registrar y las que siguen pendientes.
 *
 * @param summary Resumen calculado del periodo.
 * @param modifier Modificador opcional para la card.
 */
@Composable
fun StatisticsSummaryCard(
    summary: StatisticsSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.statistics_adherence_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = summary.adherencePercent?.let {
                            stringResource(R.string.statistics_percent_value, it)
                        } ?: stringResource(R.string.statistics_no_data),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(
                            horizontal = AppSpacing.sm,
                            vertical = AppSpacing.xs
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = AppSpacing.sm),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            StatisticsCountRow(
                label = stringResource(R.string.statistics_taken_label),
                value = summary.taken,
                color = MaterialTheme.colorScheme.tertiary
            )
            StatisticsCountRow(
                label = stringResource(R.string.statistics_missed_label),
                value = summary.missed,
                color = MaterialTheme.colorScheme.error
            )
            StatisticsCountRow(
                label = stringResource(R.string.statistics_pending_label),
                value = summary.pending,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Fila de conteo con un punto de color y una etiqueta.
 *
 * @param label Texto de la métrica.
 * @param value Cantidad que se muestra a la derecha.
 * @param color Color del punto identificativo.
 * @param modifier Modificador opcional para la fila.
 */
@Composable
fun StatisticsCountRow(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(AppSpacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsSummaryCardPreview() {
    PillRemindTheme {
        StatisticsSummaryCard(
            summary = StatisticsSummary(taken = 24, missed = 3, pending = 2)
        )
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
private fun StatisticsSummaryCardLargeTextPreview() {
    PillRemindTheme(darkTheme = true) {
        StatisticsSummaryCard(
            summary = StatisticsSummary(taken = 24, missed = 3, pending = 2)
        )
    }
}
