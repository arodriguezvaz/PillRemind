package com.andres.pillremind.ui.screens.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.settings.SettingsCard
import com.andres.pillremind.ui.components.settings.SettingsLinkItem
import com.andres.pillremind.ui.components.settings.SettingsToggleItem
import com.andres.pillremind.ui.screens.main.MainTopBar
import com.andres.pillremind.ui.screens.main.TopBarState

/**
 * Pantalla de ajustes de la aplicación.
 *
 * Permite gestionar el modo oscuro, el estado del permiso de alarmas exactas y
 * la exportación e importación de una copia de seguridad local. Los datos de la
 * aplicación son siempre locales.
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

    val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()
    val backupState by settingsViewModel.backupState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var exactAlarmsAllowed by remember { mutableStateOf(canScheduleExactAlarms(context)) }

    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var showImportConfirmation by rememberSaveable { mutableStateOf(false) }
    var showImportPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showImportSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var importIsEncrypted by rememberSaveable { mutableStateOf(false) }
    var pendingImportUri by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingExportUri by rememberSaveable { mutableStateOf<String?>(null) }

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
            pendingExportUri = uri.toString()
            showExportDialog = true
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

    // Al volver de los ajustes del sistema se refresca el estado del permiso.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        exactAlarmsAllowed = canScheduleExactAlarms(context)
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Tarjeta de apariencia
            SettingsCard {
                SettingsToggleItem(
                    label = stringResource(R.string.settings_dark_mode),
                    checked = isDarkMode,
                    onCheckedChange = { settingsViewModel.toggleDarkMode(it) }
                )
            }

            // Aviso de alarmas exactas: no se abre Ajustes automáticamente.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmsAllowed) {
                SettingsCard(title = stringResource(R.string.settings_reminders_title)) {
                    Text(
                        text = stringResource(R.string.settings_exact_alarms_disabled),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    AppButton(
                        text = stringResource(R.string.settings_allow_exact_alarms),
                        onClick = { openExactAlarmSettings(context) },
                        size = ButtonSize.Large
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
                    onClick = {
                        if (!busy) {
                            exportLauncher.launch(settingsViewModel.suggestedBackupFileName())
                        }
                    },
                    enabled = !busy
                )
                SettingsLinkItem(
                    label = stringResource(R.string.settings_import_data),
                    onClick = { if (!busy) importLauncher.launch(arrayOf("*/*")) },
                    enabled = !busy
                )
                if (busy) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                SettingsLinkItem(stringResource(R.string.settings_privacy_policy), { /* abrir url */ })
                SettingsLinkItem(stringResource(R.string.settings_terms), { /* abrir url */ })
                SettingsLinkItem(stringResource(R.string.settings_legal_notice), { /* abrir url */ })
            }
        }
    }

    if (showExportDialog) {
        ExportBackupDialog(
            onDismiss = {
                showExportDialog = false
                pendingExportUri?.toUri()?.let { settingsViewModel.discardExport(it) }
                pendingExportUri = null
            },
            onConfirm = { password ->
                showExportDialog = false
                val uri = pendingExportUri?.toUri()
                pendingExportUri = null
                if (uri != null) {
                    settingsViewModel.exportBackup(uri, password?.toCharArray())
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
 * Comprueba si la app puede programar alarmas exactas.
 *
 * En Android 11 y anteriores el permiso no existe, así que se considera concedido.
 */
private fun canScheduleExactAlarms(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
}

/**
 * Abre la pantalla del sistema para conceder el permiso de alarmas exactas.
 */
@RequiresApi(Build.VERSION_CODES.S)
private fun openExactAlarmSettings(context: Context) {
    val packageUri = "package:${context.packageName}".toUri()
    val exactAlarmIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri)

    runCatching { context.startActivity(exactAlarmIntent) }
        .onFailure {
            // Algunos fabricantes no exponen la pantalla anterior; se usa la ficha
            // de la app como alternativa.
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                )
            }
        }
}
