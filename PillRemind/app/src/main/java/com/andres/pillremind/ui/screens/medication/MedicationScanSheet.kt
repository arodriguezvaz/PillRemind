package com.andres.pillremind.ui.screens.medication

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.DraftSource
import com.andres.pillremind.domain.model.MedicationDraft
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.AppPhotoPicker
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.theme.AppSpacing
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
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
                    imageVector = Icons.Rounded.Medication,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.scan_cn_cta_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = stringResource(R.string.scan_cn_cta_text),
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.scan_cn_button))
            }
        }
    }
}

/**
 * Hoja de escaneo: CN, consulta a CIMA, foto de caja y revisión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScanSheet(
    state: ScanUiState,
    onDismiss: () -> Unit,
    onCnChange: (String) -> Unit,
    onCnPhoto: (Uri) -> Unit,
    onSearchCn: () -> Unit,
    onBoxPhoto: (Uri) -> Unit,
    onReadBox: () -> Unit,
    onShowBox: () -> Unit,
    onBackToCn: () -> Unit,
    onGrantConsent: () -> Unit,
    onDeclineConsent: () -> Unit,
    onApply: (MedicationDraft, Uri?) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.md)
                .padding(bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = stringResource(R.string.scan_cn_title),
                style = MaterialTheme.typography.headlineSmall
            )

            when (state.stage) {
                ScanStage.CN_INPUT -> CnInputStep(
                    state = state,
                    onCnChange = onCnChange,
                    onCnPhoto = onCnPhoto,
                    onSearchCn = onSearchCn,
                    onShowBox = onShowBox
                )

                ScanStage.BOX_INPUT -> BoxInputStep(
                    state = state,
                    onBoxPhoto = onBoxPhoto,
                    onReadBox = onReadBox,
                    onBackToCn = onBackToCn
                )

                ScanStage.REVIEW -> state.draft?.let { draft ->
                    ReviewStep(
                        draft = draft,
                        boxPhotoUri = state.boxPhotoUri,
                        onApply = onApply
                    )
                }
            }

            if (state.loading) {
                LoadingRow()
            }

            state.errorRes?.let { errorRes ->
                ErrorText(errorRes = errorRes, notFoundCn = state.notFoundCn)
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.common_cancel))
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

@Composable
private fun CnInputStep(
    state: ScanUiState,
    onCnChange: (String) -> Unit,
    onCnPhoto: (Uri) -> Unit,
    onSearchCn: () -> Unit,
    onShowBox: () -> Unit
) {
    Text(
        text = stringResource(R.string.scan_cn_help),
        style = MaterialTheme.typography.bodySmall
    )

    AppFormField(label = stringResource(R.string.scan_cn_manual_label)) {
        AppTextField(
            value = state.cnInput,
            onValueChange = onCnChange,
            placeholder = stringResource(R.string.scan_cn_manual_placeholder),
            keyboardType = KeyboardType.Number
        )
    }

    AppPhotoPicker(
        photoUri = state.cnPhotoUri,
        onPhotoSelected = onCnPhoto,
        placeholderIcon = Icons.Default.AddAPhoto,
        photoDescriptionRes = R.string.scan_cn_photo,
        modifier = Modifier.fillMaxWidth()
    )

    Button(
        onClick = onSearchCn,
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.loading
    ) {
        Text(stringResource(R.string.scan_cn_search))
    }

    TextButton(onClick = onShowBox) {
        Text(stringResource(R.string.scan_cn_box_photo))
    }
}

@Composable
private fun BoxInputStep(
    state: ScanUiState,
    onBoxPhoto: (Uri) -> Unit,
    onReadBox: () -> Unit,
    onBackToCn: () -> Unit
) {
    Text(
        text = if (state.notFoundCn != null) {
            stringResource(R.string.scan_cn_not_found_message, state.notFoundCn)
        } else {
            stringResource(R.string.scan_cn_box_help)
        },
        style = MaterialTheme.typography.bodySmall
    )

    AppPhotoPicker(
        photoUri = state.boxPhotoUri,
        onPhotoSelected = onBoxPhoto,
        placeholderIcon = Icons.Default.AddAPhoto,
        photoDescriptionRes = R.string.scan_cn_box_photo,
        modifier = Modifier.fillMaxWidth()
    )

    Button(
        onClick = onReadBox,
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.loading
    ) {
        Text(stringResource(R.string.scan_cn_read_box))
    }

    TextButton(onClick = onBackToCn) {
        Text(stringResource(R.string.scan_cn_back))
    }
}

@Composable
private fun ReviewStep(
    draft: MedicationDraft,
    boxPhotoUri: Uri?,
    onApply: (MedicationDraft, Uri?) -> Unit
) {
    val unitOptions = stringArrayResource(R.array.medication_units).toList()
    var name by remember(draft) { mutableStateOf(draft.name) }
    var dose by remember(draft) { mutableStateOf(draft.doseText) }
    var unit by remember(draft) { mutableStateOf(draft.unit) }

    Text(
        text = when (draft.source) {
            DraftSource.CIMA -> stringResource(R.string.scan_cn_source_cima)
            DraftSource.OCR -> stringResource(R.string.scan_cn_source_ocr)
            DraftSource.MANUAL -> stringResource(R.string.scan_cn_source_manual)
        },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    if (draft.cn != null) {
        Text(stringResource(R.string.scan_cn_cn, draft.cn))
    }
    if (draft.strengthText != null) {
        Text(stringResource(R.string.scan_cn_strength, draft.strengthText))
    }
    if (draft.presentation != null) {
        Text(stringResource(R.string.scan_cn_presentation, draft.presentation))
    }
    if (draft.notCatalogued && draft.cn != null) {
        Text(
            text = stringResource(R.string.scan_cn_not_catalogued, draft.cn),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }

    AppFormField(label = stringResource(R.string.nombre)) {
        AppTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.medication_name_placeholder)
        )
    }

    AppFormField(label = stringResource(R.string.dosis)) {
        AppTextField(
            value = dose,
            onValueChange = { dose = it },
            placeholder = stringResource(R.string.medication_dose_placeholder),
            keyboardType = KeyboardType.Decimal
        )
    }

    AppFormField(label = stringResource(R.string.unidad)) {
        AppDropdownField(
            value = unit,
            options = unitOptions,
            onOptionSelected = { unit = it },
            placeholder = stringResource(R.string.medication_select_placeholder)
        )
    }

    val intake = draft.intakeDoseText?.let { intakeDose ->
        draft.intakeUnit?.let { intakeUnit -> intakeDose to intakeUnit }
    }
    val strength = draft.strengthDoseText?.let { strengthDose ->
        draft.strengthUnit?.let { strengthUnit -> strengthDose to strengthUnit }
    }

    if (intake != null && strength != null) {
        HorizontalDivider()
        Text(
            text = stringResource(R.string.scan_cn_dose_section),
            style = MaterialTheme.typography.labelLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            FilterChip(
                selected = dose == intake.first && unit == intake.second,
                onClick = {
                    dose = intake.first
                    unit = intake.second
                },
                label = {
                    Text(
                        stringResource(
                            R.string.scan_cn_dose_intake,
                            intake.first,
                            intake.second
                        )
                    )
                }
            )
            FilterChip(
                selected = dose == strength.first && unit == strength.second,
                onClick = {
                    dose = strength.first
                    unit = strength.second
                },
                label = {
                    Text(
                        stringResource(
                            R.string.scan_cn_dose_strength,
                            strength.first,
                            strength.second
                        )
                    )
                }
            )
        }
    }

    if (draft.retrievedAt != null) {
        Text(
            text = stringResource(
                R.string.scan_cn_attribution,
                draft.retrievedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (name.isBlank() && dose.isBlank()) {
        Text(
            text = stringResource(R.string.scan_cn_no_data),
            style = MaterialTheme.typography.bodySmall
        )
    }

    Button(
        onClick = { onApply(draft.copy(name = name, doseText = dose, unit = unit), boxPhotoUri) },
        modifier = Modifier.fillMaxWidth(),
        enabled = name.isNotBlank() &&
            dose.isNotBlank() &&
            unit.isNotBlank() &&
            unit in unitOptions
    ) {
        Text(stringResource(R.string.scan_cn_apply))
    }
}

@Composable
private fun LoadingRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
        )
        Text(stringResource(R.string.scan_cn_loading))
    }
}

@Composable
private fun ErrorText(errorRes: Int, notFoundCn: String?) {
    val text = if (errorRes == R.string.scan_cn_not_found_title && notFoundCn != null) {
        stringResource(R.string.scan_cn_not_found_message, notFoundCn)
    } else {
        stringResource(errorRes)
    }
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
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
