package com.andres.pillremind.ui.screens.importer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.MedicationImportStatus
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.ScheduleRule
import com.andres.pillremind.domain.model.MedicationImportWarning
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.AppDatePickerField
import com.andres.pillremind.ui.components.AppDetailRow
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormActions
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.AppFormSection
import com.andres.pillremind.ui.components.AppNoticeBlock
import com.andres.pillremind.ui.components.AppScrollWithFooter
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.settings.SettingsToggleItem
import com.andres.pillremind.ui.screens.medication.ScheduleEditor
import com.andres.pillremind.ui.screens.medication.ScheduleFormState
import com.andres.pillremind.ui.screens.medication.ScheduleFormStateSaver
import com.andres.pillremind.ui.screens.medication.scheduleSummary
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla de importación de medicamentos desde una hoja de medicación en PDF.
 *
 * @param onSaved Callback ejecutado al cerrar la pantalla después de guardar.
 * @param onOpenMedication Abre la ficha de un medicamento existente.
 * @param viewModel ViewModel de la importación.
 */
@Composable
fun MedicationImportScreen(
    onSaved: () -> Unit,
    onOpenMedication: (id: Int, archived: Boolean) -> Unit,
    viewModel: MedicationImportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editingKey by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingForceKey by rememberSaveable { mutableStateOf<String?>(null) }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.onPdfSelected(uri)
    }

    when (state.stage) {
        MedicationImportStage.PICK_FILE -> ImportPickFileContent(
            errorRes = state.errorRes,
            onPickFile = { pdfLauncher.launch(arrayOf(PDF_MIME_TYPE)) }
        )

        MedicationImportStage.READING -> ImportReadingContent()

        MedicationImportStage.REVIEW -> ImportReviewContent(
            state = state,
            onStartDateChange = viewModel::onStartDateChange,
            onToggleItem = viewModel::onToggleItem,
            onEditItem = { editingKey = it.key },
            onDuplicateCheck = { pendingForceKey = it.key },
            onOpenMedication = onOpenMedication,
            onSave = viewModel::save
        )

        MedicationImportStage.SAVED -> ImportSavedContent(
            count = state.savedCount,
            onClose = onSaved
        )
    }

    val editingItem = state.items.firstOrNull { it.key == editingKey }
    if (editingItem != null) {
        MedicationImportEditSheet(
            item = editingItem,
            onDismiss = { editingKey = null },
            onApply = { name, dose, unit, scheduleRules, times, chronic ->
                viewModel.onEditItem(
                    key = editingItem.key,
                    name = name,
                    doseText = dose,
                    unit = unit,
                    scheduleRules = scheduleRules,
                    times = times,
                    isChronic = chronic
                )
                editingKey = null
            }
        )
    }

    val pendingForce = state.items.firstOrNull { it.key == pendingForceKey }
    if (pendingForce != null) {
        AlertDialog(
            onDismissRequest = { pendingForceKey = null },
            title = { Text(stringResource(R.string.medication_import_force_title)) },
            text = { Text(stringResource(R.string.medication_import_force_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.forceSelect(pendingForce.key)
                        pendingForceKey = null
                    }
                ) {
                    Text(stringResource(R.string.medication_import_force_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingForceKey = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

/**
 * Pantalla inicial: explicación y selector de PDF.
 */
@Composable
private fun ImportPickFileContent(
    errorRes: Int?,
    onPickFile: () -> Unit
) {
    AppScrollWithFooter(
        modifier = Modifier.fillMaxSize(),
        footer = {
            AppButton(
                text = stringResource(R.string.medication_import_select_pdf),
                onClick = onPickFile,
                size = ButtonSize.Large
            )
        }
    ) {
        Text(
            text = stringResource(R.string.medication_import_pick_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.medication_import_pick_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        AppNoticeBlock(
            icon = Icons.Rounded.Lock,
            title = stringResource(R.string.medication_import_privacy_title),
            text = stringResource(R.string.medication_import_privacy_text)
        )
        errorRes?.let { messageRes ->
            AppNoticeBlock(
                icon = Icons.Rounded.Warning,
                title = stringResource(R.string.medication_import_error_title),
                text = stringResource(messageRes)
            )
        }
    }
}

/**
 * Pantalla de espera mientras se extrae el texto.
 */
@Composable
private fun ImportReadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.medication_import_reading),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = AppSpacing.md)
        )
    }
}

/**
 * Pantalla de revisión de los medicamentos detectados.
 */
@Composable
private fun ImportReviewContent(
    state: MedicationImportUiState,
    onStartDateChange: (LocalDate) -> Unit,
    onToggleItem: (String, Boolean) -> Unit,
    onEditItem: (MedicationImportItemUi) -> Unit,
    onDuplicateCheck: (MedicationImportItemUi) -> Unit,
    onOpenMedication: (Int, Boolean) -> Unit,
    onSave: () -> Unit
) {
    val selectedCount = state.selectedCount

    AppScrollWithFooter(
        modifier = Modifier.fillMaxSize(),
        footer = {
            AppFormActions(
                primaryText = if (selectedCount == 0) {
                    stringResource(R.string.medication_import_save_none)
                } else {
                    pluralStringResource(
                        R.plurals.medication_import_save,
                        selectedCount,
                        selectedCount
                    )
                },
                onPrimaryClick = onSave,
                primaryEnabled = selectedCount > 0 && !state.isSaving,
                supportingText = if (selectedCount > 0) {
                    pluralStringResource(
                        R.plurals.medication_import_save_support,
                        selectedCount,
                        selectedCount
                    )
                } else {
                    stringResource(R.string.medication_import_save_support_none)
                }
            )
        }
    ) {
        state.errorRes?.let { messageRes ->
            AppNoticeBlock(
                icon = Icons.Rounded.Warning,
                title = stringResource(R.string.medication_import_error_title),
                text = stringResource(messageRes)
            )
        }

        AppFormField(label = stringResource(R.string.fecha_inicio)) {
            AppDatePickerField(
                value = state.startDate,
                onDateSelected = onStartDateChange
            )
        }

        AppNoticeBlock(
            icon = Icons.Rounded.Info,
            title = stringResource(R.string.medication_import_review_title),
            text = stringResource(R.string.medication_import_review_text)
        )

        AppFormSection(title = stringResource(R.string.medication_import_list_title)) {
            state.items.forEach { item ->
                ImportMedicationCard(
                    item = item,
                    onToggle = { onToggleItem(item.key, it) },
                    onEdit = { onEditItem(item) },
                    onDuplicateCheck = { onDuplicateCheck(item) },
                    onOpenMedication = {
                        item.existingMedicationId?.let { id ->
                            onOpenMedication(id, item.existingArchived)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Card de un medicamento detectado.
 */
@Composable
private fun ImportMedicationCard(
    item: MedicationImportItemUi,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDuplicateCheck: () -> Unit,
    onOpenMedication: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            MedicationCheckRow(
                item = item,
                onToggle = onToggle,
                onDuplicateCheck = onDuplicateCheck
            )

            val doseValue = if (item.doseText.isBlank()) {
                stringResource(R.string.medication_import_no_dose)
            } else {
                listOf(item.doseText, item.unit)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
            }
            AppDetailRow(
                label = stringResource(R.string.dosis),
                value = doseValue
            )
            AppDetailRow(
                label = stringResource(R.string.medication_import_schedule_label),
                value = scheduleLabel(item)
            )
            AppDetailRow(
                label = stringResource(R.string.medication_import_treatment_label),
                value = if (item.isChronic) {
                    stringResource(R.string.medication_chronic_treatment)
                } else {
                    stringResource(R.string.medication_import_acute_treatment)
                },
                showDivider = false
            )

            if (item.status == MedicationImportStatus.EXISTING) {
                AppNoticeBlock(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.medication_import_existing_title),
                    text = item.existingName?.let { name ->
                        stringResource(R.string.medication_import_existing_text, name)
                    } ?: stringResource(R.string.medication_import_existing_pdf_text)
                )
            }

            if (item.status == MedicationImportStatus.INCOMPLETE) {
                AppNoticeBlock(
                    icon = Icons.Rounded.Warning,
                    title = stringResource(R.string.medication_import_incomplete_title),
                    text = stringResource(R.string.medication_import_incomplete_text)
                )
            }

            if (item.warnings.contains(MedicationImportWarning.INTERVAL_NOT_SUPPORTED)) {
                Text(
                    text = stringResource(R.string.medication_import_warning_interval),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.warnings.contains(MedicationImportWarning.UNKNOWN_UNIT)) {
                Text(
                    text = stringResource(R.string.medication_import_warning_unit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (item.status) {
                MedicationImportStatus.NEW -> {
                    AppButton(
                        text = stringResource(R.string.medication_import_edit),
                        onClick = onEdit,
                        variant = ButtonVariant.Tonal,
                        size = ButtonSize.Large
                    )
                }

                MedicationImportStatus.EXISTING -> {
                    if (item.existingMedicationId != null) {
                        AppButton(
                            text = stringResource(R.string.medication_import_view_existing),
                            onClick = onOpenMedication,
                            variant = ButtonVariant.Tonal,
                            size = ButtonSize.Large
                        )
                    }
                }

                MedicationImportStatus.INCOMPLETE -> {
                    AppButton(
                        text = stringResource(R.string.medication_import_complete),
                        onClick = onEdit,
                        variant = ButtonVariant.Tonal,
                        size = ButtonSize.Large
                    )
                }
            }
        }
    }
}

/**
 * Cabecera de la card con el checkbox y el estado.
 */
@Composable
private fun MedicationCheckRow(
    item: MedicationImportItemUi,
    onToggle: (Boolean) -> Unit,
    onDuplicateCheck: () -> Unit
) {
    val statusColor = when (item.status) {
        MedicationImportStatus.NEW -> MaterialTheme.colorScheme.onTertiaryContainer
        MedicationImportStatus.EXISTING,
        MedicationImportStatus.INCOMPLETE -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val statusText = when (item.status) {
        MedicationImportStatus.NEW -> stringResource(R.string.medication_import_status_new)
        MedicationImportStatus.EXISTING -> stringResource(R.string.medication_import_status_existing)
        MedicationImportStatus.INCOMPLETE -> stringResource(R.string.medication_import_status_review)
    }

    val rowModifier = when (item.status) {
        MedicationImportStatus.NEW -> Modifier.toggleable(
            value = item.selected,
            role = Role.Checkbox,
            onValueChange = onToggle
        )

        MedicationImportStatus.EXISTING -> Modifier.toggleable(
            value = false,
            role = Role.Checkbox,
            onValueChange = { onDuplicateCheck() }
        )

        MedicationImportStatus.INCOMPLETE -> Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(rowModifier),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = item.status == MedicationImportStatus.NEW && item.selected,
            onCheckedChange = null,
            enabled = item.status != MedicationImportStatus.INCOMPLETE
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = AppSpacing.sm)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                color = statusColor
            )
        }
    }
}

/**
 * Texto de la pauta con las horas, usando el mismo resumen que la ficha.
 */
@Composable
private fun scheduleLabel(item: MedicationImportItemUi): String {
    if (item.scheduleRules.isEmpty() || item.times.isEmpty()) {
        return stringResource(R.string.medication_import_no_schedule)
    }

    val form = remember(item.scheduleRules, item.times) {
        ScheduleFormState().apply {
            loadFrom(
                item.scheduleRules.flatMap { rule ->
                    item.times.map { time ->
                        Schedule(medicationId = 0, rule = rule, time = time)
                    }
                }
            )
        }
    }
    val summary = scheduleSummary(form)
    val timePattern = stringResource(R.string.common_time_pattern)
    val formatter = remember(timePattern) { DateTimeFormatter.ofPattern(timePattern) }
    val timeText = item.times.joinToString(", ") { time -> time.format(formatter) }

    return "$summary · $timeText"
}

/**
 * Resultado de la importación.
 */
@Composable
private fun ImportSavedContent(
    count: Int,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.medication_import_saved_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = pluralStringResource(
                R.plurals.medication_import_saved_message,
                count,
                count
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = AppSpacing.sm, bottom = AppSpacing.lg)
        )
        AppButton(
            text = stringResource(R.string.medication_import_close),
            onClick = onClose,
            size = ButtonSize.Large
        )
    }
}

/**
 * Hoja de edición de un medicamento detectado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationImportEditSheet(
    item: MedicationImportItemUi,
    onDismiss: () -> Unit,
    onApply: (String, String, String, List<ScheduleRule>, List<LocalTime>, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val unitOptions = stringArrayResource(R.array.medication_units).toList()

    var name by remember(item.key) { mutableStateOf(item.name) }
    var dose by remember(item.key) { mutableStateOf(item.doseText) }
    var unit by remember(item.key) { mutableStateOf(item.unit) }
    var chronic by remember(item.key) { mutableStateOf(item.isChronic) }

    val form = rememberSaveable(item.key, saver = ScheduleFormStateSaver) {
        ScheduleFormState().apply {
            val rules = item.scheduleRules.ifEmpty {
                listOf(ScheduleRule.EveryNDays(1))
            }
            val timesToUse = item.times.ifEmpty {
                listOf(LocalTime.of(9, 0))
            }
            loadFrom(
                rules.flatMap { rule ->
                    timesToUse.map { time ->
                        Schedule(medicationId = 0, rule = rule, time = time)
                    }
                }
            )
        }
    }

    val parsedDose = dose.replace(',', '.').toDoubleOrNull()
    val canApply = name.isNotBlank() &&
        parsedDose != null &&
        parsedDose > 0 &&
        unit.isNotBlank() &&
        form.isValid()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.md)
                .padding(top = AppSpacing.md, bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.medication_import_edit_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.common_close)
                    )
                }
            }

            AppFormField(label = stringResource(R.string.nombre)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = stringResource(R.string.medication_name_placeholder)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                AppFormField(
                    label = stringResource(R.string.dosis),
                    modifier = Modifier.weight(1f)
                ) {
                    AppTextField(
                        value = dose,
                        onValueChange = { dose = it },
                        keyboardType = KeyboardType.Decimal
                    )
                }
                AppFormField(
                    label = stringResource(R.string.unidad),
                    modifier = Modifier.weight(1f)
                ) {
                    AppDropdownField(
                        value = unit,
                        options = unitOptions,
                        onOptionSelected = { unit = it },
                        placeholder = stringResource(R.string.medication_select_placeholder)
                    )
                }
            }

            ScheduleEditor(
                form = form,
                enabled = true
            )

            SettingsToggleItem(
                label = stringResource(R.string.medication_chronic_treatment),
                checked = chronic,
                onCheckedChange = { chronic = it }
            )

            if (!canApply) {
                Text(
                    text = stringResource(R.string.medication_import_incomplete_edit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AppButton(
                text = stringResource(R.string.medication_import_apply),
                onClick = {
                    onApply(name, dose, unit, form.rules(), form.times.toList(), chronic)
                },
                size = ButtonSize.Large,
                enabled = canApply
            )
        }
    }
}

@Preview(showBackground = true, name = "Medicamento importado nuevo")
@Composable
private fun ImportMedicationCardPreview() {
    PillRemindTheme {
        ImportMedicationCard(
            item = MedicationImportItemUi(
                key = "preview",
                name = "RYALTRIS 25/600MCG/PULSACION",
                doseText = "2",
                unit = "pulsaciones",
                scheduleRules = listOf(ScheduleRule.EveryNDays(1)),
                times = listOf(LocalTime.of(9, 0)),
                isChronic = true,
                status = MedicationImportStatus.NEW,
                selected = true
            ),
            onToggle = {},
            onEdit = {},
            onDuplicateCheck = {},
            onOpenMedication = {}
        )
    }
}

private const val PDF_MIME_TYPE = "application/pdf"
