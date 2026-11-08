package com.andres.pillremind.ui.components.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.StatisticsSummary
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Banner compacto con el resumen semanal de adherencia.
 *
 * Toda la card abre la pantalla de estadísticas. La pantalla de Inicio lo
 * oculta cuando no hay ninguna toma evaluada.
 *
 * @param summary Resumen de los últimos siete días.
 * @param onClick Acción ejecutada al pulsar la card.
 * @param modifier Modificador opcional para la card.
 */
@Composable
fun StatisticsBanner(
    summary: StatisticsSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Icon(
                imageVector = Icons.Rounded.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.statistics_weekly_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.statistics_taken_of_total,
                        summary.evaluated,
                        summary.taken,
                        summary.evaluated
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (summary.pending > 0) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.statistics_pending_count,
                            summary.pending,
                            summary.pending
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(AppSpacing.xxs))
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
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsBannerPreview() {
    PillRemindTheme {
        StatisticsBanner(
            summary = StatisticsSummary(taken = 17, missed = 3, pending = 2),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
private fun StatisticsBannerLargeTextPreview() {
    PillRemindTheme(darkTheme = true) {
        StatisticsBanner(
            summary = StatisticsSummary(taken = 17, missed = 3, pending = 2),
            onClick = {}
        )
    }
}
