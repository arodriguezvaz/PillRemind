package com.andres.pillremind.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.BloodType
import com.andres.pillremind.domain.model.Sex
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.AppDatePickerField
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormActionBar
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.AppFormScaffold
import com.andres.pillremind.ui.components.AppFormSection
import com.andres.pillremind.ui.components.AppInfoCard
import com.andres.pillremind.ui.components.AppInfoRow
import com.andres.pillremind.ui.components.AppPhotoPicker
import com.andres.pillremind.ui.components.AppReadOnlyField
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.labelRes
import com.andres.pillremind.ui.screens.main.TopBarState
import com.andres.pillremind.ui.theme.AppSpacing
import java.io.File
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

/**
 * Representa los distintos modos de uso de la pantalla de perfil.
 *
 * Permite definir si la pantalla se usa para crear, visualizar o editar un perfil.
 */
sealed class ProfileMode {
    object Create : ProfileMode()
    object View : ProfileMode()
    object Edit : ProfileMode()
}

/**
 * Pantalla de gestión de perfil de usuario.
 *
 * Permite crear, visualizar y editar un perfil según el modo activo.
 * Gestiona la imagen del perfil, los datos personales, la información médica
 * y el número de tarjeta sanitaria.
 *
 * @param modifier Modificador opcional para la pantalla.
 * @param onSaveClicked Callback ejecutado al guardar cambios.
 * @param profileMode Modo inicial de la pantalla (crear, ver o editar).
 * @param onDeletedClicked Callback ejecutado tras eliminar el perfil.
 * @param onTopBarChange Callback para modificar la TopBar.
 * @param profileViewModel ViewModel encargado de la lógica del perfil.
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit = {},
    profileMode: ProfileMode = ProfileMode.Create,
    onDeletedClicked: () -> Unit = {},
    onTopBarChange: (TopBarState) -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val name = rememberSaveable { mutableStateOf("") }
    val birthDate = rememberSaveable { mutableStateOf<LocalDate?>(null) }
    val sex = rememberSaveable { mutableStateOf<Sex?>(null) }
    val bloodType = rememberSaveable { mutableStateOf<BloodType?>(null) }
    val allergies = rememberSaveable { mutableStateOf("") }
    val medicalNotes = rememberSaveable { mutableStateOf("") }
    val healthCardNumber = rememberSaveable { mutableStateOf("") }

    val perfil by profileViewModel.profile.collectAsStateWithLifecycle()
    var selectedPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var currentImagePath by rememberSaveable { mutableStateOf<String?>(null) }
    var newPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showAdherencePlaceholder by rememberSaveable { mutableStateOf(false) }
    var formInitialized by rememberSaveable { mutableStateOf(false) }
    val modeSaver = Saver<ProfileMode, String>(
        save = { mode ->
            when (mode) {
                ProfileMode.Create -> "create"
                ProfileMode.View -> "view"
                ProfileMode.Edit -> "edit"
            }
        },
        restore = { value ->
            when (value) {
                "create" -> ProfileMode.Create
                "view" -> ProfileMode.View
                else -> ProfileMode.Edit
            }
        }
    )
    var currentMode by rememberSaveable(stateSaver = modeSaver) {
        mutableStateOf(profileMode)
    }

    val topBarTitle = when (currentMode) {
        ProfileMode.Create -> stringResource(R.string.profile_create_title)
        ProfileMode.View -> stringResource(R.string.profile_view_title)
        ProfileMode.Edit -> stringResource(R.string.profile_edit_title)
    }

    LaunchedEffect(topBarTitle) {
        onTopBarChange(TopBarState(title = topBarTitle))
    }

    BackHandler(enabled = currentMode == ProfileMode.Edit) {
        currentMode = ProfileMode.View
    }

    LaunchedEffect(perfil) {
        if (!formInitialized && (perfil != null || profileMode == ProfileMode.Create)) {
            perfil?.let {
                name.value = it.name
                selectedPhotoUri = it.urlImagen?.let(Uri::parse)
                currentImagePath = it.urlImagen
                birthDate.value = it.birthDate
                sex.value = it.sex
                bloodType.value = it.bloodType
                allergies.value = it.allergies.orEmpty()
                medicalNotes.value = it.medicalNotes.orEmpty()
                healthCardNumber.value = it.healthCardNumber.orEmpty()
            }
            formInitialized = true
        }
    }

    when (currentMode) {
        ProfileMode.Create -> {
            ProfileForm(
                modifier = modifier,
                enabled = true,
                name = name.value,
                onNameChange = { name.value = it },
                photoUri = selectedPhotoUri,
                onPhotoSelected = { selectedPhotoUri = it },
                birthDate = birthDate.value,
                onBirthDateChange = { birthDate.value = it },
                sex = sex.value,
                onSexChange = { sex.value = it },
                bloodType = bloodType.value,
                onBloodTypeChange = { bloodType.value = it },
                allergies = allergies.value,
                onAllergiesChange = { allergies.value = it },
                medicalNotes = medicalNotes.value,
                onMedicalNotesChange = { medicalNotes.value = it },
                healthCardNumber = healthCardNumber.value,
                onHealthCardNumberChange = { healthCardNumber.value = it },
                actionBar = {
                    AppFormActionBar(
                        primaryText = stringResource(R.string.guardar),
                        onPrimaryClick = {
                            profileViewModel.addProfile(
                                nombre = name.value,
                                photoUri = selectedPhotoUri,
                                birthDate = birthDate.value,
                                sex = sex.value,
                                bloodType = bloodType.value,
                                allergies = allergies.value,
                                medicalNotes = medicalNotes.value,
                                healthCardNumber = healthCardNumber.value,
                                onSaved = onSaveClicked
                            )
                        },
                        primaryEnabled = name.value.isNotBlank(),
                        supportingText = if (name.value.isBlank()) {
                            stringResource(R.string.profile_save_name_required)
                        } else {
                            null
                        },
                    )
                }
            )
        }

        ProfileMode.Edit -> {
            ProfileForm(
                modifier = modifier,
                enabled = true,
                name = name.value,
                onNameChange = { name.value = it },
                photoUri = newPhotoUri ?: currentImagePath?.let { Uri.fromFile(File(it)) },
                onPhotoSelected = { newPhotoUri = it },
                birthDate = birthDate.value,
                onBirthDateChange = { birthDate.value = it },
                sex = sex.value,
                onSexChange = { sex.value = it },
                bloodType = bloodType.value,
                onBloodTypeChange = { bloodType.value = it },
                allergies = allergies.value,
                onAllergiesChange = { allergies.value = it },
                medicalNotes = medicalNotes.value,
                onMedicalNotesChange = { medicalNotes.value = it },
                healthCardNumber = healthCardNumber.value,
                onHealthCardNumberChange = { healthCardNumber.value = it },
                actionBar = {
                    AppFormActionBar(
                        primaryText = stringResource(R.string.guardar),
                        onPrimaryClick = {
                            profileViewModel.updateProfile(
                                nombre = name.value,
                                newPhotoUri = newPhotoUri,
                                currentImagePath = currentImagePath,
                                birthDate = birthDate.value,
                                sex = sex.value,
                                bloodType = bloodType.value,
                                allergies = allergies.value,
                                medicalNotes = medicalNotes.value,
                                healthCardNumber = healthCardNumber.value,
                                onSaved = {
                                    onSaveClicked()
                                    currentMode = ProfileMode.View
                                }
                            )
                        },
                        primaryEnabled = name.value.isNotBlank(),
                        supportingText = if (name.value.isBlank()) {
                            stringResource(R.string.profile_save_name_required)
                        } else {
                            null
                        },
                    )
                }
            )
        }

        ProfileMode.View -> {
            ProfileForm(
                modifier = modifier,
                enabled = false,
                hideEmptyFields = true,
                name = name.value,
                onNameChange = {},
                photoUri = selectedPhotoUri,
                onPhotoSelected = {},
                birthDate = birthDate.value,
                onBirthDateChange = {},
                sex = sex.value,
                onSexChange = {},
                bloodType = bloodType.value,
                onBloodTypeChange = {},
                allergies = allergies.value,
                onAllergiesChange = {},
                medicalNotes = medicalNotes.value,
                onMedicalNotesChange = {},
                healthCardNumber = healthCardNumber.value,
                onHealthCardNumberChange = {},
                footer = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppButton(
                            text = stringResource(R.string.profile_view_adherence),
                            onClick = { showAdherencePlaceholder = true },
                            size = ButtonSize.Large
                        )
                        Text(
                            text = stringResource(R.string.profile_adherence_soon),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actionBar = {
                    AppFormActionBar(
                        primaryText = stringResource(R.string.editar),
                        onPrimaryClick = { currentMode = ProfileMode.Edit },
                        secondaryIcon = Icons.Rounded.Delete,
                        secondaryContentDescription = stringResource(R.string.eliminar),
                        onSecondaryClick = { perfil?.let { showDeleteDialog = true } },
                    )
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.profile_delete_title)) },
            text = { Text(stringResource(R.string.profile_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        perfil?.let {
                            profileViewModel.deleteProfile(it) {
                                onDeletedClicked()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.eliminar)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showAdherencePlaceholder) {
        AlertDialog(
            onDismissRequest = { showAdherencePlaceholder = false },
            title = { Text(stringResource(R.string.profile_adherence_placeholder_title)) },
            text = { Text(stringResource(R.string.profile_adherence_placeholder_message)) },
            confirmButton = {
                TextButton(onClick = { showAdherencePlaceholder = false }) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }
}

/**
 * Formulario compartido por los modos de creación, edición y visualización.
 *
 * En el modo de visualización los campos se muestran deshabilitados, igual que
 * en la pantalla de medicamentos.
 */
@Composable
private fun ProfileForm(
    modifier: Modifier,
    enabled: Boolean,
    hideEmptyFields: Boolean = false,
    name: String,
    onNameChange: (String) -> Unit,
    photoUri: Uri?,
    onPhotoSelected: (Uri?) -> Unit,
    birthDate: LocalDate?,
    onBirthDateChange: (LocalDate) -> Unit,
    sex: Sex?,
    onSexChange: (Sex?) -> Unit,
    bloodType: BloodType?,
    onBloodTypeChange: (BloodType?) -> Unit,
    allergies: String,
    onAllergiesChange: (String) -> Unit,
    medicalNotes: String,
    onMedicalNotesChange: (String) -> Unit,
    healthCardNumber: String,
    onHealthCardNumberChange: (String) -> Unit,
    actionBar: @Composable () -> Unit,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    val notSpecified = stringResource(R.string.profile_not_specified_short)

    val sexFemale = stringResource(R.string.profile_sex_female)
    val sexMale = stringResource(R.string.profile_sex_male)
    val sexOther = stringResource(R.string.profile_sex_other)
    val sexPreferNot = stringResource(R.string.profile_sex_prefer_not_to_say)
    val sexOptions = listOf(notSpecified, sexFemale, sexMale, sexOther, sexPreferNot)
    val sexByLabel = mapOf(
        notSpecified to null,
        sexFemale to Sex.FEMALE,
        sexMale to Sex.MALE,
        sexOther to Sex.OTHER,
        sexPreferNot to Sex.PREFER_NOT_TO_SAY
    )

    val bloodAPositive = stringResource(R.string.profile_blood_a_positive)
    val bloodANegative = stringResource(R.string.profile_blood_a_negative)
    val bloodBPositive = stringResource(R.string.profile_blood_b_positive)
    val bloodBNegative = stringResource(R.string.profile_blood_b_negative)
    val bloodABPositive = stringResource(R.string.profile_blood_ab_positive)
    val bloodABNegative = stringResource(R.string.profile_blood_ab_negative)
    val bloodOPositive = stringResource(R.string.profile_blood_o_positive)
    val bloodONegative = stringResource(R.string.profile_blood_o_negative)
    val bloodOptions = listOf(
        notSpecified,
        bloodAPositive,
        bloodANegative,
        bloodBPositive,
        bloodBNegative,
        bloodABPositive,
        bloodABNegative,
        bloodOPositive,
        bloodONegative
    )
    val bloodByLabel = mapOf(
        notSpecified to null,
        bloodAPositive to BloodType.A_POSITIVE,
        bloodANegative to BloodType.A_NEGATIVE,
        bloodBPositive to BloodType.B_POSITIVE,
        bloodBNegative to BloodType.B_NEGATIVE,
        bloodABPositive to BloodType.AB_POSITIVE,
        bloodABNegative to BloodType.AB_NEGATIVE,
        bloodOPositive to BloodType.O_POSITIVE,
        bloodONegative to BloodType.O_NEGATIVE
    )

    val ageText = birthDate?.let { date ->
        val age = Period.between(date, LocalDate.now()).years.coerceAtLeast(0)
        pluralStringResource(R.plurals.profile_age, age, age)
    }
    val birthDateDisplay = listOfNotNull(
        birthDate?.format(
            DateTimeFormatter.ofPattern(stringResource(R.string.common_date_pattern))
        ),
        ageText,
    ).joinToString(" · ")
    val sexValue = sex?.let { stringResource(it.labelRes()) } ?: notSpecified
    val bloodTypeValue = bloodType?.let { stringResource(it.labelRes()) } ?: notSpecified

    val showBirthDate = !hideEmptyFields || birthDate != null
    val showSex = !hideEmptyFields || sex != null
    val showBloodType = !hideEmptyFields || bloodType != null
    val showAllergies = !hideEmptyFields || allergies.isNotBlank()
    val showMedicalNotes = !hideEmptyFields || medicalNotes.isNotBlank()
    val showHealthCard = !hideEmptyFields || healthCardNumber.isNotBlank()

    AppFormScaffold(
        modifier = modifier,
        scrollState = rememberScrollState(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        actionBar = actionBar,
    ) {
        AppPhotoPicker(
            photoUri = photoUri,
            onPhotoSelected = { uri -> onPhotoSelected(uri) },
            enabled = enabled,
            placeholderIcon = if (enabled) Icons.Default.AddAPhoto else Icons.Default.Person,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (!enabled) {
            AppInfoCard(title = stringResource(R.string.profile_personal_data)) {
                AppInfoRow(
                    label = stringResource(R.string.nombre),
                    value = name,
                )
                if (showBirthDate) {
                    AppInfoRow(
                        label = stringResource(R.string.profile_birth_date),
                        value = birthDateDisplay,
                    )
                }
                if (showSex) {
                    AppInfoRow(
                        label = stringResource(R.string.profile_sex),
                        value = sexValue,
                    )
                }
            }

            if (showBloodType || showAllergies || showMedicalNotes) {
                AppInfoCard(title = stringResource(R.string.profile_medical_info)) {
                    if (showBloodType) {
                        AppInfoRow(
                            label = stringResource(R.string.profile_blood_type),
                            value = bloodTypeValue,
                        )
                    }
                    if (showAllergies) {
                        AppInfoRow(
                            label = stringResource(R.string.profile_allergies),
                            value = allergies,
                        )
                    }
                    if (showMedicalNotes) {
                        AppInfoRow(
                            label = stringResource(R.string.profile_medical_notes),
                            value = medicalNotes,
                        )
                    }
                }
            }

            if (showHealthCard) {
                AppInfoCard(title = stringResource(R.string.profile_admin_data)) {
                    AppInfoRow(
                        label = stringResource(R.string.profile_health_card),
                        value = healthCardNumber,
                    )
                }
            }
        } else {
            AppFormSection(title = stringResource(R.string.profile_personal_data)) {
            if (enabled) {
                AppFormField(label = stringResource(R.string.nombre)) {
                    AppTextField(
                        value = name,
                        onValueChange = onNameChange,
                        placeholder = stringResource(R.string.profile_name_placeholder),
                    )
                }
            } else {
                AppReadOnlyField(
                    label = stringResource(R.string.nombre),
                    value = name,
                )
            }
            if (showBirthDate || showSex) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    if (showBirthDate) {
                        if (enabled) {
                            AppFormField(
                                label = stringResource(R.string.profile_birth_date),
                                modifier = Modifier.weight(1f)
                            ) {
                                AppDatePickerField(
                                    value = birthDate,
                                    onDateSelected = onBirthDateChange,
                                    maxDate = LocalDate.now()
                                )
                                ageText?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            AppReadOnlyField(
                                label = stringResource(R.string.profile_birth_date),
                                value = birthDateDisplay,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    if (showSex) {
                        if (enabled) {
                            AppFormField(
                                label = stringResource(R.string.profile_sex),
                                modifier = Modifier.weight(1f)
                            ) {
                                AppDropdownField(
                                    value = sexValue,
                                    options = sexOptions,
                                    onOptionSelected = { label -> onSexChange(sexByLabel[label]) },
                                )
                            }
                        } else {
                            AppReadOnlyField(
                                label = stringResource(R.string.profile_sex),
                                value = sexValue,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        if (showBloodType || showAllergies || showMedicalNotes) {
            AppFormSection(title = stringResource(R.string.profile_medical_info)) {
                if (showBloodType) {
                    if (enabled) {
                        AppFormField(label = stringResource(R.string.profile_blood_type)) {
                            AppDropdownField(
                                value = bloodTypeValue,
                                options = bloodOptions,
                                onOptionSelected = { label -> onBloodTypeChange(bloodByLabel[label]) },
                            )
                        }
                    } else {
                        AppReadOnlyField(
                            label = stringResource(R.string.profile_blood_type),
                            value = bloodTypeValue,
                        )
                    }
                }
                if (showAllergies) {
                    if (enabled) {
                        AppFormField(label = stringResource(R.string.profile_allergies)) {
                            AppTextField(
                                value = allergies,
                                onValueChange = onAllergiesChange,
                                placeholder = stringResource(R.string.profile_allergies_placeholder),
                                singleLine = false,
                                minLines = 2
                            )
                        }
                    } else {
                        AppReadOnlyField(
                            label = stringResource(R.string.profile_allergies),
                            value = allergies,
                        )
                    }
                }
                if (showMedicalNotes) {
                    if (enabled) {
                        AppFormField(label = stringResource(R.string.profile_medical_notes)) {
                            AppTextField(
                                value = medicalNotes,
                                onValueChange = onMedicalNotesChange,
                                placeholder = stringResource(R.string.profile_medical_notes_placeholder),
                                singleLine = false,
                                minLines = 3
                            )
                        }
                    } else {
                        AppReadOnlyField(
                            label = stringResource(R.string.profile_medical_notes),
                            value = medicalNotes,
                        )
                    }
                }
            }
        }

        if (showHealthCard) {
            AppFormSection(title = stringResource(R.string.profile_admin_data)) {
                if (enabled) {
                    AppFormField(label = stringResource(R.string.profile_health_card)) {
                        AppTextField(
                            value = healthCardNumber,
                            onValueChange = onHealthCardNumberChange,
                            placeholder = stringResource(R.string.profile_health_card_placeholder),
                        )
                    }
                } else {
                    AppReadOnlyField(
                        label = stringResource(R.string.profile_health_card),
                        value = healthCardNumber,
                    )
                }
            }
        }
        }

        Text(
            text = stringResource(R.string.profile_privacy_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        footer()
    }
}
