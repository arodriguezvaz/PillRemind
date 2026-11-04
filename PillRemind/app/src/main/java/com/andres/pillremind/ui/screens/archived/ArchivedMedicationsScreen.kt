package com.andres.pillremind.ui.screens.archived

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.ui.components.ArchivedMedicationCard
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.theme.AppSpacing
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Pantalla con los medicamentos archivados del perfil activo.
 *
 * @param modifier Modificador opcional.
 * @param archivedViewModel ViewModel de medicamentos archivados.
 */
@Composable
fun ArchivedMedicationsScreen(
    modifier: Modifier = Modifier,
    archivedViewModel: ArchivedMedicationsViewModel = hiltViewModel()
) {
    val medications by archivedViewModel.archivedMedications.collectAsStateWithLifecycle()
    val isSaving by archivedViewModel.isSaving.collectAsStateWithLifecycle()
    val errorRes by archivedViewModel.errorRes.collectAsStateWithLifecycle()

    var restoreTarget by remember { mutableStateOf<Medication?>(null) }
    var deleteTarget by remember { mutableStateOf<Medication?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.archived_info),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = AppSpacing.sm),
        )

        ArchivedMedicationsContent(
            medications = medications,
            enabled = !isSaving,
            onRestore = { restoreTarget = it },
            onDelete = { deleteTarget = it },
            modifier = Modifier.weight(1f),
        )
    }

    restoreTarget?.let { medication ->
        AlertDialog(
            onDismissRequest = { restoreTarget = null },
            title = { Text(stringResource(R.string.archived_restore_title)) },
            text = { Text(stringResource(R.string.archived_restore_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        restoreTarget = null
                        archivedViewModel.restoreMedication(medication)
                    }
                ) {
                    Text(stringResource(R.string.archived_restore))
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreTarget = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    deleteTarget?.let { medication ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.archived_delete_title)) },
            text = { Text(stringResource(R.string.archived_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        archivedViewModel.deletePermanently(medication)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.archived_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    errorRes?.let { messageRes ->
        AlertDialog(
            onDismissRequest = archivedViewModel::clearError,
            title = { Text(stringResource(R.string.archived_error_title)) },
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = archivedViewModel::clearError) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }
}

@Composable
private fun ArchivedMedicationsContent(
    medications: List<Medication>,
    enabled: Boolean,
    onRestore: (Medication) -> Unit,
    onDelete: (Medication) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (medications.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            BlankCard(stringResource(R.string.archived_empty))
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items(medications, key = { it.id }) { medication ->
                ArchivedMedicationCard(
                    medication = medication,
                    onRestore = { onRestore(medication) },
                    onDelete = { onDelete(medication) },
                    enabled = enabled,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchivedMedicationsContentPreview() {
    ArchivedMedicationsContent(
        medications = listOf(
            Medication(
                id = 1,
                profileId = 1,
                name = "Ibuprofeno",
                dose = 600.0,
                unit = "mg",
                startDate = LocalDate.of(2026, 1, 1),
                deletedAt = LocalDateTime.of(2026, 10, 1, 12, 0),
            )
        ),
        enabled = true,
        onRestore = {},
        onDelete = {},
    )
}
