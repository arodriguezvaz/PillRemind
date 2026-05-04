package com.andres.pillremind.ui.screens.medication


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.WeekDay
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.AppDatePickerField
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.FormField
import com.andres.pillremind.ui.components.WeekDayChipGroup
import com.andres.pillremind.ui.components.timeeditor.TimeListEditor
import java.time.LocalDate
import java.time.LocalTime
import com.andres.pillremind.ui.components.timeeditor.TimeListMode

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
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit = {},
    medicationMode: MedicationMode = MedicationMode.Create,
) {
    val medicationViewModel: MedicationViewModel = hiltViewModel()

    /**
     * Variables con estado mutable para los campos de texto
     */
    val nombre = remember { mutableStateOf("") }
    val urlImagen = remember { mutableStateOf("") }

    val dosis = remember { mutableStateOf("") }
    val unidad = remember { mutableStateOf("") }

    val startDate = remember { mutableStateOf<LocalDate?>(null) }
    val endDate = remember { mutableStateOf<LocalDate?>(null) }

    val weekDays = remember { mutableStateOf(setOf<WeekDay>()) }

    val times = remember { mutableStateListOf<LocalTime>() }

    val snackbarMessage by medicationViewModel.snackbarMessage.collectAsState()
    val scrollState = rememberScrollState()
    when (medicationMode) {
        MedicationMode.Create -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FormField(label = stringResource(R.string.nombre)) {
                    AppTextField(
                        value = nombre.value,
                        onValueChange = { nombre.value = it },
                        placeholder = "Nombre del medicamento",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                FormField(label = stringResource(R.string.url_imagen)) {
                    AppTextField(
                        value = urlImagen.value,
                        onValueChange = { urlImagen.value = it },
                        placeholder = "hhtps",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                FormField(label = stringResource(R.string.dosis)) {
                    AppTextField(
                        value = dosis.value,
                        onValueChange = { dosis.value = it },
                        placeholder = "Escribe algo...",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                FormField(label = stringResource(R.string.unidad)) {
                    AppTextField(
                        value = unidad.value,
                        onValueChange = { unidad.value = it },
                        placeholder = "Escribe algo...",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                FormField(label = stringResource(R.string.fecha_inicio)) {
                    AppDatePickerField(
                        value = startDate.value,
                        onDateSelected = { startDate.value = it },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                FormField(label = stringResource(R.string.fecha_fin)) {
                    AppDatePickerField(
                        value = endDate.value,
                        onDateSelected = { endDate.value = it },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                FormField(stringResource(R.string.periocidad)) {
                    WeekDayChipGroup(
                        selectedDays = weekDays.value,
                        onSelectionChange = { weekDays.value = it })
                }
                FormField(stringResource(R.string.horario)) {
                    TimeListEditor(
                        times = times,
                        mode = TimeListMode.EDIT,
                        onAddClick = {
                            times.add(LocalTime.now()) // aquí luego meterás TimePicker
                        },
                        onRemoveTime = { time ->
                            times.remove(time)
                        }
                    )
                }
                AppButton(
                    text = "Guardar",
                    onClick = {
                        medicationViewModel.addMedication(
                            nombre = nombre.value,
                            imageUrl = urlImagen.value.ifEmpty { null },
                            dose = dosis.value.toDoubleOrNull() ?: 0.0,
                            unit = unidad.value,
                            startDate = startDate.value!!,
                            endDate = endDate.value,
                            weekDays = weekDays.value,
                            times = times
                        )

                        onSaveClicked()
                    },
                    size = ButtonSize.Large,
                    enabled = nombre.value.isNotBlank()
                )
            }
            LaunchedEffect(snackbarMessage) {
                snackbarMessage?.let {
                    // aquí muestras snackbar
                    medicationViewModel.clearMessage()
                }
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
