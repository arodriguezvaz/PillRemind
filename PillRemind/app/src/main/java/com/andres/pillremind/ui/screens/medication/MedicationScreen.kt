package com.andres.pillremind.ui.screens.medication


import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.andres.pillremind.R
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.AppDatePickerField
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.AppFormSection
import com.andres.pillremind.ui.components.AppInfoCard
import com.andres.pillremind.ui.components.AppInfoRow
import com.andres.pillremind.ui.components.AppPhotoPicker
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.screens.main.TopBarState
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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
 * Coloca dos campos en fila cuando hay espacio y los apila cuando la fuente
 * es grande o la pantalla es estrecha. Evita que textos como "Selecciona" o
 * las fechas se partan en dos líneas dentro de un campo de una sola línea.
 */
private val medicationDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
private fun AdaptiveFieldPair(
    modifier: Modifier = Modifier,
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
) {
    val fontScale = LocalDensity.current.fontScale

    BoxWithConstraints(modifier = modifier) {
        val stacked = fontScale >= 1.3f || maxWidth < 340.dp

        if (stacked) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                first(Modifier.fillMaxWidth())
                second(Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                first(Modifier.weight(1f))
                second(Modifier.weight(1f))
            }
        }
    }
}

/**
 * Pantalla para crear, ver o editar una medicación.
 *
 * @param modifier Modificador opcional para la pantalla.
 * @param onSaveClicked Callback llamado tras guardar la medicación.
 * @param medicationMode Modo inicial de la pantalla (por defecto `Create`).
 * @param onArchivedClicked Callback llamado tras archivar la medicación.
 */
@Composable
fun MedicationScreen(
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit = {},
    medicationMode: MedicationMode = MedicationMode.Create,
    onArchivedClicked: () -> Unit = {},
    onTopBarChange: (TopBarState) -> Unit = {},
    medicationViewModel: MedicationViewModel = hiltViewModel()
) {
    val medication by medicationViewModel.medication.collectAsState()
    val medicationSchedules by medicationViewModel.schedules.collectAsState()

    //Variables con estado mutable para los campos del formulario
    val name = remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val dose = remember { mutableStateOf("") }
    val unit = remember { mutableStateOf("") }

    val startDate = remember { mutableStateOf<LocalDate?>(null) }
    val isChronicTreatment = remember { mutableStateOf(false) }
    val endDate = remember { mutableStateOf<LocalDate?>(null) }

    val scheduleForm = remember { ScheduleFormState() }
    var scheduleInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(medicationSchedules) {
        if (!scheduleInitialized && medicationSchedules.isNotEmpty()) {
            scheduleForm.loadFrom(medicationSchedules)
            scheduleInitialized = true
        }
    }

    val unitOptions = listOf(
        "mg",
        "g",
        "mcg",
        "kg",

        "ml",
        "l",

        "comprimidos",
        "cápsulas",
        "pastillas",
        "tabletas",

        "gotas",
        "sobres",
        "ampollas",
        "viales",

        "inyecciones",
        "dosis",
        "puffs",
        "sprays",

        "UI",
        "mEq",
        "mmol",

        "parches",
        "supositorios",
        "óvulos",

        "cucharadas",
        "cucharaditas"
    )
    //Si medication no es null actualiza las variables con los valores de medication
    LaunchedEffect(medication) {
        medication?.let {
            name.value = it.name
            selectedPhotoUri = it.imageUrl?.let(Uri::parse)
            dose.value = it.dose.toString()
            unit.value = it.unit
            startDate.value = it.startDate
            endDate.value = it.endDate
            isChronicTreatment.value = it.isChronic
        }
    }
    //Varibale que especifica el modo en el que está la screen
    var currentMode by remember { mutableStateOf(medicationMode) }

    //Variable que si es true muestra el diálogo de eliminación
    var showArchiveDialog by remember {
        mutableStateOf(false)
    }

    //Si cambia el modo actualiza el título en la topbar de MainScreen
    val topBarTitle = when (currentMode) {
        MedicationMode.Create -> stringResource(R.string.medication_add_title)
        MedicationMode.View -> stringResource(R.string.medication_view_title)
        MedicationMode.Edit -> stringResource(R.string.medication_edit_title)
    }
    LaunchedEffect(currentMode, topBarTitle) {
        onTopBarChange(TopBarState(title = topBarTitle))
    }
    //Si está en la vista de editar y se pulsa el botón de back se vuelve a la vista de ver
    BackHandler(enabled = currentMode == MedicationMode.Edit) {
        currentMode = MedicationMode.View
    }

    val scrollState = rememberScrollState()
    val isFormValid = name.value.isNotBlank() &&
            dose.value.isNotBlank() &&
            unit.value.isNotBlank() &&
            startDate.value != null &&
            scheduleForm.isValid() &&
            (isChronicTreatment.value || endDate.value != null)
    when (currentMode) {
        //---------CREATE---------
        MedicationMode.Create -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //-----IMAGEN
                AppPhotoPicker(
                    photoUri = selectedPhotoUri,
                    onPhotoSelected = { selectedPhotoUri = it },
                    enabled = true,
                    placeholderIcon = Icons.Default.AddAPhoto,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                //-----------NOMBRE
                AppFormSection(
                    title = stringResource(R.string.medication_data_section),
                    contentSpacing = 0.dp,
                ) {
                    AppFormField(label = stringResource(R.string.nombre)) {
                        AppTextField(
                            value = name.value,
                            onValueChange = { name.value = it },
                            placeholder = stringResource(R.string.medication_name_placeholder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        )
                    }
                    AdaptiveFieldPair(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        first = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.dosis),
                                modifier = fieldModifier
                            ) {
                                AppTextField(
                                    value = dose.value,
                                    onValueChange = { dose.value = it },
                                    placeholder = stringResource(R.string.medication_dose_placeholder),
                                    keyboardType = KeyboardType.Decimal
                                )
                            }
                        },
                        second = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.unidad),
                                modifier = fieldModifier
                            ) {
                                AppDropdownField(
                                    value = unit.value,
                                    options = unitOptions,
                                    onOptionSelected = { unit.value = it },
                                    placeholder = stringResource(R.string.medication_select_placeholder)
                                )
                            }
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChronicTreatment.value,
                            onCheckedChange = {
                                isChronicTreatment.value = it
                                if (it) endDate.value = null
                            },
                            enabled = true
                        )
                        Text(
                            text = stringResource(R.string.medication_chronic_treatment),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    AdaptiveFieldPair(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        first = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.fecha_inicio),
                                modifier = fieldModifier
                            ) {
                                AppDatePickerField(
                                    value = startDate.value,
                                    onDateSelected = { startDate.value = it },
                                    enabled = true,
                                )
                            }
                        },
                        second = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.fecha_fin),
                                modifier = fieldModifier
                            ) {
                                AppDatePickerField(
                                    value = endDate.value,
                                    onDateSelected = { endDate.value = it },
                                    enabled = !isChronicTreatment.value,
                                    minDate = startDate.value
                                )
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                AppFormSection(
                    title = stringResource(R.string.medication_schedule_section),
                    contentSpacing = 0.dp,
                ) {
                    ScheduleEditor(
                        form = scheduleForm,
                        enabled = true
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                AppButton(
                    text = stringResource(R.string.guardar),
                    onClick = {
                        medicationViewModel.addMedication(
                            nombre = name.value,
                            photoUri = selectedPhotoUri,
                            dose = dose.value.toDoubleOrNull() ?: 0.0,
                            unit = unit.value,
                            startDate = startDate.value!!,
                            endDate = endDate.value,
                            scheduleRules = scheduleForm.rules(),
                            times = scheduleForm.times,
                            isChronic = isChronicTreatment.value,
                            onSaved = onSaveClicked
                        )
                    },
                    size = ButtonSize.Large,
                    enabled = isFormValid
                )
            }
        }
        //----------EDIT-----------
        MedicationMode.Edit -> {
            var currentImagePath by remember { mutableStateOf(medication?.imageUrl) }
            var newPhotoUri by remember { mutableStateOf<Uri?>(null) }
            LaunchedEffect(medication) {
                currentImagePath = medication?.imageUrl
            }
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppPhotoPicker(
                    photoUri = newPhotoUri ?: currentImagePath?.let { Uri.fromFile(File(it)) },
                    onPhotoSelected = { newPhotoUri = it },
                    enabled = true,
                    placeholderIcon = Icons.Default.AddAPhoto,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                AppFormSection(
                    title = stringResource(R.string.medication_data_section),
                    contentSpacing = 0.dp,
                ) {
                    AppFormField(label = stringResource(R.string.nombre)) {
                        AppTextField(
                            value = name.value,
                            onValueChange = { name.value = it },
                            placeholder = stringResource(R.string.medication_name_placeholder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        )
                    }
                    AdaptiveFieldPair(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        first = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.dosis),
                                modifier = fieldModifier
                            ) {
                                AppTextField(
                                    value = dose.value,
                                    onValueChange = { dose.value = it },
                                    placeholder = stringResource(R.string.medication_dose_placeholder),
                                    keyboardType = KeyboardType.Decimal
                                )
                            }
                        },
                        second = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.unidad),
                                modifier = fieldModifier
                            ) {
                                AppDropdownField(
                                    value = unit.value,
                                    options = unitOptions,
                                    onOptionSelected = { unit.value = it },
                                    placeholder = stringResource(R.string.medication_select_placeholder)
                                )
                            }
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChronicTreatment.value,
                            onCheckedChange = {
                                isChronicTreatment.value = it
                                if (it) endDate.value = null
                            },
                            enabled = true
                        )
                        Text(
                            text = stringResource(R.string.medication_chronic_treatment),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    AdaptiveFieldPair(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        first = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.fecha_inicio),
                                modifier = fieldModifier
                            ) {
                                AppDatePickerField(
                                    value = startDate.value,
                                    onDateSelected = { startDate.value = it },
                                    enabled = true
                                )
                            }
                        },
                        second = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.fecha_fin),
                                modifier = fieldModifier
                            ) {
                                AppDatePickerField(
                                    value = endDate.value,
                                    onDateSelected = { endDate.value = it },
                                    enabled = !isChronicTreatment.value,
                                    minDate = startDate.value
                                )
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                AppFormSection(
                    title = stringResource(R.string.medication_schedule_section),
                    contentSpacing = 0.dp,
                ) {
                    ScheduleEditor(
                        form = scheduleForm,
                        enabled = true
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier.fillMaxWidth()) {
                    //ARCHIVAR
                    AppButton(
                        onClick = {
                            showArchiveDialog = true
                        },
                        text = stringResource(R.string.medication_archive),
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
                                newPhotoUri = newPhotoUri,
                                currentImagePath = currentImagePath,
                                dose = dose.value.toDoubleOrNull() ?: 0.0,
                                unit = unit.value,
                                startDate = startDate.value!!,
                                endDate = endDate.value,
                                scheduleRules = scheduleForm.rules(),
                                times = scheduleForm.times,
                                isChronic = isChronicTreatment.value,
                                onSaved = {
                                    currentMode = MedicationMode.View
                                    onSaveClicked()
                                }
                            )
                        },
                        size = ButtonSize.Medium,
                        modifier = Modifier.weight(1f),
                        enabled = isFormValid
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
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppPhotoPicker(
                    photoUri = selectedPhotoUri,
                    onPhotoSelected = { selectedPhotoUri = it },
                    enabled = false,
                    placeholderIcon = Icons.Default.Person,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                AppInfoCard(title = stringResource(R.string.medication_data_section)) {
                    AppInfoRow(
                        label = stringResource(R.string.nombre),
                        value = name.value,
                    )
                    AppInfoRow(
                        label = stringResource(R.string.dosis),
                        value = listOf(dose.value, unit.value)
                            .filter { it.isNotBlank() }
                            .joinToString(" "),
                    )
                    AppInfoRow(
                        label = stringResource(R.string.medication_chronic_treatment),
                        value = stringResource(
                            if (isChronicTreatment.value) R.string.common_yes else R.string.common_no
                        ),
                    )
                    AppInfoRow(
                        label = stringResource(R.string.fecha_inicio),
                        value = startDate.value
                            ?.format(medicationDateFormatter)
                            ?: stringResource(R.string.common_not_specified),
                    )
                    AppInfoRow(
                        label = stringResource(R.string.fecha_fin),
                        value = endDate.value
                            ?.format(medicationDateFormatter)
                            ?: stringResource(R.string.common_not_specified),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                AppInfoCard(title = stringResource(R.string.medication_schedule_section)) {
                    ScheduleReadOnly(form = scheduleForm)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier.fillMaxWidth()) {
                    //ARCHIVAR
                    AppButton(
                        onClick = {
                            showArchiveDialog = true
                        },
                        text = stringResource(R.string.medication_archive),
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
        }
    }
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = {
                showArchiveDialog = false
            },
            title = {
                Text(stringResource(R.string.medication_archive_title))
            },
            text = {
                Text(stringResource(R.string.medication_archive_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchiveDialog = false
                        medication?.let {
                            medicationViewModel.archiveMedication(it) {
                                onArchivedClicked()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.medication_archive))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showArchiveDialog = false
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}