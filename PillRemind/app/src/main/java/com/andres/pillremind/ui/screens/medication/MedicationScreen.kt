package com.andres.pillremind.ui.screens.medication


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize

/**
 * Clase sellada que representa el modo de medication
 */
sealed class MedicationMode {
    object Create : MedicationMode()
    object View : MedicationMode()
    object Edit : MedicationMode()
}

/**
 * Vista de creación de medicamentos
 * @param viewModel ViewModel para la creación de perfiles
 * @param navController Controlador de navegación
 * @param modifier Modificador para personalizar la vista
 */
@Composable
fun MedicationScreen(
    medicationViewModel: MedicationViewModel,
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit = {},
    medicationMode: MedicationMode = MedicationMode.Create,
) {
    /**
     * Variables con estado mutable para los campos de texto
     */
    val nombre = remember { mutableStateOf("") }
    val urlImagen = remember { mutableStateOf("") }

    val dosis = remember { mutableStateOf("") }
    val unidad = remember { mutableStateOf("") }

    val startDate = remember { mutableStateOf("") }
    val endDate = remember { mutableStateOf("") }
    //val medication = medicationViewModel.medication.collectAsState().value
    when (medicationMode) {
        MedicationMode.Create -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TextField(
                    value = nombre.value,
                    onValueChange = { nombre.value = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = urlImagen.value,
                    onValueChange = { urlImagen.value = it },
                    label = { Text("URL de imagen") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = dosis.value,
                    onValueChange = { dosis.value = it },
                    label = { Text("Dosis") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = unidad.value,
                    onValueChange = { unidad.value = it },
                    label = { Text("Unidad (mg, ml...)") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = startDate.value,
                    onValueChange = { startDate.value = it },
                    label = { Text("Fecha inicio (YYYY-MM-DD)") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = endDate.value,
                    onValueChange = { endDate.value = it },
                    label = { Text("Fecha fin (opcional)") },
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                AppButton(
                    text = "Guardar",
                    onClick = {
                        medicationViewModel.addMedication(
                            nombre = nombre.value,
                            imageUrl = urlImagen.value.ifEmpty { null },
                            dose = dosis.value.toDoubleOrNull() ?: 0.0,
                            unit = unidad.value,
                            startDate = startDate.value,
                            endDate = endDate.value.ifBlank { null }
                        )

                        onSaveClicked()
                    },
                    size = ButtonSize.Large,
                    enabled = nombre.value.isNotBlank()
                )
            }
        }

        MedicationMode.Edit -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //TODO
            }
        }

        MedicationMode.View -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //TODO
            }
        }
    }
}
