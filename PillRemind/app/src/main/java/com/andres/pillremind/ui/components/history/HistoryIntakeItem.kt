package com.andres.pillremind.ui.components.history

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsStatus
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Escala de fuente a partir de la cual los badges de una toma archivada pasan
 * a una línea inferior para no recortar el nombre del medicamento.
 */
private const val LARGE_FONT_SCALE = 1.3f

/**
 * Fila del historial de tomas.
 *
 * La hora, el medicamento y la dosis se muestran a la izquierda. A la derecha
 * quedan el estado y, si procede, el badge de archivado, centrados
 * verticalmente en la card.
 *
 * @param intake Toma que se muestra.
 * @param status Estado calculado de la toma.
 * @param modifier Modificador opcional para la fila.
 */
@Composable
fun HistoryIntakeItem(
    intake: StatisticsIntake,
    status: StatisticsStatus,
    modifier: Modifier = Modifier,
) {
    val timePattern = stringResource(R.string.common_time_pattern)
    val timeFormatter = DateTimeFormatter.ofPattern(timePattern)
    val timeText = intake.scheduledAt.format(timeFormatter)
    val useStackedBadges = intake.isArchived &&
        LocalDensity.current.fontScale >= LARGE_FONT_SCALE

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation)
    ) {
        if (useStackedBadges) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
            ) {
                IntakeSummary(
                    time = timeText,
                    intake = intake,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    IntakeBadges(status = status, isArchived = true)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                IntakeSummary(
                    time = timeText,
                    intake = intake,
                    modifier = Modifier.weight(1f),
                )
                IntakeBadges(status = status, isArchived = intake.isArchived)
            }
        }
    }
}

/**
 * Hora, nombre y dosis de la toma.
 */
@Composable
private fun IntakeSummary(
    time: String,
    intake: StatisticsIntake,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = time,
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
        Text(
            text = "${intake.dose} ${intake.unit}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Badges de estado y archivado de una toma.
 */
@Composable
private fun IntakeBadges(
    status: StatisticsStatus,
    isArchived: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        StatusBadge(status = status)
        if (isArchived) {
            ArchivedBadge()
        }
    }
}

/**
 * Etiqueta compacta con el estado de la toma.
 */
@Composable
private fun StatusBadge(status: StatisticsStatus) {
    val (containerColor, contentColor) = when (status) {
        StatisticsStatus.TAKEN -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer

        StatisticsStatus.MISSED -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer

        StatisticsStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer
    }

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

/**
 * Etiqueta compacta para las tomas de medicamentos archivados.
 */
@Composable
private fun ArchivedBadge() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = stringResource(R.string.history_archived),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(
                horizontal = AppSpacing.xs,
                vertical = AppSpacing.xxs,
            ),
        )
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
private fun HistoryIntakeItemPreview() {
    PillRemindTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            HistoryIntakeItem(
                intake = previewIntake(taken = true),
                status = StatisticsStatus.TAKEN
            )
            HistoryIntakeItem(
                intake = previewIntake(taken = false),
                status = StatisticsStatus.MISSED
            )
            HistoryIntakeItem(
                intake = previewIntake(taken = false, archived = true),
                status = StatisticsStatus.PENDING
            )
            HistoryIntakeItem(
                intake = previewIntake(taken = true, archived = true),
                status = StatisticsStatus.TAKEN
            )
        }
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
private fun HistoryIntakeItemLargeTextPreview() {
    PillRemindTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            HistoryIntakeItem(
                intake = previewIntake(taken = false, archived = true),
                status = StatisticsStatus.MISSED
            )
            HistoryIntakeItem(
                intake = previewIntake(taken = true, archived = true),
                status = StatisticsStatus.TAKEN
            )
        }
    }
}

private fun previewIntake(taken: Boolean, archived: Boolean = false): StatisticsIntake =
    StatisticsIntake(
        intakeId = 1,
        medicationId = 1,
        scheduledAt = LocalDateTime.of(2026, 11, 5, 9, 0),
        takenAt = if (taken) LocalDateTime.of(2026, 11, 5, 9, 5) else null,
        medicationName = "Ibuprofeno",
        dose = 600.0,
        unit = "mg",
        imageUrl = null,
        isArchived = archived
    )
