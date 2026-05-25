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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.ui.components.BlankCard
import com.andres.pillremind.ui.components.MedicationCard
import com.andres.pillremind.ui.screens.main.TopBarState

/**
 * Pantalla que muestra la lista de medicamentos del perfil activo.
 *
 * @param modifier Modificador opcional para la pantalla.
 * @param onMedicationClick Callback que recibe el id del medicamento seleccionado.
 */
@Composable
fun MedicationsScreen(
    modifier: Modifier = Modifier,
    onMedicationClick: (medicationId: Int) -> Unit,
    onTopBarChange: (TopBarState) -> Unit = {},
    medicationsViewModel: MedicationsViewModel = hiltViewModel()
) {
    
    val medications by medicationsViewModel.medications.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onTopBarChange(TopBarState(title = "Medicamentos"))
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {


        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BlankCard("No hay medicamentos aún")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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