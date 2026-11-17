package com.andres.pillremind.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.andres.pillremind.R
import com.andres.pillremind.data.pdf.AdherencePdfLabels
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.ui.components.statistics.StatisticsPeriodSelector
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Diálogo para elegir el periodo del informe de adherencia.
 *
 * @param period Periodo seleccionado.
 * @param onPeriodSelected Acción al cambiar el periodo.
 * @param onDismiss Acción al cancelar.
 * @param onConfirm Acción al confirmar la exportación.
 */
@Composable
fun ProfileReportPeriodDialog(
    period: StatisticsPeriod,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_report_period_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    text = stringResource(R.string.profile_report_period_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatisticsPeriodSelector(
                    selectedPeriod = period,
                    onPeriodSelected = onPeriodSelected
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.profile_report_export_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

/**
 * Resuelve con recursos de la app los textos del informe en PDF.
 */
@Composable
fun adherencePdfLabels(): AdherencePdfLabels = AdherencePdfLabels(
    title = stringResource(R.string.statistics_pdf_title),
    profileLabel = stringResource(R.string.statistics_pdf_profile),
    periodLabel = stringResource(R.string.statistics_pdf_period),
    generatedAtLabel = stringResource(R.string.statistics_pdf_generated_at),
    adherenceLabel = stringResource(R.string.statistics_adherence_label),
    evaluatedLabel = stringResource(R.string.statistics_pdf_evaluated),
    takenLabel = stringResource(R.string.statistics_taken_label),
    missedLabel = stringResource(R.string.statistics_missed_label),
    pendingLabel = stringResource(R.string.statistics_pending_label),
    noDataLabel = stringResource(R.string.statistics_no_data),
    archivedLabel = stringResource(R.string.statistics_archived),
    byMedicationTitle = stringResource(R.string.statistics_by_medication_title),
    medicationColumn = stringResource(R.string.statistics_pdf_column_medication),
    missedTitle = stringResource(R.string.statistics_pdf_missed_title),
    missedEmpty = stringResource(R.string.statistics_pdf_missed_empty),
    disclaimer = stringResource(R.string.statistics_pdf_disclaimer),
    footer = stringResource(R.string.statistics_pdf_footer),
    pageFormat = stringResource(R.string.statistics_pdf_page_format),
    unknownProfile = stringResource(R.string.statistics_pdf_unknown_profile),
    percentPattern = stringResource(R.string.statistics_percent_value),
    datePattern = stringResource(R.string.common_date_pattern)
)

@Preview(showBackground = true, name = "Diálogo del informe de adherencia")
@Composable
private fun ProfileReportPeriodDialogPreview() {
    PillRemindTheme {
        ProfileReportPeriodDialog(
            period = StatisticsPeriod.THIRTY,
            onPeriodSelected = {},
            onDismiss = {},
            onConfirm = {}
        )
    }
}
