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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.ui.components.AppDatePickerField
import com.andres.pillremind.ui.components.AppDetailRow
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormActions
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.AppFormSection
import com.andres.pillremind.ui.components.AppNoticeBlock
import com.andres.pillremind.ui.components.AppNavigationRow
import com.andres.pillremind.ui.components.AppPhotoPicker
import com.andres.pillremind.ui.components.AppReadOnlySection
import com.andres.pillremind.ui.components.AppScrollWithFooter
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.screens.main.TopBarAction
import com.andres.pillremind.ui.screens.main.TopBarState
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


/**
 * Modos en los que puede estar la pantalla de medicamento.
 * - `Create`: crear nueva medicación.
 * - `View`: ver detalles.
 * - `Edit`: editar existente.
 * - `Archived`: consultar un medicamento archivado.
 */
sealed class MedicationMode {
    object Create : MedicationMode()
    object View : MedicationMode()
    object Edit : MedicationMode()
    object Archived : MedicationMode()
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
 * @param onUnarchivedClicked Callback llamado tras desarchivar la medicación.
 * @param onDeletedPermanentlyClicked Callback llamado tras eliminar
 * definitivamente la medicación.
 * @param onStatisticsClick Callback ejecutado al abrir las estadísticas del
 * medicamento.
 * @param onHistoryClick Callback ejecutado al abrir el historial del medicamento.
 */
@Composable
fun MedicationScreen(
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit = {},
    medicationMode: MedicationMode = MedicationMode.Create,
    onArchivedClicked: () -> Unit = {},
    onUnarchivedClicked: () -> Unit = {},
    onDeletedPermanentlyClicked: () -> Unit = {},
    onStatisticsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onTopBarChange: (TopBarState) -> Unit = {},
    medicationViewModel: MedicationViewModel = hiltViewModel(),
    medicationScanViewModel: MedicationScanViewModel = hiltViewModel()
) {
    val medication by medicationViewModel.medication.collectAsState()
    val medicationSchedules by medicationViewModel.schedules.collectAsState()
    val nextIntake by medicationViewModel.nextIntake.collectAsStateWithLifecycle()
    val scanState by medicationScanViewModel.state.collectAsStateWithLifecycle()
    var showScanSheet by rememberSaveable { mutableStateOf(false) }

    val unitOptions = stringArrayResource(R.array.medication_units).toList()

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
                MedicationMode.Archived -> "archived"
            }
        },
        restore = { value ->
            when (value) {
                "create" -> MedicationMode.Create
                "view" -> MedicationMode.View
                "edit" -> MedicationMode.Edit
                else -> MedicationMode.Archived
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

    //Variable que si es true muestra el diálogo de borrado definitivo
    var showPermanentDeleteDialog by remember {
        mutableStateOf(false)
    }

    //Si cambia el modo actualiza el título en la topbar de MainScreen
    val topBarTitle = when (currentMode) {
        MedicationMode.Create -> stringResource(R.string.medication_add_title)
        MedicationMode.View -> stringResource(R.string.medication_view_title)
        MedicationMode.Edit -> stringResource(R.string.medication_edit_title)
        MedicationMode.Archived -> stringResource(R.string.medication_archived_title)
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
    val parsedDose = parseDoseInput(dose.value)
    val doseInvalid = dose.value.isNotBlank() && parsedDose == null
    val dateRangeInvalid = !isDateRangeValid(startDate.value, endDate.value)
    val isFormValid = name.value.isNotBlank() &&
            parsedDose != null &&
            unit.value.isNotBlank() &&
            startDate.value != null &&
            !dateRangeInvalid &&
            scheduleForm.isValid() &&
            (isChronicTreatment.value || endDate.value != null)

    val missingFields = mutableListOf<String>()
    if (name.value.isBlank()) missingFields.add(stringResource(R.string.nombre))
    if (dose.value.isBlank()) missingFields.add(stringResource(R.string.dosis))
    if (unit.value.isBlank()) missingFields.add(stringResource(R.string.unidad))
    if (startDate.value == null) missingFields.add(stringResource(R.string.fecha_inicio))
    if (!isChronicTreatment.value && endDate.value == null) {
        missingFields.add(stringResource(R.string.fecha_fin))
    }
    if (!scheduleForm.isValid()) {
        missingFields.add(stringResource(R.string.medication_missing_schedule))
    }

    val missingFieldsMessage = if (!isFormValid && missingFields.isNotEmpty()) {
        stringResource(R.string.save_missing_fields, missingFields.joinToString(", "))
    } else {
        null
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
                ScanCtaCard(
                    onClick = {
                        medicationScanViewModel.reset()
                        showScanSheet = true
                    },
                    modifier = Modifier.padding(bottom = AppSpacing.md)
                )
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
                    if (doseInvalid) {
                        Text(
                            text = stringResource(R.string.medication_dose_invalid),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = AppSpacing.md),
                        )
                    }
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
                                    isError = dateRangeInvalid,
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
                                    isError = dateRangeInvalid,
                                    enabled = !isChronicTreatment.value,
                                    minDate = startDate.value
                                )
                            }
                        }
                    )
                    if (dateRangeInvalid) {
                        Text(
                            text = stringResource(R.string.medication_date_range_invalid),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = AppSpacing.md),
                        )
                    }
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
                        val doseValue = parsedDose
                        val formStartDate = startDate.value
                        if (doseValue != null && formStartDate != null) {
                            medicationViewModel.addMedication(
                                nombre = name.value,
                                photoUri = selectedPhotoUri,
                                dose = doseValue,
                                unit = unit.value,
                                startDate = formStartDate,
                                endDate = endDate.value,
                                scheduleRules = scheduleForm.rules(),
                                times = scheduleForm.times,
                                isChronic = isChronicTreatment.value,
                                onSaved = onSaveClicked
                            )
                        }
                    },
                    primaryEnabled = isFormValid,
                    supportingText = missingFieldsMessage,
                )
            }

            if (showScanSheet) {
                MedicationScanSheet(
                    state = scanState,
                    onDismiss = {
                        showScanSheet = false
                        medicationScanViewModel.reset()
                    },
                    onCnChange = medicationScanViewModel::onCnChange,
                    onCnPhoto = medicationScanViewModel::onCnPhoto,
                    onSearchCn = medicationScanViewModel::searchCn,
                    onBoxPhoto = medicationScanViewModel::onBoxPhoto,
                    onReadBox = medicationScanViewModel::readBox,
                    onShowBox = medicationScanViewModel::showBoxInput,
                    onBackToCn = medicationScanViewModel::backToCn,
                    onGrantConsent = medicationScanViewModel::grantConsent,
                    onDeclineConsent = medicationScanViewModel::declineConsent,
                    onApply = { draft, photo ->
                        name.value = draft.name
                        dose.value = draft.doseText
                        unit.value = draft.unit
                        if (startDate.value == null) startDate.value = LocalDate.now()
                        if (photo != null) selectedPhotoUri = photo
                        showScanSheet = false
                        medicationScanViewModel.reset()
                    }
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
                    if (doseInvalid) {
                        Text(
                            text = stringResource(R.string.medication_dose_invalid),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = AppSpacing.md),
                        )
                    }
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
                                    isError = dateRangeInvalid,
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
                                    isError = dateRangeInvalid,
                                    enabled = !isChronicTreatment.value,
                                    minDate = startDate.value
                                )
                            }
                        }
                    )
                    if (dateRangeInvalid) {
                        Text(
                            text = stringResource(R.string.medication_date_range_invalid),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = AppSpacing.md),
                        )
                    }
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
                        val doseValue = parsedDose
                        val formStartDate = startDate.value
                        if (doseValue != null && formStartDate != null) {
                            medicationViewModel.updateMedication(
                                name = name.value,
                                newPhotoUri = newPhotoUri,
                                currentImagePath = currentImagePath,
                                dose = doseValue,
                                unit = unit.value,
                                startDate = formStartDate,
                                endDate = endDate.value,
                                scheduleRules = scheduleForm.rules(),
                                times = scheduleForm.times,
                                isChronic = isChronicTreatment.value,
                                onSaved = { savedImagePath ->
                                    selectedPhotoUri = savedImagePath?.let(Uri::parse)
                                    currentImagePath = savedImagePath
                                    newPhotoUri = null
                                    currentMode = MedicationMode.View
                                    onSaveClicked()
                                }
                            )
                        }
                    },
                    primaryEnabled = isFormValid,
                    supportingText = missingFieldsMessage,
                    secondaryIcon = Icons.Rounded.Archive,
                    secondaryContentDescription = stringResource(R.string.medication_archive),
                    onSecondaryClick = { showArchiveDialog = true },
                )
            }
        }
        //-------------SEE---------------
        MedicationMode.View -> {
            MedicationViewContent(
                modifier = modifier,
                name = name.value,
                photoUri = selectedPhotoUri,
                dose = dose.value,
                unit = unit.value,
                startDate = startDate.value,
                endDate = endDate.value,
                periodicitySummary = scheduleSummary(scheduleForm),
                times = scheduleForm.times.toList(),
                nextIntake = nextIntake,
                onEditClick = { currentMode = MedicationMode.Edit },
                onArchiveClick = { showArchiveDialog = true },
                onStatisticsClick = onStatisticsClick,
                onHistoryClick = onHistoryClick,
            )
        }
        //-------------ARCHIVED---------------
        MedicationMode.Archived -> {
            MedicationArchivedContent(
                modifier = modifier,
                name = name.value,
                photoUri = selectedPhotoUri,
                dose = dose.value,
                unit = unit.value,
                startDate = startDate.value,
                endDate = endDate.value,
                archivedAt = medication?.deletedAt,
                periodicitySummary = scheduleSummary(scheduleForm),
                times = scheduleForm.times.toList(),
                showSchedule = scheduleInitialized && medicationSchedules.isNotEmpty(),
                onUnarchiveClick = {
                    medication?.let { med ->
                        medicationViewModel.restoreMedication(med) {
                            onUnarchivedClicked()
                        }
                    }
                },
                onDeletePermanentlyClick = { showPermanentDeleteDialog = true },
                onStatisticsClick = onStatisticsClick,
            )
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
    if (showPermanentDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermanentDeleteDialog = false
            },
            title = {
                Text(stringResource(R.string.medication_delete_permanently_title))
            },
            text = {
                Text(stringResource(R.string.medication_delete_permanently_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermanentDeleteDialog = false
                        medication?.let {
                            medicationViewModel.deleteMedicationPermanently(it) {
                                onDeletedPermanentlyClicked()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.medication_delete_permanently))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermanentDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

/**
 * Ficha de solo lectura de un medicamento archivado.
 *
 * Muestra el aviso de archivo, el tratamiento y, si se conserva, la pauta.
 * El pie permite desarchivar el medicamento o eliminarlo definitivamente.
 */
@Composable
private fun MedicationArchivedContent(
    modifier: Modifier = Modifier,
    name: String,
    photoUri: Uri?,
    dose: String,
    unit: String,
    startDate: LocalDate?,
    endDate: LocalDate?,
    archivedAt: LocalDateTime?,
    periodicitySummary: String,
    times: List<LocalTime>,
    showSchedule: Boolean,
    onUnarchiveClick: () -> Unit,
    onDeletePermanentlyClick: () -> Unit,
    onStatisticsClick: () -> Unit,
) {
    val datePattern = stringResource(R.string.common_date_pattern)
    val dateFormatter = remember(datePattern) { DateTimeFormatter.ofPattern(datePattern) }
    val archivedDate = archivedAt?.format(dateFormatter)
    val doseValue = listOf(dose, unit)
        .filter { it.isNotBlank() }
        .joinToString(" ")

    AppScrollWithFooter(
        modifier = modifier,
        footer = {
            AppFormActions(
                primaryText = stringResource(R.string.medication_unarchive),
                onPrimaryClick = onUnarchiveClick,
                secondaryIcon = Icons.Rounded.Delete,
                secondaryContentDescription = stringResource(
                    R.string.medication_delete_permanently
                ),
                secondaryContentColor = MaterialTheme.colorScheme.error,
                onSecondaryClick = onDeletePermanentlyClick,
            )
        },
    ) {
        MedicationHeader(
            name = name,
            photoUri = photoUri,
            subtitle = doseValue,
        )

        if (archivedDate != null) {
            AppNoticeBlock(
                icon = Icons.Rounded.Archive,
                title = stringResource(R.string.medication_archived_title),
                text = stringResource(R.string.medication_archived_notice, archivedDate),
            )
        }

        AppReadOnlySection(title = stringResource(R.string.medication_treatment_section)) {
            AppDetailRow(
                label = stringResource(R.string.dosis),
                value = doseValue,
            )
            AppDetailRow(
                label = stringResource(R.string.fecha_inicio),
                value = startDate?.format(dateFormatter),
            )
            AppDetailRow(
                label = stringResource(R.string.fecha_fin),
                value = endDate?.format(dateFormatter)
                    ?: stringResource(R.string.medication_no_end_date),
                showDivider = false,
            )
        }

        if (showSchedule) {
            AppReadOnlySection(title = stringResource(R.string.medication_schedule_section)) {
                ScheduleReadOnly(
                    summary = periodicitySummary,
                    times = times,
                    nextIntake = null,
                )
            }
        }

        AppReadOnlySection(title = stringResource(R.string.medication_tracking_section)) {
            AppNavigationRow(
                icon = Icons.Rounded.BarChart,
                label = stringResource(R.string.medication_view_statistics),
                description = stringResource(R.string.medication_statistics_description),
                onClick = onStatisticsClick,
            )
        }
    }
}

/**
 * Ficha de solo lectura de un medicamento.
 *
 * Sustituye al formulario deshabilitado: cabecera con foto y dosis, secciones
 * planas sin cards y acciones al final del contenido.
 */
@Composable
private fun MedicationViewContent(
    modifier: Modifier = Modifier,
    name: String,
    photoUri: Uri?,
    dose: String,
    unit: String,
    startDate: LocalDate?,
    endDate: LocalDate?,
    periodicitySummary: String,
    times: List<LocalTime>,
    nextIntake: LocalDateTime?,
    onEditClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    val datePattern = stringResource(R.string.common_date_pattern)
    val dateFormatter = remember(datePattern) { DateTimeFormatter.ofPattern(datePattern) }
    val doseValue = listOf(dose, unit)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    val hasInvalidData = parseDoseInput(dose) == null ||
            !isDateRangeValid(startDate, endDate)

    AppScrollWithFooter(
        modifier = modifier,
        footer = {
            AppFormActions(
                primaryText = stringResource(R.string.editar),
                onPrimaryClick = onEditClick,
                secondaryIcon = Icons.Rounded.Archive,
                secondaryContentDescription = stringResource(R.string.medication_archive),
                onSecondaryClick = onArchiveClick,
            )
        },
    ) {
        MedicationHeader(
            name = name,
            photoUri = photoUri,
            subtitle = doseValue,
        )

        if (hasInvalidData) {
            AppNoticeBlock(
                icon = Icons.Rounded.Warning,
                title = stringResource(R.string.medication_invalid_data_title),
                text = stringResource(R.string.medication_invalid_data_message),
            )
        }

        AppReadOnlySection(title = stringResource(R.string.medication_treatment_section)) {
            AppDetailRow(
                label = stringResource(R.string.dosis),
                value = doseValue,
            )
            AppDetailRow(
                label = stringResource(R.string.fecha_inicio),
                value = startDate?.format(dateFormatter),
            )
            AppDetailRow(
                label = stringResource(R.string.fecha_fin),
                value = endDate?.format(dateFormatter)
                    ?: stringResource(R.string.medication_no_end_date),
                showDivider = false,
            )
        }

        AppReadOnlySection(title = stringResource(R.string.medication_schedule_section)) {
            ScheduleReadOnly(
                summary = periodicitySummary,
                times = times,
                nextIntake = nextIntake,
            )
        }

        AppReadOnlySection(title = stringResource(R.string.medication_tracking_section)) {
            AppNavigationRow(
                icon = Icons.Rounded.BarChart,
                label = stringResource(R.string.medication_view_statistics),
                description = stringResource(R.string.medication_statistics_description),
                onClick = onStatisticsClick,
            )
            AppNavigationRow(
                icon = Icons.Rounded.History,
                label = stringResource(R.string.medication_view_history),
                description = stringResource(R.string.medication_history_description),
                onClick = onHistoryClick,
            )
        }
    }
}

/**
 * Cabecera de la ficha de un medicamento.
 */
@Composable
private fun MedicationHeader(
    name: String,
    photoUri: Uri?,
    subtitle: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppPhotoPicker(
            photoUri = photoUri,
            onPhotoSelected = {},
            enabled = false,
            placeholderIcon = Icons.Default.Medication,
            photoDescriptionRes = R.string.photo_medication,
        )
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Ficha de medicamento")
@Composable
private fun MedicationViewContentPreview() {
    PillRemindTheme {
        MedicationViewContent(
            name = "Enalapril",
            photoUri = null,
            dose = "10",
            unit = "mg",
            startDate = LocalDate.of(2026, 3, 12),
            endDate = null,
            periodicitySummary = "Todos los días",
            times = listOf(LocalTime.of(9, 0), LocalTime.of(21, 0)),
            nextIntake = LocalDateTime.of(2026, 3, 12, 21, 0),
            onEditClick = {},
            onArchiveClick = {},
            onStatisticsClick = {},
            onHistoryClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Ficha de medicamento archivado")
@Composable
private fun MedicationArchivedContentPreview() {
    PillRemindTheme {
        MedicationArchivedContent(
            name = "Enalapril",
            photoUri = null,
            dose = "10",
            unit = "mg",
            startDate = LocalDate.of(2026, 3, 12),
            endDate = null,
            archivedAt = LocalDateTime.of(2026, 11, 2, 18, 30),
            periodicitySummary = "Todos los días",
            times = listOf(LocalTime.of(9, 0), LocalTime.of(21, 0)),
            showSchedule = true,
            onUnarchiveClick = {},
            onDeletePermanentlyClick = {},
            onStatisticsClick = {},
        )
    }
}
