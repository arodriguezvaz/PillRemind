package com.andres.pillremind.ui.screens.medications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.ui.components.AppSearchField
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.MedicationCard
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Pantalla que muestra la lista de medicamentos del perfil activo.
 *
 * @param modifier Modificador opcional para la pantalla.
 * @param onMedicationClick Callback que recibe el id del medicamento seleccionado.
 * @param onArchivedClick Callback para abrir la lista de medicamentos archivados.
 * @param medicationsViewModel ViewModel que gestiona la lógica de medicamentos.
 */
@Composable
fun MedicationsScreen(
    modifier: Modifier = Modifier,
    onMedicationClick: (medicationId: Int) -> Unit,
    onArchivedClick: () -> Unit = {},
    medicationsViewModel: MedicationsViewModel = hiltViewModel()
) {

    val medications by medicationsViewModel.medications.collectAsStateWithLifecycle()
    val archivedCount by medicationsViewModel.archivedCount.collectAsStateWithLifecycle()
    val searchQuery by medicationsViewModel.searchQuery.collectAsStateWithLifecycle()
    val query = searchQuery.trim()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md)
    ) {
        AppSearchField(
            value = searchQuery,
            onValueChange = { medicationsViewModel.onSearchQueryChange(it) },
            placeholder = stringResource(R.string.medication_search_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.sm)
        )

        if (archivedCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onArchivedClick) {
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Text(
                        text = stringResource(
                            R.string.medication_archived_count,
                            archivedCount,
                        )
                    )
                }
            }
        }

        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BlankCard(
                    if (query.isBlank()) {
                        stringResource(R.string.medication_empty)
                    } else {
                        stringResource(R.string.medication_search_no_results, query)
                    }
                )
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
                        accion = {
                            onMedicationClick(medication.id)
                        }
                    )
                }
            }
        }
    }
}