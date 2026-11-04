package com.andres.pillremind.ui.screens.adherence

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.AdherencePeriod
import com.andres.pillremind.domain.model.AdherenceSummary
import com.andres.pillremind.domain.model.MedicationAdherence
import com.andres.pillremind.ui.components.AdherenceCard
import com.andres.pillremind.ui.components.AppInfoCard
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.theme.AppSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val CLOCK_TICK_MILLIS = 60_000L

/**
 * Pantalla con las estadísticas de adherencia del perfil activo.
 *
 * @param modifier Modificador opcional.
 * @param adherenceViewModel ViewModel con los datos de adherencia.
 */
@Composable
fun AdherenceScreen(
    modifier: Modifier = Modifier,
    adherenceViewModel: AdherenceViewModel = hiltViewModel()
) {
    val medications by adherenceViewModel.medications.collectAsStateWithLifecycle()
    val summary by adherenceViewModel.summary.collectAsStateWithLifecycle()
    val period by adherenceViewModel.period.collectAsStateWithLifecycle()

    LaunchedEffect(adherenceViewModel) {
        while (isActive) {
            delay(CLOCK_TICK_MILLIS)
            adherenceViewModel.refreshNow()
        }
    }

    AdherenceContent(
        medications = medications,
        summary = summary,
        period = period,
        onPeriodSelected = adherenceViewModel::selectPeriod,
        modifier = modifier,
    )
}

/**
 * Contenido de la pantalla, separado del ViewModel para poder previsualizarlo.
 */
@Composable
private fun AdherenceContent(
    medications: List<MedicationAdherence>,
    summary: AdherenceSummary,
    period: AdherencePeriod,
    onPeriodSelected: (AdherencePeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            AdherencePeriod.entries.forEach { option ->
                FilterChip(
                    selected = option == period,
                    onClick = { onPeriodSelected(option) },
                    label = { Text(stringResource(option.labelRes())) },
                )
            }
        }

        AppInfoCard(title = stringResource(R.string.adherence_summary_title)) {
            Text(
                text = summary.adherencePercent?.let {
                    stringResource(R.string.adherence_percent_value, it)
                } ?: stringResource(R.string.adherence_no_percent),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (summary.taken + summary.missed + summary.pending == 0) {
                    stringResource(R.string.adherence_summary_no_data)
                } else {
                    stringResource(
                        R.string.adherence_counts,
                        summary.taken,
                        summary.missed,
                        summary.pending,
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (medications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = AppSpacing.md),
                verticalArrangement = Arrangement.Center,
            ) {
                BlankCard(stringResource(R.string.adherence_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                items(medications, key = { it.medicationId }) { medication ->
                    AdherenceCard(adherence = medication)
                }
            }
        }
    }
}

@StringRes
private fun AdherencePeriod.labelRes(): Int = when (this) {
    AdherencePeriod.SEVEN -> R.string.adherence_period_seven
    AdherencePeriod.THIRTY -> R.string.adherence_period_thirty
    AdherencePeriod.NINETY -> R.string.adherence_period_ninety
}

@Preview(showBackground = true)
@Composable
private fun AdherenceContentPreview() {
    val medications = listOf(
        MedicationAdherence(
            medicationId = 1,
            medicationName = "Ibuprofeno",
            dose = 600.0,
            unit = "mg",
            imageUrl = null,
            isArchived = false,
            total = 30,
            taken = 24,
            missed = 3,
        ),
        MedicationAdherence(
            medicationId = 2,
            medicationName = "Omeprazol",
            dose = 20.0,
            unit = "mg",
            imageUrl = null,
            isArchived = true,
            total = 30,
            taken = 30,
            missed = 0,
        ),
    )

    AdherenceContent(
        medications = medications,
        summary = AdherenceSummary(
            taken = 54,
            missed = 3,
            pending = 3,
            adherencePercent = 95,
        ),
        period = AdherencePeriod.THIRTY,
        onPeriodSelected = {},
    )
}
