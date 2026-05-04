package com.andres.pillremind.ui.screens.medication


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.FormField
import com.andres.pillremind.ui.components.WeekDayChipGroup
import com.andres.pillremind.ui.components.timeeditor.TimeListEditor
import java.time.LocalDate
import java.time.LocalTime


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
    onEditClicked: () -> Unit = {},
    medicationId: Int? = null
) {
    val medicationViewModel: MedicationViewModel = hiltViewModel()
    val medication by medicationViewModel.medication.collectAsState()

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
        //---------CREATE---------
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
                        enabled = true,
                        onAddClick = {
                            times.add(LocalTime.now()) // aquí luego meterás TimePicker
                        },
                        onRemoveTime = { time ->
                            times.remove(time)
                        }
                    )
                }
                AppButton(
                    text = stringResource(R.string.guardar),
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
                    medicationViewModel.clearMessage()
                }
            }
        }
        //----------EDIT-----------
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
        //-------------SEE---------------
        MedicationMode.View -> {

            val currentMedication = medication

            val medicationWeekDays by medicationViewModel.weekDays.collectAsState()
            val medicationTimes by medicationViewModel.times.collectAsState()

            if (currentMedication == null) {
                // Muestra loading mientras llega el dato
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
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
                            value = currentMedication.name,
                            onValueChange = { nombre.value = it },
                            modifier = Modifier.padding(bottom = 16.dp),
                            enabled = false
                        )
                    }
                    FormField(label = stringResource(R.string.url_imagen)) {
                        AppTextField(
                            value = currentMedication.imageUrl.toString(),
                            onValueChange = { urlImagen.value = it },
                            placeholder = "hhtps",
                            modifier = Modifier.padding(bottom = 16.dp),
                            enabled = false
                        )
                    }

                    FormField(label = stringResource(R.string.dosis)) {
                        AppTextField(
                            value = currentMedication.dose.toString(),
                            onValueChange = { dosis.value = it },
                            placeholder = "Escribe algo...",
                            modifier = Modifier.padding(bottom = 16.dp),
                            enabled = false
                        )
                    }
                    FormField(label = stringResource(R.string.unidad)) {
                        AppTextField(
                            value = currentMedication.toString(),
                            onValueChange = { unidad.value = it },
                            placeholder = "Escribe algo...",
                            modifier = Modifier.padding(bottom = 16.dp),
                            enabled = false
                        )
                    }
                    FormField(label = stringResource(R.string.fecha_inicio)) {
                        AppDatePickerField(
                            value = currentMedication.startDate,
                            onDateSelected = { startDate.value = it },
                            modifier = Modifier.padding(bottom = 16.dp),
                            enabled = false
                        )
                    }
                    FormField(label = stringResource(R.string.fecha_fin)) {
                        AppDatePickerField(
                            value = currentMedication.endDate,
                            onDateSelected = { endDate.value = it },
                            modifier = Modifier.padding(bottom = 16.dp),
                            enabled = false
                        )
                    }
                    //TODO
                    FormField(stringResource(R.string.periocidad)) {
                        WeekDayChipGroup(
                            selectedDays = medicationWeekDays,
                            onSelectionChange = { weekDays.value = it },
                            enabled = false
                        )
                    }
                    FormField(stringResource(R.string.horario)) {
                        TimeListEditor(
                            times = medicationTimes,
                            enabled = false,
                            onAddClick = {
                                times.add(LocalTime.now())
                            },
                            onRemoveTime = { time ->
                                times.remove(time)
                            }
                        )
                    }
                    Row(modifier.fillMaxWidth()) {
                        AppButton(
                            onClick = {},
                            text = stringResource(R.string.eliminar),
                            size = ButtonSize.Medium,
                            variant = ButtonVariant.Danger,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        AppButton(
                            onClick = {},
                            text = stringResource(R.string.editar),
                            size = ButtonSize.Medium,
                            variant = ButtonVariant.Primary,
                            modifier = Modifier.weight(1f)

                        )
                    }
                }
            }
            LaunchedEffect(snackbarMessage) {
                snackbarMessage?.let {
                    medicationViewModel.clearMessage()
                }
            }
        }
    }
}
