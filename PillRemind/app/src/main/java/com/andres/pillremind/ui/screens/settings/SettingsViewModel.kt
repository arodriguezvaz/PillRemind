package com.andres.pillremind.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.backup.BackupException
import com.andres.pillremind.data.backup.BackupFileStore
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.repository.BackupRepository
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
    data class Exported(val message: String) : BackupUiState

    /** La importación terminó correctamente. */
    data class Imported(val message: String) : BackupUiState

    /** La operación falló. */
    data class Error(val message: String) : BackupUiState
}

/**
 * ViewModel encargado de la gestión de la pantalla de ajustes.
 *
 * Gestiona la preferencia de modo oscuro y las operaciones de exportación e
 * importación de la copia de seguridad local.
 *
 * @property sessionManager Gestor de sesión del usuario activo.
 * @property backupRepository Repositorio de copias de seguridad.
 * @property backupFileStore Acceso a los documentos elegidos por el usuario.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val backupRepository: BackupRepository,
    private val backupFileStore: BackupFileStore
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = sessionManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _backupState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)

    /**
     * Estado actual del flujo de copia de seguridad.
     */
    val backupState: StateFlow<BackupUiState> = _backupState.asStateFlow()

    /**
     * Nombre sugerido para el archivo de exportación.
     */
    fun suggestedBackupFileName(): String = backupFileStore.suggestedFileName()

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { sessionManager.setDarkMode(enabled) }
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
                _backupState.value = BackupUiState.Error(e.userMessage())
            }
        }
    }

    /**
     * Exporta la copia al documento indicado.
     *
     * @param uri Documento de destino.
     * @param password Contraseña de cifrado, o `null` para no cifrar.
     */
    fun exportBackup(uri: Uri, password: CharArray?) {
        if (isBusy()) return
        viewModelScope.launch {
            _backupState.value = BackupUiState.Working
            try {
                withContext(Dispatchers.IO) {
                    backupFileStore.openOutput(uri).use { output ->
                        backupRepository.exportTo(output, password)
                    }
                }
                _backupState.value = BackupUiState.Exported("Copia exportada correctamente")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                backupFileStore.delete(uri)
                _backupState.value = BackupUiState.Error(e.userMessage())
            } finally {
                password?.fill('\u0000')
            }
        }
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
                _backupState.value = BackupUiState.Imported("Datos importados correctamente")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _backupState.value = BackupUiState.Error(e.userMessage())
            } finally {
                password?.fill('\u0000')
            }
        }
    }

    /**
     * Descarta un archivo de exportación que se creó pero no llegó a escribirse.
     */
    fun discardExport(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            backupFileStore.delete(uri)
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

    private fun isBusy(): Boolean =
        _backupState.value is BackupUiState.Working ||
            _backupState.value is BackupUiState.Inspecting

    private fun Throwable.userMessage(): String =
        (this as? BackupException)?.message ?: "No se pudo completar la operación"
}
