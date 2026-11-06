package com.andres.pillremind.ui.components.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Tune
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
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Botón que abre la hoja de filtros del historial.
 *
 * Muestra el rango de fechas activo y cuántos filtros hay aplicados.
 *
 * @param activeFilterCount Número de filtros activos.
 * @param dateRangeSummary Resumen del rango de fechas activo.
 * @param onClick Acción que abre la hoja de filtros.
 * @param modifier Modificador opcional para la tarjeta.
 */
@Composable
fun HistoryFiltersButton(
    activeFilterCount: Int,
    dateRangeSummary: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.history_filter_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = dateRangeSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (activeFilterCount > 0) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.history_filter_active_count,
                            activeFilterCount,
                            activeFilterCount,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(
                            horizontal = AppSpacing.xs,
                            vertical = AppSpacing.xxs,
                        ),
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.xs))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "Filtros sin activar")
@Composable
private fun HistoryFiltersButtonPreview() {
    PillRemindTheme {
        HistoryFiltersButton(
            activeFilterCount = 0,
            dateRangeSummary = "Últimos 30 días",
            onClick = {},
            modifier = Modifier.padding(AppSpacing.md),
        )
    }
}

@Preview(showBackground = true, name = "Filtros activos")
@Composable
private fun HistoryFiltersButtonActivePreview() {
    PillRemindTheme {
        HistoryFiltersButton(
            activeFilterCount = 3,
            dateRangeSummary = "01/10/2026 – 05/11/2026",
            onClick = {},
            modifier = Modifier.padding(AppSpacing.md),
        )
    }
}
