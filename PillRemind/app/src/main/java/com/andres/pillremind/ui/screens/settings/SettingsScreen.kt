package com.andres.pillremind.ui.screens.settings

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.ThemeMode
import com.andres.pillremind.notifications.ExactAlarmStatus
import com.andres.pillremind.notifications.NotificationAction
import com.andres.pillremind.notifications.NotificationStatus
import com.andres.pillremind.notifications.POST_NOTIFICATIONS_PERMISSION
import com.andres.pillremind.notifications.ReminderPermissions
import com.andres.pillremind.notifications.appDetailsIntent
import com.andres.pillremind.notifications.exactAlarmSettingsIntent
import com.andres.pillremind.notifications.notificationSettingsIntent
import com.andres.pillremind.notifications.resolveNotificationAction
import com.andres.pillremind.notifications.shouldShowNotificationRationale
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.settings.SettingsCard
import com.andres.pillremind.ui.components.settings.SettingsLinkItem
import com.andres.pillremind.ui.components.settings.SettingsRadioItem
import com.andres.pillremind.ui.screens.main.MainTopBar
import com.andres.pillremind.ui.screens.main.TopBarState
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Pantalla de ajustes de la aplicación.
 *
 * Permite elegir el tema, consultar el estado de los permisos de notificaciones
 * y alarmas exactas, y exportar o importar una copia de seguridad local. Los
 * datos de la aplicación son siempre locales.
 *
 * @param navController Controlador de navegación para volver a la pantalla anterior.
 * @param topLevelRoutes Rutas principales que no muestran flecha de retroceso.
 * @param settingsViewModel ViewModel encargado de la lógica de ajustes.
 */
@Composable
fun SettingsScreen(
    navController: NavHostController,
    topLevelRoutes: Set<String>,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {

    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val backupState by settingsViewModel.backupState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }

    val reminderPermissions = remember(context) { ReminderPermissions(context) }
    var notificationStatus by remember {
        mutableStateOf(reminderPermissions.notificationStatus())
    }
    var exactAlarmStatus by remember {
        mutableStateOf(reminderPermissions.exactAlarmStatus())
    }
    var notificationRationale by remember {
        mutableStateOf(shouldShowNotificationRationale(activity))
    }
    var notificationRequested by rememberSaveable { mutableStateOf(false) }
    val notificationAction = resolveNotificationAction(
        status = notificationStatus,
        requested = notificationRequested,
        showRationale = notificationRationale,
    )

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        notificationRequested = true
        notificationStatus = reminderPermissions.notificationStatus()
        notificationRationale = shouldShowNotificationRationale(activity)
    }

    val notificationStatusRes = when (notificationStatus) {
        NotificationStatus.GRANTED -> R.string.settings_notifications_granted
        NotificationStatus.REQUESTABLE -> R.string.settings_notifications_pending
        NotificationStatus.BLOCKED -> R.string.settings_notifications_blocked
    }
    val notificationActionLabelRes: Int? = when (notificationAction) {
        NotificationAction.REQUEST -> R.string.settings_notifications_allow
        NotificationAction.OPEN_SETTINGS -> R.string.settings_notifications_open_settings
        NotificationAction.NONE -> null
    }
    val exactAlarmStatusRes = when (exactAlarmStatus) {
        ExactAlarmStatus.GRANTED -> R.string.settings_exact_alarms_granted
        ExactAlarmStatus.PENDING -> R.string.settings_exact_alarms_disabled
        ExactAlarmStatus.UNAVAILABLE -> R.string.onboarding_status_unavailable
        ExactAlarmStatus.NOT_REQUIRED -> R.string.onboarding_status_not_required
    }
    val exactAlarmActionLabelRes: Int? = when (exactAlarmStatus) {
        ExactAlarmStatus.PENDING -> R.string.settings_allow_exact_alarms
        ExactAlarmStatus.UNAVAILABLE -> R.string.settings_open_app_settings
        else -> null
    }

    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var showImportConfirmation by rememberSaveable { mutableStateOf(false) }
    var showImportPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showImportSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var importIsEncrypted by rememberSaveable { mutableStateOf(false) }
    var pendingImportUri by rememberSaveable { mutableStateOf<String?>(null) }
    var legalDocument by rememberSaveable { mutableStateOf<LegalDocument?>(null) }

    val busy = backupState is BackupUiState.Working ||
            backupState is BackupUiState.Inspecting

    val exportedMessage = stringResource(R.string.backup_export_success)
    val genericErrorMessage = stringResource(R.string.backup_error_generic)
    val errorMessage = when (val state = backupState) {
        is BackupUiState.Error -> stringResource(state.messageRes)
        else -> null
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            settingsViewModel.exportBackup(uri)
        } else {
            settingsViewModel.cancelPendingExport()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri.toString()
            settingsViewModel.inspectBackup(uri)
        }
    }

    LaunchedEffect(backupState) {
        when (val state = backupState) {
            is BackupUiState.AwaitingConfirmation -> {
                importIsEncrypted = state.encrypted
                showImportConfirmation = true
                settingsViewModel.consumeBackupState()
            }

            is BackupUiState.Exported -> {
                snackbarHostState.showSnackbar(exportedMessage)
                settingsViewModel.consumeBackupState()
            }

            is BackupUiState.Imported -> {
                showImportSuccessDialog = true
            }

            is BackupUiState.Error -> {
                snackbarHostState.showSnackbar(errorMessage ?: genericErrorMessage)
                settingsViewModel.consumeBackupState()
            }

            else -> Unit
        }
    }

    // Al volver de los ajustes del sistema se refrescan ambos permisos.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationStatus = reminderPermissions.notificationStatus()
        exactAlarmStatus = reminderPermissions.exactAlarmStatus()
        notificationRationale = shouldShowNotificationRationale(activity)
    }

    Scaffold(
        topBar = {
            MainTopBar(
                state = TopBarState(title = stringResource(R.string.settings_title)),
                navController = navController,
                topLevelRoutes = topLevelRoutes
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {

            // Tarjeta de apariencia
            SettingsCard(title = stringResource(R.string.settings_theme_title)) {
                Column(modifier = Modifier.selectableGroup()) {
                    ThemeMode.entries.forEach { mode ->
                        SettingsRadioItem(
                            label = stringResource(mode.labelRes()),
                            description = mode.descriptionRes()?.let { stringResource(it) },
                            selected = themeMode == mode,
                            onClick = { settingsViewModel.setThemeMode(mode) },
                        )
                    }
                }
            }

            // Estado de los permisos de recordatorios.
            SettingsCard(title = stringResource(R.string.settings_reminders_title)) {
                ReminderPermissionRow(
                    title = stringResource(R.string.settings_notifications_title),
                    description = stringResource(R.string.settings_notifications_description),
                    statusText = stringResource(notificationStatusRes),
                    actionLabel = notificationActionLabelRes?.let { stringResource(it) },
                    onAction = {
                        when (notificationAction) {
                            NotificationAction.REQUEST ->
                                notificationLauncher.launch(POST_NOTIFICATIONS_PERMISSION)

                            NotificationAction.OPEN_SETTINGS ->
                                runCatching {
                                    context.startActivity(notificationSettingsIntent(context))
                                }

                            NotificationAction.NONE -> Unit
                        }
                    },
                )

                if (exactAlarmStatus != ExactAlarmStatus.NOT_REQUIRED) {
                    Spacer(Modifier.height(AppSpacing.md))
                    ReminderPermissionRow(
                        title = stringResource(R.string.settings_exact_alarms_title),
                        description = stringResource(R.string.settings_exact_alarms_description),
                        statusText = stringResource(exactAlarmStatusRes),
                        actionLabel = exactAlarmActionLabelRes?.let { stringResource(it) },
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = exactAlarmSettingsIntent(context)
                                val opened = intent != null &&
                                    runCatching { context.startActivity(intent) }.isSuccess
                                if (!opened) {
                                    runCatching {
                                        context.startActivity(appDetailsIntent(context))
                                    }
                                }
                            }
                        },
                    )
                }
            }

            // Copia de seguridad local
            SettingsCard(title = stringResource(R.string.settings_data_title)) {
                Text(
                    text = stringResource(R.string.settings_data_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                SettingsLinkItem(
                    label = stringResource(R.string.settings_export_data),
                    onClick = { if (!busy) showExportDialog = true },
                    enabled = !busy
                )
                SettingsLinkItem(
                    label = stringResource(R.string.settings_import_data),
                    onClick = { if (!busy) importLauncher.launch(arrayOf("*/*")) },
                    enabled = !busy
                )
                if (busy) {
                    Spacer(Modifier.height(AppSpacing.xs))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(R.string.settings_backup_in_progress),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.settings_backup_privacy_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tarjeta legal
            SettingsCard(title = stringResource(R.string.settings_legal_title)) {
                SettingsLinkItem(
                    label = stringResource(R.string.settings_privacy_policy),
                    onClick = { legalDocument = LegalDocument.PrivacyPolicy }
                )
                SettingsLinkItem(
                    label = stringResource(R.string.settings_legal_notice),
                    onClick = { legalDocument = LegalDocument.LegalNotice }
                )
            }
        }
    }

    if (showExportDialog) {
        ExportBackupDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = { password ->
                showExportDialog = false
                if (settingsViewModel.prepareExport(password?.toCharArray())) {
                    exportLauncher.launch(settingsViewModel.suggestedBackupFileName())
                }
            }
        )
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmation = false
                pendingImportUri = null
                settingsViewModel.consumeBackupState()
            },
            title = { Text(stringResource(R.string.backup_import_title)) },
            text = {
                Text(stringResource(R.string.backup_import_warning))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmation = false
                        val uri = pendingImportUri?.toUri()
                        when {
                            uri == null -> settingsViewModel.consumeBackupState()
                            importIsEncrypted -> showImportPasswordDialog = true
                            else -> settingsViewModel.importBackup(uri, null)
                        }
                    }
                ) {
                    Text(stringResource(R.string.backup_continue))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportConfirmation = false
                        pendingImportUri = null
                        settingsViewModel.consumeBackupState()
                    }
                ) {
                    Text(stringResource(R.string.backup_cancel))
                }
            }
        )
    }

    if (showImportPasswordDialog) {
        ImportPasswordDialog(
            onDismiss = {
                showImportPasswordDialog = false
                pendingImportUri = null
                settingsViewModel.consumeBackupState()
            },
            onConfirm = { password ->
                showImportPasswordDialog = false
                val uri = pendingImportUri?.toUri()
                if (uri != null) {
                    settingsViewModel.importBackup(uri, password.toCharArray())
                } else {
                    settingsViewModel.consumeBackupState()
                }
            }
        )
    }

    if (showImportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* La copia ya está aplicada; solo se cierra con el botón. */ },
            title = { Text(stringResource(R.string.backup_import_success_title)) },
            text = {
                Text(stringResource(R.string.backup_import_success_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportSuccessDialog = false
                        pendingImportUri = null
                        settingsViewModel.consumeBackupState()
                        navController.popBackStack()
                    }
                ) {
                    Text(stringResource(R.string.backup_continue))
                }
            }
        )
    }

    legalDocument?.let { document ->
        LegalDocumentDialog(
            title = stringResource(document.titleRes),
            body = stringResource(document.bodyRes),
            onDismiss = { legalDocument = null }
        )
    }
}

/**
 * Documentos legales que se muestran dentro de la app.
 *
 * Se mantienen en local para que funcionen sin conexión y no dependan de una
 * página externa.
 *
 * @property titleRes Título del documento.
 * @property bodyRes Texto completo del documento.
 */
private enum class LegalDocument(
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int
) {
    PrivacyPolicy(R.string.settings_privacy_policy, R.string.settings_privacy_policy_body),
    LegalNotice(R.string.settings_legal_notice, R.string.settings_legal_notice_body)
}

/**
 * Diálogo de lectura para un documento legal.
 *
 * @param title Título mostrado en la cabecera.
 * @param body Texto completo del documento.
 * @param onDismiss Acción al cerrar el diálogo.
 */
@Composable
private fun LegalDocumentDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Text(
                text = body,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_legal_close))
            }
        }
    )
}

/**
 * Diálogo para elegir si la copia se protege con contraseña.
 *
 * @param onDismiss Acción al cancelar.
 * @param onConfirm Recibe la contraseña, o `null` si la copia no se cifra.
 */
@Composable
private fun ExportBackupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var protectWithPassword by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var errorRes by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_export_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Text(
                    stringResource(R.string.backup_export_description)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = protectWithPassword,
                        onCheckedChange = {
                            protectWithPassword = it
                            errorRes = null
                        }
                    )
                    Text(stringResource(R.string.backup_protect_with_password))
                }
                if (protectWithPassword) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorRes = null
                        },
                        label = { Text(stringResource(R.string.backup_password_min_length)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    OutlinedTextField(
                        value = confirmation,
                        onValueChange = {
                            confirmation = it
                            errorRes = null
                        },
                        label = { Text(stringResource(R.string.backup_repeat_password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Text(
                        text = stringResource(R.string.backup_password_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                errorRes?.let {
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (protectWithPassword) {
                        when {
                            password.length < 8 -> {
                                errorRes = R.string.backup_password_too_short
                            }

                            password != confirmation -> {
                                errorRes = R.string.backup_passwords_mismatch
                            }

                            else -> onConfirm(password)
                        }
                    } else {
                        onConfirm(null)
                    }
                }
            ) {
                Text(stringResource(R.string.backup_export_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.backup_cancel))
            }
        }
    )
}

/**
 * Diálogo para introducir la contraseña de una copia cifrada.
 *
 * @param onDismiss Acción al cancelar.
 * @param onConfirm Recibe la contraseña introducida.
 */
@Composable
private fun ImportPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var errorRes by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(stringResource(R.string.backup_password_description))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorRes = null
                    },
                    label = { Text(stringResource(R.string.backup_password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                errorRes?.let {
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (password.isBlank()) {
                        errorRes = R.string.backup_password_required
                    } else {
                        onConfirm(password)
                    }
                }
            ) {
                Text(stringResource(R.string.backup_import_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.backup_cancel))
            }
        }
    )
}

/**
 * Fila con el estado y la acción de un permiso de recordatorios.
 *
 * @param title Nombre del permiso.
 * @param description Explicación breve de para qué se usa.
 * @param statusText Estado actual ya traducido.
 * @param actionLabel Texto del botón, o `null` si no hace falta acción.
 * @param onAction Acción para solicitar el permiso o abrir los ajustes.
 */
@Composable
private fun ReminderPermissionRow(
    title: String,
    description: String,
    statusText: String,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(AppSpacing.xxs))
    Text(
        text = statusText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(AppSpacing.xxs))
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    actionLabel?.let { label ->
        Spacer(Modifier.height(AppSpacing.sm))
        AppButton(
            text = label,
            onClick = onAction,
            size = ButtonSize.Large,
        )
    }
}

/**
 * Etiqueta del selector de tema para cada modo.
 */
@StringRes
private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

/**
 * Descripción opcional del selector de tema.
 */
@StringRes
private fun ThemeMode.descriptionRes(): Int? = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system_description
    ThemeMode.LIGHT -> null
    ThemeMode.DARK -> null
}
