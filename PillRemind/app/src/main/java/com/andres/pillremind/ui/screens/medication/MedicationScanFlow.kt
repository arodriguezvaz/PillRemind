package com.andres.pillremind.ui.screens.medication

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.MedicationDraft
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.AppFormSection
import com.andres.pillremind.ui.components.AppPhotoPicker
import com.andres.pillremind.ui.components.AppScrollWithFooter
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Tarjeta que abre el escaneo desde el formulario de creación.
 */
@Composable
fun ScanCtaCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_medication),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.scan_cn_cta_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = stringResource(R.string.scan_cn_cta_text),
                style = MaterialTheme.typography.bodyMedium
            )
            AppButton(
                text = stringResource(R.string.scan_cn_button),
                onClick = onClick,
                size = ButtonSize.Large
            )
        }
    }
}

/**
 * Flujo de escaneo a pantalla completa: fotos, análisis y revisión.
 *
 * El CN y la respuesta de CIMA viven solo en [MedicationScanViewModel]; este
 * composable no los persiste. Al aplicar los datos se devuelven al formulario
 * de creación como un borrador normal.
 *
 * @param state Estado actual del flujo.
 * @param onDismiss Cierra el flujo y descarta el borrador.
 * @param onBoxPhoto Selecciona la foto de la caja.
 * @param onCodePhoto Selecciona la foto del código.
 * @param onManualCnChange Actualiza el CN escrito a mano.
 * @param onAnalyze Lanza el análisis de las fotos.
 * @param onBackToPhotos Vuelve a la pantalla de fotos conservando lo elegido.
 * @param onGrantConsent Acepta la consulta a CIMA.
 * @param onDeclineConsent Rechaza la consulta a CIMA.
 * @param onApply Aplica el borrador editado al formulario de medicamento.
 */
@Composable
fun MedicationScanFlow(
    state: ScanUiState,
    onDismiss: () -> Unit,
    onBoxPhoto: (Uri) -> Unit,
    onCodePhoto: (Uri) -> Unit,
    onManualCnChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onBackToPhotos: () -> Unit,
    onGrantConsent: () -> Unit,
    onDeclineConsent: () -> Unit,
    onApply: (MedicationDraft, Uri?) -> Unit
) {
    var scrollToCn by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            when (state.stage) {
                ScanStage.PHOTOS -> onDismiss()
                ScanStage.ANALYZING,
                ScanStage.RESULT -> onBackToPhotos()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding()
            ) {
                ScanTopBar(
                    title = when (state.stage) {
                        ScanStage.PHOTOS -> stringResource(R.string.scan_photos_title)
                        ScanStage.ANALYZING -> stringResource(R.string.scan_analyzing_title)
                        ScanStage.RESULT -> stringResource(R.string.scan_result_title)
                    },
                    backIsClose = state.stage == ScanStage.PHOTOS,
                    onBack = when (state.stage) {
                        ScanStage.PHOTOS -> onDismiss
                        else -> onBackToPhotos
                    }
                )

                when (state.stage) {
                    ScanStage.PHOTOS -> ScanPhotosContent(
                        state = state,
                        scrollToCn = scrollToCn,
                        onScrollHandled = { scrollToCn = false },
                        onBoxPhoto = onBoxPhoto,
                        onCodePhoto = onCodePhoto,
                        onManualCnChange = onManualCnChange,
                        onAnalyze = onAnalyze,
                        onDismiss = onDismiss
                    )

                    ScanStage.ANALYZING -> ScanAnalyzingContent(
                        state = state,
                        onBackToPhotos = onBackToPhotos
                    )

                    ScanStage.RESULT -> ScanResultContent(
                        state = state,
                        onApply = onApply,
                        onWriteCn = {
                            scrollToCn = true
                            onBackToPhotos()
                        },
                        onBackToPhotos = onBackToPhotos,
                        onDismiss = onDismiss
                    )
                }
            }
        }

        if (state.consentRequired) {
            ConsentDialog(
                onGrant = onGrantConsent,
                onDecline = onDeclineConsent
            )
        }
    }
}

@Composable
private fun ScanTopBar(
    title: String,
    backIsClose: Boolean,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = if (backIsClose) {
                    ImageVector.vectorResource(R.drawable.ic_close)
                } else {
                    ImageVector.vectorResource(R.drawable.ic_arrow_back)
                },
                contentDescription = stringResource(
                    if (backIsClose) R.string.common_close else R.string.common_back
                )
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = AppSpacing.md)
        )
    }
}

@Composable
private fun ScanPhotosContent(
    state: ScanUiState,
    scrollToCn: Boolean,
    onScrollHandled: () -> Unit,
    onBoxPhoto: (Uri) -> Unit,
    onCodePhoto: (Uri) -> Unit,
    onManualCnChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val cnFocusRequester = remember { FocusRequester() }

    LaunchedEffect(scrollToCn, scrollState.maxValue) {
        if (scrollToCn) {
            runCatching { cnFocusRequester.requestFocus() }
            if (scrollState.maxValue > 0) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            onScrollHandled()
        }
    }

    AppScrollWithFooter(
        modifier = Modifier.fillMaxSize(),
        scrollState = scrollState,
        footer = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppButton(
                    text = stringResource(R.string.scan_analyze),
                    onClick = onAnalyze,
                    size = ButtonSize.Large,
                    enabled = state.canAnalyze
                )
                AppButton(
                    text = stringResource(R.string.scan_fill_manual),
                    onClick = onDismiss,
                    size = ButtonSize.Large,
                    variant = ButtonVariant.Tonal
                )
            }
        }
    ) {
        state.errorRes?.let { errorRes ->
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        Text(
            text = stringResource(R.string.scan_photos_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ScanPhotoSlot(
            title = stringResource(R.string.scan_box_title),
            hint = stringResource(R.string.scan_box_hint),
            photoUri = state.boxPhotoUri,
            photoDescriptionRes = R.string.scan_box_photo,
            placeholderDescriptionRes = R.string.scan_add_box_photo,
            onPhotoSelected = onBoxPhoto
        )
        ScanPhotoSlot(
            title = stringResource(R.string.scan_code_title),
            hint = stringResource(R.string.scan_code_hint),
            photoUri = state.codePhotoUri,
            photoDescriptionRes = R.string.scan_code_photo,
            placeholderDescriptionRes = R.string.scan_add_code_photo,
            onPhotoSelected = onCodePhoto
        )
        AppFormField(label = stringResource(R.string.scan_manual_cn_label)) {
            AppTextField(
                value = state.manualCn,
                onValueChange = onManualCnChange,
                placeholder = stringResource(R.string.scan_manual_cn_placeholder),
                keyboardType = KeyboardType.Number,
                focusRequester = cnFocusRequester
            )
            if (state.manualCnInvalid) {
                Text(
                    text = stringResource(R.string.scan_error_invalid_cn),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = AppSpacing.xs)
                )
            }
        }
    }
}

@Composable
private fun ScanPhotoSlot(
    title: String,
    hint: String,
    photoUri: Uri?,
    @StringRes photoDescriptionRes: Int,
    @StringRes placeholderDescriptionRes: Int,
    onPhotoSelected: (Uri) -> Unit
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            AppPhotoPicker(
                photoUri = photoUri,
                onPhotoSelected = onPhotoSelected,
                size = 128.dp,
                placeholderIcon = ImageVector.vectorResource(R.drawable.ic_add_a_photo),
                photoDescriptionRes = photoDescriptionRes,
                cropEnabled = false,
                placeholderDescriptionRes = placeholderDescriptionRes
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScanAnalyzingContent(
    state: ScanUiState,
    onBackToPhotos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.xl))

        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Text(
            text = stringResource(R.string.scan_analyzing_text),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = AppElevation)
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                ScanProgressRow(
                    label = stringResource(R.string.scan_step_code),
                    step = state.codeStep,
                    detail = scanStepDetail(
                        step = state.codeStep,
                        runningRes = R.string.scan_step_reading_code,
                        doneRes = R.string.scan_step_done,
                        failedRes = R.string.scan_step_failed
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ScanProgressRow(
                    label = stringResource(R.string.scan_step_box),
                    step = state.boxStep,
                    detail = scanStepDetail(
                        step = state.boxStep,
                        runningRes = R.string.scan_step_reading_box,
                        doneRes = R.string.scan_step_done,
                        failedRes = R.string.scan_step_failed
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ScanProgressRow(
                    label = stringResource(R.string.scan_step_cima),
                    step = state.cimaStep,
                    detail = scanStepDetail(
                        step = state.cimaStep,
                        runningRes = R.string.scan_step_consulting_cima,
                        doneRes = R.string.scan_step_found,
                        failedRes = R.string.scan_step_not_available
                    )
                )
            }
        }

        AppButton(
            text = stringResource(R.string.scan_back_to_photos),
            onClick = onBackToPhotos,
            size = ButtonSize.Large,
            variant = ButtonVariant.Tonal
        )
    }
}

@Composable
private fun ScanProgressRow(
    label: String,
    step: StepState,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (step) {
                StepState.PENDING -> Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_schedule),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                StepState.RUNNING -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )

                StepState.DONE -> Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )

                StepState.FAILED -> Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )

                StepState.SKIPPED -> Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_remove_circle),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun scanStepDetail(
    step: StepState,
    @StringRes runningRes: Int,
    @StringRes doneRes: Int,
    @StringRes failedRes: Int
): String = when (step) {
    StepState.PENDING -> stringResource(R.string.scan_step_pending)
    StepState.RUNNING -> stringResource(runningRes)
    StepState.DONE -> stringResource(doneRes)
    StepState.FAILED -> stringResource(failedRes)
    StepState.SKIPPED -> stringResource(R.string.scan_step_skipped)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScanResultContent(
    state: ScanUiState,
    onApply: (MedicationDraft, Uri?) -> Unit,
    onWriteCn: () -> Unit,
    onBackToPhotos: () -> Unit,
    onDismiss: () -> Unit
) {
    if (state.notice == ScanNotice.NOTHING) {
        ScanNothingContent(
            onBackToPhotos = onBackToPhotos,
            onWriteCn = onWriteCn,
            onDismiss = onDismiss
        )
        return
    }

    val draft = state.draft
    val unitOptions = stringArrayResource(R.array.medication_units).toList()
    val doseChipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    var name by rememberSaveable(draft) { mutableStateOf(draft?.name.orEmpty()) }
    var dose by rememberSaveable(draft) { mutableStateOf(draft?.doseText.orEmpty()) }
    var unit by rememberSaveable(draft) { mutableStateOf(draft?.unit.orEmpty()) }
    var useBoxPhoto by rememberSaveable(state.boxPhotoUri) {
        mutableStateOf(state.boxPhotoUri != null)
    }

    val parsedDose = parseDoseInput(dose)
    val doseInvalid = dose.isNotBlank() && parsedDose == null
    val canApply = name.isNotBlank() && parsedDose != null && unit in unitOptions

    AppScrollWithFooter(
        modifier = Modifier.fillMaxSize(),
        footer = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppButton(
                    text = stringResource(R.string.scan_use_data),
                    onClick = {
                        onApply(
                            (draft ?: MedicationDraft()).copy(
                                name = name,
                                doseText = dose,
                                unit = unit
                            ),
                            if (useBoxPhoto) state.boxPhotoUri else null
                        )
                    },
                    size = ButtonSize.Large,
                    enabled = canApply
                )
                if (!canApply) {
                    Text(
                        text = stringResource(R.string.scan_result_incomplete),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AppButton(
                    text = stringResource(R.string.scan_back_to_photos),
                    onClick = onBackToPhotos,
                    size = ButtonSize.Large,
                    variant = ButtonVariant.Tonal
                )
            }
        }
    ) {
        AppFormSection(
            title = stringResource(R.string.medication_data_section),
            contentSpacing = 0.dp
        ) {
            AppFormField(label = stringResource(R.string.nombre)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = stringResource(R.string.medication_name_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AppSpacing.md)
                )
            }
            val nameCandidates = draft?.nameCandidates.orEmpty()
            if (draft?.nameConfident == false) {
                if (nameCandidates.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.scan_result_name_candidates),
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        modifier = Modifier.padding(bottom = AppSpacing.md)
                    ) {
                        nameCandidates.take(3).forEach { candidate ->
                            FilterChip(
                                selected = name.equals(candidate, ignoreCase = true),
                                onClick = { name = candidate },
                                label = { Text(candidate) },
                                colors = doseChipColors
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.scan_result_name_missing),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppSpacing.md)
                    )
                }
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
                            value = dose,
                            onValueChange = { dose = it },
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
                            value = unit,
                            options = unitOptions,
                            onOptionSelected = { unit = it },
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
                        .padding(bottom = AppSpacing.md)
                )
            }
        }

        val draftDetails = draft
        if (draftDetails?.presentation != null) {
            Text(
                text = stringResource(R.string.scan_result_presentation, draftDetails.presentation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (draftDetails?.strengthText != null) {
            Text(
                text = stringResource(R.string.scan_result_strength, draftDetails.strengthText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val intakeDose = draft?.intakeDoseText
        val intakeUnit = draft?.intakeUnit
        val strengthDose = draft?.strengthDoseText
        val strengthUnit = draft?.strengthUnit
        val showStrengthChip = draft?.strengthText?.contains('/') != true &&
            strengthUnit != "%" && strengthUnit != "ml"
        val hasIntakeChip = intakeDose != null && intakeUnit != null
        val hasStrengthChip = strengthDose != null && strengthUnit != null && showStrengthChip
        if (hasIntakeChip || hasStrengthChip) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = stringResource(R.string.scan_result_choose_dose),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    if (intakeDose != null && intakeUnit != null) {
                        FilterChip(
                            selected = dose == intakeDose && unit == intakeUnit,
                            onClick = {
                                dose = intakeDose
                                unit = intakeUnit
                            },
                            label = {
                                Text(
                                    stringResource(
                                        R.string.scan_cn_dose_intake,
                                        intakeDose,
                                        intakeUnit
                                    )
                                )
                            },
                            colors = doseChipColors
                        )
                    }
                    if (strengthDose != null && strengthUnit != null && showStrengthChip) {
                        FilterChip(
                            selected = dose == strengthDose && unit == strengthUnit,
                            onClick = {
                                dose = strengthDose
                                unit = strengthUnit
                            },
                            label = {
                                Text(
                                    stringResource(
                                        R.string.scan_cn_dose_strength,
                                        strengthDose,
                                        strengthUnit
                                    )
                                )
                            },
                            colors = doseChipColors
                        )
                    }
                }
            }
        }

        if (draft?.presentation != null && dose.isBlank() && unit.isBlank()) {
            Text(
                text = stringResource(R.string.scan_result_no_dose),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = AppSpacing.md)
            )
        }

        if (state.boxPhotoUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = useBoxPhoto,
                        role = Role.Checkbox,
                        onValueChange = { useBoxPhoto = it }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = useBoxPhoto,
                    onCheckedChange = null
                )
                Text(
                    text = stringResource(R.string.scan_use_box_photo),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        ScanNoticeCard(
            notice = state.notice,
            draft = draft
        )
    }
}

@Composable
private fun ScanNothingContent(
    onBackToPhotos: () -> Unit,
    onWriteCn: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        ScanNoticeCard(
            notice = ScanNotice.NOTHING,
            draft = null
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        AppButton(
            text = stringResource(R.string.scan_back_to_photos),
            onClick = onBackToPhotos,
            size = ButtonSize.Large
        )
        AppButton(
            text = stringResource(R.string.scan_write_cn),
            onClick = onWriteCn,
            size = ButtonSize.Large,
            variant = ButtonVariant.Tonal
        )
        AppButton(
            text = stringResource(R.string.scan_fill_manual),
            onClick = onDismiss,
            size = ButtonSize.Large,
            variant = ButtonVariant.Ghost
        )
    }
}

@Composable
private fun ScanNoticeCard(
    notice: ScanNotice,
    draft: MedicationDraft?
) {
    if (notice == ScanNotice.NONE) return

    val containerColor = when (notice) {
        ScanNotice.CIMA_FOUND -> MaterialTheme.colorScheme.tertiaryContainer
        ScanNotice.CIMA_ERROR -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (notice) {
        ScanNotice.CIMA_FOUND -> MaterialTheme.colorScheme.onTertiaryContainer
        ScanNotice.CIMA_ERROR -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val icon = when (notice) {
        ScanNotice.CIMA_FOUND -> ImageVector.vectorResource(R.drawable.ic_check_circle)
        else -> ImageVector.vectorResource(R.drawable.ic_warning)
    }
    val title = when (notice) {
        ScanNotice.CIMA_FOUND -> R.string.scan_notice_cima_found_title
        ScanNotice.CIMA_NOT_FOUND,
        ScanNotice.CIMA_NOT_FOUND_EMPTY -> R.string.scan_notice_cima_not_found_title

        ScanNotice.CIMA_ERROR -> R.string.scan_notice_cima_error_title
        ScanNotice.CIMA_NOT_CONSULTED -> R.string.scan_notice_cima_not_consulted_title
        ScanNotice.CN_NOT_DETECTED -> R.string.scan_notice_cn_not_detected_title
        ScanNotice.CODE_UNREADABLE -> R.string.scan_notice_code_unreadable_title
        ScanNotice.NOTHING -> R.string.scan_notice_nothing_title
        ScanNotice.NONE -> return
    }
    val datePattern = stringResource(R.string.common_date_pattern)
    val detectedCn = draft?.cn
    val text = when (notice) {
        ScanNotice.CIMA_FOUND -> stringResource(
            R.string.scan_cn_attribution,
            (draft?.retrievedAt ?: LocalDate.now())
                .format(DateTimeFormatter.ofPattern(datePattern))
        )

        ScanNotice.CIMA_NOT_FOUND,
        ScanNotice.CODE_UNREADABLE,
        ScanNotice.CN_NOT_DETECTED -> stringResource(R.string.scan_notice_box_text)

        ScanNotice.CIMA_NOT_FOUND_EMPTY,
        ScanNotice.CIMA_ERROR -> if (draft == null) {
            stringResource(R.string.scan_notice_cima_empty_text)
        } else {
            stringResource(R.string.scan_notice_box_text)
        }

        ScanNotice.CIMA_NOT_CONSULTED -> stringResource(
            R.string.scan_notice_cima_not_consulted_text
        )

        ScanNotice.NOTHING -> stringResource(R.string.scan_notice_nothing_text)
        ScanNotice.NONE -> return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.labelLarge
                )
                if (notice == ScanNotice.CIMA_FOUND && detectedCn != null) {
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(
                        text = stringResource(R.string.scan_notice_cn_detected, detectedCn),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ConsentDialog(
    onGrant: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(stringResource(R.string.scan_cn_consent_title)) },
        text = { Text(stringResource(R.string.scan_cn_consent_message)) },
        confirmButton = {
            TextButton(onClick = onGrant) {
                Text(stringResource(R.string.scan_cn_consent_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(R.string.scan_cn_consent_decline))
            }
        }
    )
}

@Preview(showBackground = true, name = "Tarjeta de escaneo")
@Composable
private fun ScanCtaCardPreview() {
    PillRemindTheme {
        ScanCtaCard(
            onClick = {},
            modifier = Modifier.padding(AppSpacing.md)
        )
    }
}

@Preview(showBackground = true, name = "Aviso de CIMA encontrado")
@Composable
private fun ScanNoticeCardPreview() {
    PillRemindTheme {
        ScanNoticeCard(
            notice = ScanNotice.CIMA_FOUND,
            draft = MedicationDraft(
                name = "NASONEX",
                retrievedAt = LocalDate.of(2026, 11, 9)
            )
        )
    }
}
