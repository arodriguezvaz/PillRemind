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
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppImageShape
import com.andres.pillremind.ui.theme.AppSpacing
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Card de un medicamento archivado, con acciones para restaurarlo o borrarlo.
 *
 * @param medication Medicamento archivado.
 * @param onRestore Acción para recuperar el medicamento.
 * @param onDelete Acción para borrarlo definitivamente.
 * @param modifier Modificador opcional.
 * @param enabled Indica si las acciones están habilitadas.
 */
@Composable
fun ArchivedMedicationCard(
    medication: Medication,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val datePattern = stringResource(R.string.common_date_pattern)
    val archivedDate = medication.deletedAt
        ?.format(DateTimeFormatter.ofPattern(datePattern))
        .orEmpty()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(AppImageShape),
                ) {
                    if (!medication.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = medication.imageUrl,
                            contentDescription = stringResource(
                                R.string.medication_photo_description,
                                medication.name,
                            ),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        DefaultPill()
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    Text(
                        text = "${medication.dose} ${medication.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (archivedDate.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.archived_date, archivedDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    AppSpacing.xs,
                    Alignment.End,
                ),
            ) {
                AppButton(
                    text = stringResource(R.string.archived_restore),
                    onClick = onRestore,
                    variant = ButtonVariant.Tonal,
                    size = ButtonSize.Small,
                    enabled = enabled,
                )
                AppButton(
                    text = stringResource(R.string.archived_delete),
                    onClick = onDelete,
                    variant = ButtonVariant.Danger,
                    size = ButtonSize.Small,
                    enabled = enabled,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchivedMedicationCardPreview() {
    ArchivedMedicationCard(
        medication = Medication(
            id = 1,
            profileId = 1,
            name = "Ibuprofeno",
            dose = 600.0,
            unit = "mg",
            startDate = LocalDate.of(2026, 1, 1),
            deletedAt = LocalDateTime.of(2026, 10, 1, 12, 0),
        ),
        onRestore = {},
        onDelete = {},
    )
}
