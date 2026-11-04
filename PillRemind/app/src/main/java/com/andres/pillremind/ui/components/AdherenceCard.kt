package com.andres.pillremind.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.MedicationAdherence
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppImageShape
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Card con el resumen de adherencia de un medicamento.
 *
 * @param adherence Estadísticas del medicamento en el periodo elegido.
 * @param modifier Modificador opcional.
 */
@Composable
fun AdherenceCard(
    adherence: MedicationAdherence,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(AppImageShape),
            ) {
                if (!adherence.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = adherence.imageUrl,
                        contentDescription = stringResource(
                            R.string.medication_photo_description,
                            adherence.medicationName
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
                    text = adherence.medicationName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                Text(
                    text = "${adherence.dose} ${adherence.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.adherence_counts,
                        adherence.taken,
                        adherence.missed,
                        adherence.pending,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (adherence.isArchived) {
                    Text(
                        text = stringResource(R.string.adherence_archived),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Text(
                    text = adherence.adherencePercent?.let {
                        stringResource(R.string.adherence_percent_value, it)
                    } ?: stringResource(R.string.adherence_no_percent),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        horizontal = AppSpacing.sm,
                        vertical = AppSpacing.xs,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdherenceCardPreview() {
    AdherenceCard(
        adherence = MedicationAdherence(
            medicationId = 1,
            medicationName = "Ibuprofeno",
            dose = 600.0,
            unit = "mg",
            imageUrl = null,
            isArchived = false,
            total = 30,
            taken = 24,
            missed = 3,
        )
    )
}
