package com.andres.pillremind.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.BloodType
import com.andres.pillremind.domain.model.Sex
import com.andres.pillremind.ui.components.AppDatePickerField
import com.andres.pillremind.ui.components.AppDetailRow
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormActions
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.AppFormSection
import com.andres.pillremind.ui.components.AppNoticeBlock
import com.andres.pillremind.ui.components.AppPhotoPicker
import com.andres.pillremind.ui.components.AppReadOnlySection
import com.andres.pillremind.ui.components.AppScrollWithFooter
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.labelRes
import com.andres.pillremind.ui.screens.main.TopBarAction
import com.andres.pillremind.ui.screens.main.TopBarState
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
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
    val backContentDescription = stringResource(R.string.common_back)
    val editContentDescription = stringResource(R.string.editar)

    LaunchedEffect(currentMode, topBarTitle, editContentDescription) {
        onTopBarChange(
            TopBarState(
                title = topBarTitle,
                actions = if (currentMode == ProfileMode.View) {
                    listOf(
                        TopBarAction(
                            icon = Icons.Rounded.Edit,
                            contentDescription = editContentDescription,
                            onClick = { currentMode = ProfileMode.Edit }
                        )
                    )
                } else {
                    emptyList()
                },
                navigationAction = if (currentMode == ProfileMode.Edit) {
                    TopBarAction(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backContentDescription,
                        onClick = { currentMode = ProfileMode.View }
                    )
                } else {
                    null
                }
            )
        )
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
                footer = {
                    AppFormActions(
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
                footer = {
                    AppFormActions(
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
            ProfileViewContent(
                modifier = modifier,
                name = name.value,
                photoUri = selectedPhotoUri,
                birthDate = birthDate.value,
                sex = sex.value,
                bloodType = bloodType.value,
                allergies = allergies.value,
                medicalNotes = medicalNotes.value,
                healthCardNumber = healthCardNumber.value,
                onEditClick = { currentMode = ProfileMode.Edit },
                onDeleteClick = { perfil?.let { showDeleteDialog = true } },
                onAdherenceClick = { showAdherencePlaceholder = true },
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
 * Formulario compartido por los modos de creación y edición.
 */
@Composable
private fun ProfileForm(
    modifier: Modifier,
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
    val sexValue = sex?.let { stringResource(it.labelRes()) } ?: notSpecified
    val bloodTypeValue = bloodType?.let { stringResource(it.labelRes()) } ?: notSpecified

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        AppPhotoPicker(
            photoUri = photoUri,
            onPhotoSelected = { uri -> onPhotoSelected(uri) },
            enabled = true,
            placeholderIcon = Icons.Default.AddAPhoto,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        AppFormSection(title = stringResource(R.string.profile_personal_data)) {
            AppFormField(label = stringResource(R.string.nombre)) {
                AppTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = stringResource(R.string.profile_name_placeholder),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
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
            }
        }

        AppFormSection(title = stringResource(R.string.profile_medical_info)) {
            AppFormField(label = stringResource(R.string.profile_blood_type)) {
                AppDropdownField(
                    value = bloodTypeValue,
                    options = bloodOptions,
                    onOptionSelected = { label -> onBloodTypeChange(bloodByLabel[label]) },
                )
            }
            AppFormField(label = stringResource(R.string.profile_allergies)) {
                AppTextField(
                    value = allergies,
                    onValueChange = onAllergiesChange,
                    placeholder = stringResource(R.string.profile_allergies_placeholder),
                    singleLine = false,
                    minLines = 2
                )
            }
            AppFormField(label = stringResource(R.string.profile_medical_notes)) {
                AppTextField(
                    value = medicalNotes,
                    onValueChange = onMedicalNotesChange,
                    placeholder = stringResource(R.string.profile_medical_notes_placeholder),
                    singleLine = false,
                    minLines = 3
                )
            }
        }

        AppFormSection(title = stringResource(R.string.profile_admin_data)) {
            AppFormField(label = stringResource(R.string.profile_health_card)) {
                AppTextField(
                    value = healthCardNumber,
                    onValueChange = onHealthCardNumberChange,
                    placeholder = stringResource(R.string.profile_health_card_placeholder),
                )
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

/**
 * Ficha de solo lectura de un perfil.
 *
 * Sustituye al formulario deshabilitado: cabecera con foto y datos básicos,
 * secciones planas sin cards y acciones al final del contenido.
 */
@Composable
private fun ProfileViewContent(
    modifier: Modifier = Modifier,
    name: String,
    photoUri: Uri?,
    birthDate: LocalDate?,
    sex: Sex?,
    bloodType: BloodType?,
    allergies: String,
    medicalNotes: String,
    healthCardNumber: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAdherenceClick: () -> Unit,
) {
    val datePattern = stringResource(R.string.common_date_pattern)
    val dateFormatter = remember(datePattern) { DateTimeFormatter.ofPattern(datePattern) }
    val birthDateValue = birthDate?.format(dateFormatter)
    val ageText = birthDate?.let { date ->
        val age = Period.between(date, LocalDate.now()).years.coerceAtLeast(0)
        pluralStringResource(R.plurals.profile_age, age, age)
    }
    val sexValue = sex?.let { stringResource(it.labelRes()) }
    val subtitle = listOfNotNull(ageText, sexValue).joinToString(" · ")

    val showAllergies = allergies.isNotBlank()
    val showNotes = medicalNotes.isNotBlank()

    AppScrollWithFooter(
        modifier = modifier,
        footer = {
            AppFormActions(
                primaryText = stringResource(R.string.editar),
                onPrimaryClick = onEditClick,
                secondaryIcon = Icons.Rounded.Delete,
                secondaryContentDescription = stringResource(R.string.eliminar),
                secondaryContentColor = MaterialTheme.colorScheme.error,
                onSecondaryClick = onDeleteClick,
            )
        },
    ) {
        ProfileHeader(
            name = name,
            photoUri = photoUri,
            subtitle = subtitle,
        )

        if (birthDateValue != null) {
            AppReadOnlySection(title = stringResource(R.string.profile_personal_data)) {
                AppDetailRow(
                    label = stringResource(R.string.profile_birth_date),
                    value = birthDateValue,
                    showDivider = false,
                )
            }
        }

        if (bloodType != null || showAllergies || showNotes) {
            AppReadOnlySection(title = stringResource(R.string.profile_medical_info)) {
                if (bloodType != null) {
                    AppDetailRow(
                        label = stringResource(R.string.profile_blood_type),
                        value = stringResource(bloodType.labelRes()),
                        showDivider = showAllergies || showNotes,
                    )
                }
                if (showAllergies) {
                    AppNoticeBlock(
                        icon = Icons.Rounded.Warning,
                        title = stringResource(R.string.profile_allergies),
                        text = allergies,
                        modifier = Modifier.padding(
                            top = AppSpacing.sm,
                            bottom = if (showNotes) AppSpacing.sm else 0.dp,
                        ),
                    )
                }
                if (showNotes) {
                    AppDetailRow(
                        label = stringResource(R.string.profile_medical_notes),
                        value = medicalNotes,
                        stacked = true,
                        showDivider = false,
                    )
                }
            }
        }

        if (healthCardNumber.isNotBlank()) {
            AppReadOnlySection(title = stringResource(R.string.profile_admin_data)) {
                AppDetailRow(
                    label = stringResource(R.string.profile_health_card),
                    value = healthCardNumber,
                    showDivider = false,
                )
            }
        }

        AppReadOnlySection(title = stringResource(R.string.profile_tracking_section)) {
            AdherenceRow(onClick = onAdherenceClick)
        }
    }
}

/**
 * Cabecera de la ficha de un perfil.
 */
@Composable
private fun ProfileHeader(
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
            placeholderIcon = Icons.Default.Person,
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

/**
 * Fila que abre el aviso de adherencia.
 */
@Composable
private fun AdherenceRow(onClick: () -> Unit) {
    val label = stringResource(R.string.profile_view_adherence)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = label, onClick = onClick)
            .padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.BarChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.profile_adherence_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true, name = "Ficha de perfil")
@Composable
private fun ProfileViewContentPreview() {
    PillRemindTheme {
        ProfileViewContent(
            name = "María Rodríguez",
            photoUri = null,
            birthDate = LocalDate.of(1958, 3, 12),
            sex = Sex.FEMALE,
            bloodType = BloodType.A_POSITIVE,
            allergies = "Penicilina y polen",
            medicalNotes = "Hipertensión en tratamiento. Revisión de tensión cada 6 meses.",
            healthCardNumber = "1234 5678 9012",
            onEditClick = {},
            onDeleteClick = {},
            onAdherenceClick = {},
        )
    }
}
