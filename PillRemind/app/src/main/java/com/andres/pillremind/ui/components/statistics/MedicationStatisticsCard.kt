package com.andres.pillremind.ui.components.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.MedicationStatistics
import com.andres.pillremind.ui.components.DefaultPill
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppImageShape
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Card con las estadísticas de un medicamento en el periodo consultado.
 *
 * @param statistics Resumen del medicamento.
 * @param modifier Modificador opcional para la card.
 */
@Composable
fun MedicationStatisticsCard(
    statistics: MedicationStatistics,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(AppImageShape)
            ) {
                if (!statistics.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = statistics.imageUrl,
                        contentDescription = stringResource(
                            R.string.medication_photo_description,
                            statistics.medicationName
                        ),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    DefaultPill()
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statistics.medicationName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = "${statistics.dose} ${statistics.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.statistics_taken_of_total,
                        statistics.evaluated,
                        statistics.taken,
                        statistics.evaluated
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (statistics.pending > 0) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.statistics_pending_count,
                            statistics.pending,
                            statistics.pending
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (statistics.isArchived) {
                    Text(
                        text = stringResource(R.string.statistics_archived),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(
                    text = statistics.adherencePercent?.let {
                        stringResource(R.string.statistics_percent_value, it)
                    } ?: stringResource(R.string.statistics_no_data),
                    style = MaterialTheme.typography.titleMedium,
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
private fun MedicationStatisticsCardPreview() {
    PillRemindTheme {
        MedicationStatisticsCard(
            statistics = MedicationStatistics(
                medicationId = 1,
                medicationName = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                imageUrl = null,
                isArchived = false,
                taken = 24,
                missed = 3,
                pending = 1
            )
        )
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
private fun MedicationStatisticsCardArchivedPreview() {
    PillRemindTheme(darkTheme = true) {
        MedicationStatisticsCard(
            statistics = MedicationStatistics(
                medicationId = 2,
                medicationName = "Omeprazol",
                dose = 20.0,
                unit = "mg",
                imageUrl = null,
                isArchived = true,
                taken = 30,
                missed = 0,
                pending = 0
            )
        )
    }
}
