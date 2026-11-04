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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.parseDose
import com.andres.pillremind.ui.components.AppDatePickerField
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormActions
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.AppFormSection
import com.andres.pillremind.ui.components.AppInfoCard
import com.andres.pillremind.ui.components.AppInfoRow
import com.andres.pillremind.ui.components.AppPhotoPicker
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.screens.main.TopBarAction
import com.andres.pillremind.ui.screens.main.TopBarState
import com.andres.pillremind.ui.theme.AppSpacing
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
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                first(Modifier.fillMaxWidth())
                second(Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
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
    val medication by medicationViewModel.medication.collectAsStateWithLifecycle()
    val medicationSchedules by medicationViewModel.schedules.collectAsStateWithLifecycle()
    val isSaving by medicationViewModel.isSaving.collectAsStateWithLifecycle()
    val saveErrorRes by medicationViewModel.errorRes.collectAsStateWithLifecycle()

    val unitOptions = stringArrayResource(R.array.medication_units).toList()
    val datePattern = stringResource(R.string.common_date_pattern)
    val medicationDateFormatter = remember(datePattern) {
        DateTimeFormatter.ofPattern(datePattern)
    }

    //Variables con estado mutable para los campos del formulario
    val name = rememberSaveable { mutableStateOf("") }
    var selectedPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var currentImagePath by rememberSaveable { mutableStateOf<String?>(null) }
    var newPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val dose = rememberSaveable { mutableStateOf("") }
    val unit = rememberSaveable { mutableStateOf("") }

    val startDate = rememberSaveable { mutableStateOf<LocalDate?>(null) }
    val isChronicTreatment = rememberSaveable { mutableStateOf(false) }
    val endDate = rememberSaveable { mutableStateOf<LocalDate?>(null) }

    val scheduleForm = rememberSaveable(saver = ScheduleFormStateSaver) { ScheduleFormState() }
    var scheduleInitialized by rememberSaveable { mutableStateOf(false) }
    var formInitialized by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(medicationSchedules) {
        if (!scheduleInitialized && medicationSchedules.isNotEmpty()) {
            scheduleForm.loadFrom(medicationSchedules)
            scheduleInitialized = true
        }
    }

    //Si medication no es null actualiza las variables con los valores de medication
    LaunchedEffect(medication) {
        if (!formInitialized && (medication != null || medicationMode == MedicationMode.Create)) {
            medication?.let {
                name.value = it.name
                selectedPhotoUri = it.imageUrl?.let(Uri::parse)
                currentImagePath = it.imageUrl
                dose.value = it.dose.toString()
                unit.value = it.unit
                startDate.value = it.startDate
                endDate.value = it.endDate
                isChronicTreatment.value = it.isChronic
            }
            formInitialized = true
        }
    }
    val modeSaver = Saver<MedicationMode, String>(
        save = { mode ->
            when (mode) {
                MedicationMode.Create -> "create"
                MedicationMode.View -> "view"
                MedicationMode.Edit -> "edit"
            }
        },
        restore = { value ->
            when (value) {
                "create" -> MedicationMode.Create
                "view" -> MedicationMode.View
                else -> MedicationMode.Edit
            }
        }
    )
    //Varibale que especifica el modo en el que está la screen
    var currentMode by rememberSaveable(stateSaver = modeSaver) {
        mutableStateOf(medicationMode)
    }

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
    val backContentDescription = stringResource(R.string.common_back)
    LaunchedEffect(currentMode, topBarTitle) {
        onTopBarChange(
            TopBarState(
                title = topBarTitle,
                navigationAction = if (currentMode == MedicationMode.Edit) {
                    TopBarAction(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backContentDescription,
                        onClick = { currentMode = MedicationMode.View }
                    )
                } else {
                    null
                }
            )
        )
    }
    //Si está en la vista de editar y se pulsa el botón de back se vuelve a la vista de ver
    BackHandler(enabled = currentMode == MedicationMode.Edit) {
        currentMode = MedicationMode.View
    }

    val scrollState = rememberScrollState()
    val doseValue = parseDose(dose.value)
    val dateRangeInvalid = startDate.value != null &&
        endDate.value != null &&
        endDate.value!!.isBefore(startDate.value!!)
    val isFormValid = name.value.isNotBlank() &&
            doseValue != null &&
            unit.value.isNotBlank() &&
            startDate.value != null &&
            !dateRangeInvalid &&
            scheduleForm.isValid() &&
            (isChronicTreatment.value || endDate.value != null)

    val missingFields = mutableListOf<String>()
    if (name.value.isBlank()) missingFields.add(stringResource(R.string.nombre))
    if (doseValue == null) missingFields.add(stringResource(R.string.dosis))
    if (unit.value.isBlank()) missingFields.add(stringResource(R.string.unidad))
    if (startDate.value == null) missingFields.add(stringResource(R.string.fecha_inicio))
    if (!isChronicTreatment.value && endDate.value == null) {
        missingFields.add(stringResource(R.string.fecha_fin))
    }
    if (!scheduleForm.isValid()) {
        missingFields.add(stringResource(R.string.medication_missing_schedule))
    }

    val missingFieldsMessage = when {
        isFormValid -> null
        dateRangeInvalid -> stringResource(R.string.medication_end_before_start)
        else -> stringResource(R.string.save_missing_fields, missingFields.joinToString(", "))
    }

    when (currentMode) {
        //---------CREATE---------
        MedicationMode.Create -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(AppSpacing.md)
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
                    photoDescriptionRes = R.string.photo_medication,
                    modifier = Modifier.padding(bottom = AppSpacing.xl)
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
                                .padding(bottom = AppSpacing.md),
                        )
                    }
                    AdaptiveFieldPair(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppSpacing.md),
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
                            .padding(bottom = AppSpacing.md),
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
                            .padding(bottom = AppSpacing.md),
                        first = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.fecha_inicio),
                                modifier = fieldModifier
                            ) {
                                AppDatePickerField(
                                    value = startDate.value,
                                    onDateSelected = { startDate.value = it },
                                    enabled = true,
                                    maxDate = endDate.value,
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
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                AppFormSection(
                    title = stringResource(R.string.medication_schedule_section),
                    contentSpacing = 0.dp,
                ) {
                    ScheduleEditor(
                        form = scheduleForm,
                        enabled = true
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                AppFormActions(
                    primaryText = stringResource(R.string.guardar),
                    onPrimaryClick = {
                        val validDose = doseValue ?: return@AppFormActions
                        medicationViewModel.addMedication(
                            nombre = name.value,
                            photoUri = selectedPhotoUri,
                            dose = validDose,
                            unit = unit.value,
                            startDate = startDate.value ?: return@AppFormActions,
                            endDate = endDate.value,
                            scheduleRules = scheduleForm.rules(),
                            times = scheduleForm.times,
                            isChronic = isChronicTreatment.value,
                            onSaved = onSaveClicked
                        )
                    },
                    primaryEnabled = isFormValid && !isSaving,
                    primaryLoading = isSaving,
                    supportingText = missingFieldsMessage,
                )
            }
        }
        //----------EDIT-----------
        MedicationMode.Edit -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(AppSpacing.md)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppPhotoPicker(
                    photoUri = newPhotoUri ?: currentImagePath?.let { Uri.fromFile(File(it)) },
                    onPhotoSelected = { newPhotoUri = it },
                    enabled = true,
                    placeholderIcon = Icons.Default.AddAPhoto,
                    photoDescriptionRes = R.string.photo_medication,
                    modifier = Modifier.padding(bottom = AppSpacing.xl)
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
                                .padding(bottom = AppSpacing.md),
                        )
                    }
                    AdaptiveFieldPair(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppSpacing.md),
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
                            .padding(bottom = AppSpacing.md),
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
                            .padding(bottom = AppSpacing.md),
                        first = { fieldModifier ->
                            AppFormField(
                                label = stringResource(R.string.fecha_inicio),
                                modifier = fieldModifier
                            ) {
                                AppDatePickerField(
                                    value = startDate.value,
                                    onDateSelected = { startDate.value = it },
                                    enabled = true,
                                    maxDate = endDate.value,
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
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                AppFormSection(
                    title = stringResource(R.string.medication_schedule_section),
                    contentSpacing = 0.dp,
                ) {
                    ScheduleEditor(
                        form = scheduleForm,
                        enabled = true
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                AppFormActions(
                    primaryText = stringResource(R.string.guardar),
                    onPrimaryClick = {
                        val validDose = doseValue ?: return@AppFormActions
                        medicationViewModel.updateMedication(
                            name = name.value,
                            newPhotoUri = newPhotoUri,
                            currentImagePath = currentImagePath,
                            dose = validDose,
                            unit = unit.value,
                            startDate = startDate.value ?: return@AppFormActions,
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
                    primaryEnabled = isFormValid && !isSaving,
                    primaryLoading = isSaving,
                    supportingText = missingFieldsMessage,
                    secondaryIcon = Icons.Rounded.Archive,
                    secondaryContentDescription = stringResource(R.string.medication_archive),
                    onSecondaryClick = { showArchiveDialog = true },
                )
            }
        }
        //-------------SEE---------------
        MedicationMode.View -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(AppSpacing.md)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppPhotoPicker(
                    photoUri = selectedPhotoUri,
                    onPhotoSelected = { selectedPhotoUri = it },
                    enabled = false,
                    placeholderIcon = Icons.Default.Person,
                    photoDescriptionRes = R.string.photo_medication,
                    modifier = Modifier.padding(bottom = AppSpacing.xl)
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
                        value = startDate.value?.format(medicationDateFormatter),
                    )
                    AppInfoRow(
                        label = stringResource(R.string.fecha_fin),
                        value = endDate.value?.format(medicationDateFormatter),
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.md))
                AppInfoCard(title = stringResource(R.string.medication_schedule_section)) {
                    ScheduleReadOnly(form = scheduleForm)
                }
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                AppFormActions(
                    primaryText = stringResource(R.string.editar),
                    onPrimaryClick = { currentMode = MedicationMode.Edit },
                    secondaryIcon = Icons.Rounded.Archive,
                    secondaryContentDescription = stringResource(R.string.medication_archive),
                    onSecondaryClick = { showArchiveDialog = true },
                )
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

    saveErrorRes?.let { messageRes ->
        AlertDialog(
            onDismissRequest = medicationViewModel::clearError,
            title = { Text(stringResource(R.string.medication_save_error_title)) },
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = medicationViewModel::clearError) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }
}