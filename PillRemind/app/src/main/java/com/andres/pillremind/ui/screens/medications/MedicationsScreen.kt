package com.andres.pillremind.ui.screens.medications

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.ui.components.AppEmptyState
import com.andres.pillremind.ui.components.AppSearchField
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.MedicationCard
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Pantalla que muestra la lista de medicamentos del perfil activo.
 *
 * El texto del buscador es transitorio: se limpia al salir de la pantalla
 * (cambio de pestaña o ficha) para no volver a una lista filtrada sin contexto.
 *
 * @param modifier Modificador opcional para la pantalla.
 * @param onMedicationClick Callback que recibe el id del medicamento seleccionado.
 * @param medicationsViewModel ViewModel que gestiona la lógica de medicamentos.
 */
@Composable
fun MedicationsScreen(
    modifier: Modifier = Modifier,
    onMedicationClick: (medicationId: Int) -> Unit,
    medicationsViewModel: MedicationsViewModel = hiltViewModel()
) {

    val uiState by medicationsViewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by medicationsViewModel.searchQuery.collectAsStateWithLifecycle()
    val query = searchQuery.trim()

    // La búsqueda vive en el ViewModel, que Navigation conserva con
    // `saveState`: sin esto, al volver a la pestaña seguiría el filtro anterior.
    val activity = LocalActivity.current
    DisposableEffect(medicationsViewModel) {
        onDispose {
            if (activity?.isChangingConfigurations != true) {
                medicationsViewModel.onSearchQueryChange("")
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        AppSearchField(
            value = searchQuery,
            onValueChange = { medicationsViewModel.onSearchQueryChange(it) },
            placeholder = stringResource(R.string.medication_search_placeholder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppSpacing.md,
                    top = AppSpacing.md,
                    end = AppSpacing.md,
                    bottom = AppSpacing.sm,
                )
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            !uiState.hasMedications && query.isBlank() -> {
                AppEmptyState(
                    icon = ImageVector.vectorResource(R.drawable.ic_medication),
                    title = stringResource(R.string.medication_empty_title),
                    description = stringResource(R.string.medication_empty_body),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md),
                )
            }

            uiState.medications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    BlankCard(stringResource(R.string.medication_search_no_results, query))
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = AppSpacing.md,
                        vertical = AppSpacing.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    items(
                        items = uiState.medications,
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
}
