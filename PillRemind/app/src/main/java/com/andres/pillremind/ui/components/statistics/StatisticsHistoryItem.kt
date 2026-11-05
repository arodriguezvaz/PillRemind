package com.andres.pillremind.ui.components.statistics

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsStatus
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Fila del historial de tomas de la pantalla de estadísticas.
 *
 * @param intake Toma que se muestra.
 * @param status Estado calculado de la toma.
 * @param modifier Modificador opcional para la fila.
 */
@Composable
fun StatisticsHistoryItem(
    intake: StatisticsIntake,
    status: StatisticsStatus,
    modifier: Modifier = Modifier
) {
    val timePattern = stringResource(R.string.common_time_pattern)
    val timeFormatter = DateTimeFormatter.ofPattern(timePattern)
    val (containerColor, contentColor) = when (status) {
        StatisticsStatus.TAKEN -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer
        StatisticsStatus.MISSED -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
        StatisticsStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant to
            MaterialTheme.colorScheme.onSurfaceVariant
    }

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
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = intake.scheduledAt.format(timeFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Text(
                    text = intake.medicationName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Text(
                    text = "${intake.dose} ${intake.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = containerColor,
                    contentColor = contentColor
                ) {
                    Text(
                        text = stringResource(status.labelRes()),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(
                            horizontal = AppSpacing.xs,
                            vertical = AppSpacing.xxs
                        )
                    )
                }
            }
        }
    }
}

/**
 * Etiqueta visible del estado de una toma.
 */
@StringRes
private fun StatisticsStatus.labelRes(): Int = when (this) {
    StatisticsStatus.TAKEN -> R.string.statistics_status_taken
    StatisticsStatus.MISSED -> R.string.statistics_status_missed
    StatisticsStatus.PENDING -> R.string.statistics_status_pending
}

@Preview(showBackground = true)
@Composable
private fun StatisticsHistoryItemPreview() {
    PillRemindTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            StatisticsHistoryItem(
                intake = previewIntake(taken = true),
                status = StatisticsStatus.TAKEN
            )
            StatisticsHistoryItem(
                intake = previewIntake(taken = false),
                status = StatisticsStatus.MISSED
            )
            StatisticsHistoryItem(
                intake = previewIntake(taken = false),
                status = StatisticsStatus.PENDING
            )
        }
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
private fun StatisticsHistoryItemLargeTextPreview() {
    PillRemindTheme(darkTheme = true) {
        StatisticsHistoryItem(
            intake = previewIntake(taken = false),
            status = StatisticsStatus.MISSED
        )
    }
}

private fun previewIntake(taken: Boolean): StatisticsIntake = StatisticsIntake(
    intakeId = 1,
    medicationId = 1,
    scheduledAt = LocalDateTime.of(2026, 11, 5, 9, 0),
    takenAt = if (taken) LocalDateTime.of(2026, 11, 5, 9, 5) else null,
    medicationName = "Ibuprofeno",
    dose = 600.0,
    unit = "mg",
    imageUrl = null,
    isArchived = false
)
