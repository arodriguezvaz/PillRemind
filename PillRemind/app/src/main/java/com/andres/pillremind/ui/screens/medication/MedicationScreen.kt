package com.andres.pillremind.ui.screens.medication


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.andres.pillremind.ui.screens.main.TopBarState
import java.time.LocalDate
import java.time.LocalTime


/**
 * Modos en los que puede estar la pantalla de medicamento.
 * - `Create`: crear nueva medicación.
 * - `View`: ver detalles.
 * - `Edit`: editar existente.
 */
sealed class MedicationMode {
    object Create : MedicationMode()
    object View : MedicationMode()
    object Edit : MedicationMode()
}

/**
 * Pantalla para crear, ver o editar una medicación.
 *
 * @param modifier Modificador opcional para la pantalla.
 * @param onSaveClicked Callback llamado tras guardar la medicación.
 * @param medicationMode Modo inicial de la pantalla (por defecto `Create`).
 * @param onDeleteClicked Callback llamado tras eliminar la medicación.
 */
@Composable
fun MedicationScreen(
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit = {},
    medicationMode: MedicationMode = MedicationMode.Create,
    onDeleteClicked: () -> Unit = {},
    onTopBarChange: (TopBarState) -> Unit = {}
) {
    val medicationViewModel: MedicationViewModel = hiltViewModel()
    val medication by medicationViewModel.medication.collectAsState()
    val medicationWeekDays by medicationViewModel.weekDays.collectAsState()
    val medicationTimes by medicationViewModel.times.collectAsState()

    //Variables con estado mutable para los campos del formulario
    val name = remember { mutableStateOf("") }
    val urlImagen = remember { mutableStateOf("") }

    val dose = remember { mutableStateOf("") }
    val unit = remember { mutableStateOf("") }

    val startDate = remember { mutableStateOf<LocalDate?>(null) }
    val endDate = remember { mutableStateOf<LocalDate?>(null) }

    val weekDays = remember { mutableStateOf(setOf<WeekDay>()) }

    val times = remember { mutableStateListOf<LocalTime>() }


    //Si medication no es null actualiza las variables con los valores de medication
    LaunchedEffect(medication) {
        medication?.let {
            name.value = it.name
            urlImagen.value = it.imageUrl ?: ""
            dose.value = it.dose.toString()
            unit.value = it.unit
            startDate.value = it.startDate
            endDate.value = it.endDate
        }
    }
    LaunchedEffect(medicationWeekDays) {
        if (weekDays.value.isEmpty()) {
            weekDays.value = medicationWeekDays
        }
    }

    LaunchedEffect(medicationTimes) {
        if (times.isEmpty()) {
            times.addAll(medicationTimes)

        }
    }

    //Varibale que especifica el modo en el que está la screen
    var currentMode by remember { mutableStateOf(medicationMode) }

    //Variable que si es true muestra el diálogo de eliminación
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    //Si cambia el modo actualiza el título en la topbar de MainScreen
    LaunchedEffect(currentMode) {
        onTopBarChange(
            TopBarState(
                title = when (currentMode) {
                    MedicationMode.Create -> "Añadir medicamento"
                    MedicationMode.View -> "Ver medicamento"
                    MedicationMode.Edit -> "Editar medicamento"
                }
            )
        )
    }
    //Si está en la vista de editar y se pulsa el botón de back se vuelve a la vista de ver
    BackHandler(enabled = currentMode == MedicationMode.Edit) {
        currentMode = MedicationMode.View
    }
    //TODO
    val snackbarMessage by medicationViewModel.snackbarMessage.collectAsState()
    val scrollState = rememberScrollState()
    when (currentMode) {
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
                        value = name.value,
                        onValueChange = { name.value = it },
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
                        value = dose.value,
                        onValueChange = { dose.value = it },
                        placeholder = "Escribe algo...",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                FormField(label = stringResource(R.string.unidad)) {
                    AppTextField(
                        value = unit.value,
                        onValueChange = { unit.value = it },
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
                        onAddClick = { time ->
                            times.add(time)
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
                            nombre = name.value,
                            imageUrl = urlImagen.value.ifEmpty { null },
                            dose = dose.value.toDoubleOrNull() ?: 0.0,
                            unit = unit.value,
                            startDate = startDate.value!!,
                            endDate = endDate.value,
                            weekDays = weekDays.value,
                            times = times
                        )

                        onSaveClicked()
                    },
                    size = ButtonSize.Large,
                    enabled = name.value.isNotBlank()
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
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FormField(label = stringResource(R.string.nombre)) {
                    AppTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                FormField(label = stringResource(R.string.url_imagen)) {
                    AppTextField(
                        value = urlImagen.value,
                        onValueChange = { urlImagen.value = it },
                        placeholder = "hhtps",
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }

                FormField(label = stringResource(R.string.dosis)) {
                    AppTextField(
                        value = dose.value,
                        onValueChange = { dose.value = it },
                        placeholder = "Escribe algo...",
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                FormField(label = stringResource(R.string.unidad)) {
                    AppTextField(
                        value = unit.value,
                        onValueChange = { unit.value = it },
                        placeholder = "Escribe algo...",
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                FormField(label = stringResource(R.string.fecha_inicio)) {
                    AppDatePickerField(
                        value = startDate.value,
                        onDateSelected = { startDate.value = it },
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                FormField(label = stringResource(R.string.fecha_fin)) {
                    AppDatePickerField(
                        value = endDate.value,
                        onDateSelected = { endDate.value = it },
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }

                FormField(stringResource(R.string.periocidad)) {
                    WeekDayChipGroup(
                        selectedDays = weekDays.value,
                        onSelectionChange = { weekDays.value = it },
                    )
                }
                FormField(stringResource(R.string.horario)) {
                    TimeListEditor(
                        times = times,
                        onAddClick = { time ->
                            times.add(time)
                        },
                        onRemoveTime = { time ->
                            times.remove(time)
                        }
                    )
                }
                Row(modifier.fillMaxWidth()) {
                    //ELIMINAR
                    AppButton(
                        onClick = {
                            showDeleteDialog = true
                        },
                        text = stringResource(R.string.eliminar),
                        size = ButtonSize.Medium,
                        variant = ButtonVariant.Danger,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    //GUARDAR -- ACTUALIZAR
                    AppButton(
                        text = stringResource(R.string.guardar),
                        onClick = {
                            medicationViewModel.updateMedication(
                                name = name.value,
                                imageUrl = urlImagen.value.ifEmpty { null },
                                dose = dose.value.toDoubleOrNull() ?: 0.0,
                                unit = unit.value,
                                startDate = startDate.value!!,
                                endDate = endDate.value,
                                weekDays = weekDays.value,
                                times = times
                            )

                            currentMode = MedicationMode.View
                        },
                        size = ButtonSize.Medium,
                        modifier = Modifier.weight(1f),
                        enabled = name.value.isNotBlank()
                    )
                }
            }
        }
        //-------------SEE---------------
        MedicationMode.View -> {


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
                        value = name.value,
                        onValueChange = { name.value = it },
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = false
                    )
                }
                FormField(label = stringResource(R.string.url_imagen)) {
                    AppTextField(
                        value = urlImagen.value,
                        onValueChange = { urlImagen.value = it },
                        placeholder = "hhtps",
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = false
                    )
                }

                FormField(label = stringResource(R.string.dosis)) {
                    AppTextField(
                        value = dose.value,
                        onValueChange = { dose.value = it },
                        placeholder = "Escribe algo...",
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = false
                    )
                }
                FormField(label = stringResource(R.string.unidad)) {
                    AppTextField(
                        value = unit.value,
                        onValueChange = { unit.value = it },
                        placeholder = "Escribe algo...",
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = false
                    )
                }
                FormField(label = stringResource(R.string.fecha_inicio)) {
                    AppDatePickerField(
                        value = startDate.value,
                        onDateSelected = { startDate.value = it },
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = false
                    )
                }
                FormField(label = stringResource(R.string.fecha_fin)) {
                    AppDatePickerField(
                        value = endDate.value,
                        onDateSelected = { endDate.value = it },
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = false
                    )
                }
                //TODO
                FormField(stringResource(R.string.periocidad)) {
                    WeekDayChipGroup(
                        selectedDays = weekDays.value,
                        onSelectionChange = { weekDays.value = it },
                        enabled = false
                    )
                }
                FormField(stringResource(R.string.horario)) {
                    TimeListEditor(
                        times = times,
                        enabled = false,
                        onAddClick = { time ->
                            times.add(time)
                        },
                        onRemoveTime = { time ->
                            times.remove(time)
                        }
                    )
                }
                Row(modifier.fillMaxWidth()) {
                    //ELIMINAR
                    AppButton(
                        onClick = {
                            showDeleteDialog = true
                        },
                        text = stringResource(R.string.eliminar),
                        size = ButtonSize.Medium,
                        variant = ButtonVariant.Danger,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    //EDITAR
                    AppButton(
                        onClick = { currentMode = MedicationMode.Edit },
                        text = stringResource(R.string.editar),
                        size = ButtonSize.Medium,
                        variant = ButtonVariant.Primary,
                        modifier = Modifier.weight(1f)

                    )
                }
            }

            LaunchedEffect(snackbarMessage) {
                snackbarMessage?.let {
                    medicationViewModel.clearMessage()
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Eliminar medicamento")
            },
            text = {
                Text("¿Seguro que quieres eliminar este medicamento?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        medicationViewModel.deleteMedication(medication!!)
                        showDeleteDialog = false
                        onDeleteClicked()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}