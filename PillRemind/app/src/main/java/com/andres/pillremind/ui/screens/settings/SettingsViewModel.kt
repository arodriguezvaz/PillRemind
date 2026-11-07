package com.andres.pillremind.ui.screens.settings

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.R
import com.andres.pillremind.data.backup.BackupError
import com.andres.pillremind.data.backup.BackupException
import com.andres.pillremind.data.backup.BackupFileStore
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.ThemeMode
import com.andres.pillremind.domain.model.UpdateStatus
import com.andres.pillremind.domain.repository.BackupRepository
import com.andres.pillremind.domain.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Estados del flujo de copia de seguridad en Ajustes.
 */
sealed interface BackupUiState {

    /** No hay ninguna operación en curso. */
    data object Idle : BackupUiState

    /** Se está comprobando si la copia requiere contraseña. */
    data object Inspecting : BackupUiState

    /** Se está exportando o importando. */
    data object Working : BackupUiState

    /**
     * La copia es válida y está pendiente de confirmación.
     *
     * @property encrypted Indica si hay que pedir contraseña.
     */
    data class AwaitingConfirmation(val encrypted: Boolean) : BackupUiState

    /** La exportación terminó correctamente. */
    data object Exported : BackupUiState

    /** La importación terminó correctamente. */
    data object Imported : BackupUiState

    /** La operación falló. */
    data class Error(@StringRes val messageRes: Int) : BackupUiState
}

/**
 * Estados de la comprobación manual de actualizaciones.
 */
sealed interface UpdateCheckUiState {

    /** No hay ninguna comprobación reciente que mostrar. */
    data object Idle : UpdateCheckUiState

    /** Se está consultando el manifiesto. */
    data object Checking : UpdateCheckUiState

    /** La versión instalada ya está al día. */
    data object UpToDate : UpdateCheckUiState

    /** Hay una versión nueva disponible. */
    data object Available : UpdateCheckUiState

    /** No se pudo consultar el manifiesto. */
    data object Error : UpdateCheckUiState
}

/**
 * ViewModel encargado de la gestión de la pantalla de ajustes.
 *
 * Gestiona la preferencia de tema y las operaciones de exportación e
 * importación de la copia de seguridad local.
 *
 * @property sessionManager Gestor de sesión del usuario activo.
 * @property backupRepository Repositorio de copias de seguridad.
 * @property backupFileStore Acceso a los documentos elegidos por el usuario.
 * @property updateRepository Repositorio de comprobación de actualizaciones.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val backupRepository: BackupRepository,
    private val backupFileStore: BackupFileStore,
    private val updateRepository: UpdateRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    internal companion object {
        /** Indica que hay una exportación esperando a que el usuario elija el archivo. */
        const val KEY_PENDING_EXPORT = "pending_export"

        /** Indica que la exportación pendiente debe cifrarse con contraseña. */
        const val KEY_PENDING_EXPORT_ENCRYPTED = "pending_export_encrypted"
    }

    private var pendingExportPassword: CharArray? = null

    val themeMode: StateFlow<ThemeMode> = sessionManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    private val _backupState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)

    /**
     * Estado actual del flujo de copia de seguridad.
     */
    val backupState: StateFlow<BackupUiState> = _backupState.asStateFlow()

    private val _updateCheckState =
        MutableStateFlow<UpdateCheckUiState>(UpdateCheckUiState.Idle)

    /**
     * Estado de la comprobación manual de actualizaciones.
     */
    val updateCheckState: StateFlow<UpdateCheckUiState> = _updateCheckState.asStateFlow()

    /**
     * Versión instalada, mostrada en la tarjeta «Acerca de».
     */
    val currentVersionName: String = updateRepository.installedAppInfo().let { info ->
        info.versionName.ifBlank { info.versionCode.toString() }
    }

    /**
     * Nombre sugerido para el archivo de exportación.
     */
    fun suggestedBackupFileName(): String = backupFileStore.suggestedFileName()

    /**
     * Guarda el tema elegido por el usuario.
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { sessionManager.setThemeMode(mode) }
    }

    /**
     * Comprueba la cabecera de la copia seleccionada para saber si está cifrada.
     */
    fun inspectBackup(uri: Uri) {
        if (isBusy()) return
        viewModelScope.launch {
            _backupState.value = BackupUiState.Inspecting
            try {
                val encrypted = withContext(Dispatchers.IO) {
                    backupFileStore.openInput(uri).use { input ->
                        backupRepository.isEncrypted(input)
                    }
                }
                _backupState.value = BackupUiState.AwaitingConfirmation(encrypted)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _backupState.value = BackupUiState.Error(e.userMessageRes())
            }
        }
    }

    /**
     * Guarda la contraseña de la próxima exportación antes de abrir el explorador.
     *
     * La contraseña se mantiene solo en memoria. El flag de cifrado se guarda en
     * [SavedStateHandle] para poder cancelar la exportación si el proceso muere
     * mientras el explorador está abierto.
     *
     * @param password Contraseña de cifrado, o `null` para exportar sin cifrar.
     * @return `true` si la exportación queda preparada.
     */
    fun prepareExport(password: CharArray?): Boolean {
        if (isBusy()) {
            password?.fill('\u0000')
            return false
        }

        clearPendingExport()
        pendingExportPassword = password
        savedStateHandle[KEY_PENDING_EXPORT] = true
        savedStateHandle[KEY_PENDING_EXPORT_ENCRYPTED] = password != null
        return true
    }

    /**
     * Exporta la copia al documento indicado usando la contraseña preparada.
     *
     * Si la copia debía ir cifrada y la contraseña se perdió (por ejemplo, por
     * muerte del proceso con el explorador abierto), se cancela la exportación
     * y se borra el archivo recién creado.
     *
     * @param uri Documento de destino.
     */
    fun exportBackup(uri: Uri) {
        if (isBusy()) return

        val prepared = savedStateHandle.get<Boolean>(KEY_PENDING_EXPORT) == true
        val encrypted = savedStateHandle.get<Boolean>(KEY_PENDING_EXPORT_ENCRYPTED) == true
        val password = pendingExportPassword

        if (!prepared || (encrypted && password == null)) {
            clearPendingExport()
            backupFileStore.delete(uri)
            _backupState.value = BackupUiState.Error(R.string.backup_error_export_restarted)
            return
        }

        viewModelScope.launch {
            _backupState.value = BackupUiState.Working
            try {
                withContext(Dispatchers.IO) {
                    backupFileStore.openOutput(uri).use { output ->
                        backupRepository.exportTo(output, password)
                    }
                }
                _backupState.value = BackupUiState.Exported
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                backupFileStore.delete(uri)
                _backupState.value = BackupUiState.Error(e.userMessageRes())
            } finally {
                clearPendingExport()
            }
        }
    }

    /**
     * Descarta la exportación pendiente cuando el usuario cancela el explorador.
     */
    fun cancelPendingExport() {
        clearPendingExport()
    }

    /**
     * Importa la copia seleccionada, reemplazando todos los datos actuales.
     *
     * @param uri Documento de origen.
     * @param password Contraseña de la copia, o `null` si no está cifrada.
     */
    fun importBackup(uri: Uri, password: CharArray?) {
        if (isBusy()) return
        viewModelScope.launch {
            _backupState.value = BackupUiState.Working
            try {
                withContext(Dispatchers.IO) {
                    backupFileStore.openInput(uri).use { input ->
                        backupRepository.importFrom(input, password)
                    }
                }
                _backupState.value = BackupUiState.Imported
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _backupState.value = BackupUiState.Error(e.userMessageRes())
            } finally {
                password?.fill('\u0000')
            }
        }
    }

    /**
     * Marca como consumido el estado actual para no repetir diálogos ni avisos.
     */
    fun consumeBackupState() {
        if (!isBusy()) {
            _backupState.value = BackupUiState.Idle
        }
    }

    /**
     * Comprueba manualmente si hay una actualización disponible.
     *
     * La comprobación automática se omite si ya se hizo en las últimas 24
     * horas; este método siempre consulta el manifiesto.
     */
    fun checkForUpdates() {
        if (_updateCheckState.value == UpdateCheckUiState.Checking) return

        viewModelScope.launch {
            _updateCheckState.value = UpdateCheckUiState.Checking
            _updateCheckState.value = try {
                val result = updateRepository.refresh()
                if (result.isSuccess) {
                    val info = result.getOrThrow()
                    val installed = updateRepository.installedAppInfo()
                    if (info.statusFor(installed.versionCode) is UpdateStatus.UpToDate) {
                        UpdateCheckUiState.UpToDate
                    } else {
                        // Una comprobación manual vuelve a mostrar el aviso aunque
                        // el usuario lo hubiera pospuesto antes.
                        updateRepository.clearDismissal()
                        UpdateCheckUiState.Available
                    }
                } else {
                    UpdateCheckUiState.Error
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UpdateCheckUiState.Error
            }
        }
    }

    /**
     * Marca como consumido el resultado de la comprobación de actualizaciones.
     */
    fun consumeUpdateCheckState() {
        if (_updateCheckState.value != UpdateCheckUiState.Checking) {
            _updateCheckState.value = UpdateCheckUiState.Idle
        }
    }

    /**
     * Borra la contraseña pendiente y los flags de exportación.
     */
    private fun clearPendingExport() {
        pendingExportPassword?.fill('\u0000')
        pendingExportPassword = null
        savedStateHandle.remove<Boolean>(KEY_PENDING_EXPORT)
        savedStateHandle.remove<Boolean>(KEY_PENDING_EXPORT_ENCRYPTED)
    }

    override fun onCleared() {
        clearPendingExport()
        super.onCleared()
    }

    private fun isBusy(): Boolean =
        _backupState.value is BackupUiState.Working ||
            _backupState.value is BackupUiState.Inspecting

    private fun Throwable.userMessageRes(): Int = when (this) {
        is BackupException -> when (error) {
            BackupError.INVALID_FILE -> R.string.backup_error_invalid_file
            BackupError.UNSUPPORTED_VERSION -> R.string.backup_error_unsupported_version
            BackupError.PASSWORD_REQUIRED -> R.string.backup_error_password_required
            BackupError.WRONG_PASSWORD -> R.string.backup_error_wrong_password
            BackupError.CORRUPTED -> R.string.backup_error_corrupted
            BackupError.TOO_LARGE -> R.string.backup_error_too_large
            BackupError.MISSING_IMAGES -> R.string.backup_error_missing_images
            BackupError.INVALID_DATA -> R.string.backup_error_invalid_data
            BackupError.IMAGES_ERROR -> R.string.backup_error_images
            BackupError.SAVE_ERROR -> R.string.backup_error_save
            BackupError.IO_ERROR -> R.string.backup_error_io
            BackupError.GENERIC -> R.string.backup_error_generic
        }

        else -> R.string.backup_error_generic
    }
}
