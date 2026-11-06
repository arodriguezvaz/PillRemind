package com.andres.pillremind.ui.screens.medications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.MedicationCard
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Pantalla que muestra los medicamentos archivados del perfil activo.
 *
 * @param modifier Modificador opcional para la pantalla.
 * @param onMedicationClick Callback que recibe el id del medicamento seleccionado.
 * @param archivedMedicationsViewModel ViewModel que carga los medicamentos archivados.
 */
@Composable
fun ArchivedMedicationsScreen(
    modifier: Modifier = Modifier,
    onMedicationClick: (medicationId: Int) -> Unit,
    archivedMedicationsViewModel: ArchivedMedicationsViewModel = hiltViewModel()
) {
    val archivedMedications by archivedMedicationsViewModel.archivedMedications
        .collectAsStateWithLifecycle()

    ArchivedMedicationsContent(
        modifier = modifier,
        medications = archivedMedications,
        onMedicationClick = onMedicationClick,
    )
}

/**
 * Contenido sin ViewModel de la lista de medicamentos archivados.
 */
@Composable
private fun ArchivedMedicationsContent(
    modifier: Modifier = Modifier,
    medications: List<Medication>,
    onMedicationClick: (medicationId: Int) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md)
    ) {
        if (medications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                BlankCard(stringResource(R.string.medication_archived_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                items(
                    items = medications,
                    key = { it.id }
                ) { medication ->
                    MedicationCard(
                        medication = medication,
                        buttonText = stringResource(R.string.ver),
                        accion = { onMedicationClick(medication.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Medicamentos archivados")
@Composable
private fun ArchivedMedicationsContentPreview() {
    PillRemindTheme {
        ArchivedMedicationsContent(
            medications = listOf(
                Medication(
                    id = 1,
                    profileId = 1,
                    name = "Enalapril",
                    dose = 10.0,
                    unit = "mg",
                    startDate = LocalDate.of(2026, 3, 12),
                    deletedAt = LocalDateTime.of(2026, 11, 2, 18, 30),
                )
            ),
            onMedicationClick = {},
        )
    }
}
