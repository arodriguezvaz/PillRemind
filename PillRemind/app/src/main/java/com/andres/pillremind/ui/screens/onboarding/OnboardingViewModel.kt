package com.andres.pillremind.ui.screens.onboarding

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.notifications.ExactAlarmStatus
import com.andres.pillremind.notifications.NotificationAction
import com.andres.pillremind.notifications.NotificationStatus
import com.andres.pillremind.notifications.ReminderPermissions
import com.andres.pillremind.notifications.resolveNotificationAction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla de bienvenida.
 *
 * Solo contiene datos de presentación: la UI decide cómo representarlos y el
 * ViewModel concentra las comprobaciones de permisos y la persistencia.
 */
data class OnboardingUiState(
    val notificationStatus: NotificationStatus = NotificationStatus.REQUESTABLE,
    val notificationAction: NotificationAction = NotificationAction.REQUEST,
    val exactAlarmStatus: ExactAlarmStatus = ExactAlarmStatus.NOT_REQUIRED,
    val showExactAlarmFallback: Boolean = false,
    val canContinue: Boolean = false,
    val isSaving: Boolean = false,
)

/**
 * ViewModel de la bienvenida de primer arranque.
 *
 * Comprueba el estado real de los permisos cada vez que la pantalla vuelve a
 * primer plano y guarda en [SessionManager] que la bienvenida ya se completó.
 *
 * @property sessionManager Gestor de sesión donde se persiste el flag.
 * @property reminderPermissions Comprobador de los permisos del sistema.
 * @property savedStateHandle Conserva si ya se intentó cada permiso.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val reminderPermissions: ReminderPermissions,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var lastShowRationale = false

    init {
        refresh(showNotificationRationale = false)
    }

    /**
     * Recalcula el estado de los permisos.
     *
     * @param showNotificationRationale Resultado de
     * `shouldShowRequestPermissionRationale` para notificaciones.
     */
    fun refresh(showNotificationRationale: Boolean) {
        lastShowRationale = showNotificationRationale

        val notificationStatus = reminderPermissions.notificationStatus()
        val notificationRequested = savedStateHandle[KEY_NOTIFICATION_REQUESTED] ?: false
        val exactAlarmStatus = reminderPermissions.exactAlarmStatus()
        val exactAlarmAttempted = savedStateHandle[KEY_EXACT_ALARM_ATTEMPTED] ?: false
        val notificationsGranted = notificationStatus == NotificationStatus.GRANTED

        _uiState.update { state ->
            state.copy(
                notificationStatus = notificationStatus,
                notificationAction = resolveNotificationAction(
                    status = notificationStatus,
                    requested = notificationRequested,
                    showRationale = showNotificationRationale,
                ),
                exactAlarmStatus = exactAlarmStatus,
                showExactAlarmFallback = notificationsGranted &&
                    (
                        exactAlarmStatus == ExactAlarmStatus.UNAVAILABLE ||
                            (exactAlarmAttempted && exactAlarmStatus == ExactAlarmStatus.PENDING)
                        ),
                canContinue = notificationsGranted &&
                    (
                        exactAlarmStatus == ExactAlarmStatus.GRANTED ||
                            exactAlarmStatus == ExactAlarmStatus.NOT_REQUIRED
                        ),
            )
        }
    }

    /**
     * Registra el resultado del diálogo de notificaciones.
     *
     * Se marca como solicitado para poder distinguir la primera petición de
     * una denegación definitiva. El estado real se vuelve a leer del sistema.
     *
     * @param showNotificationRationale Resultado de
     * `shouldShowRequestPermissionRationale` tras cerrar el diálogo.
     */
    fun onNotificationPermissionResult(showNotificationRationale: Boolean) {
        savedStateHandle[KEY_NOTIFICATION_REQUESTED] = true
        refresh(showNotificationRationale)
    }

    /**
     * Registra que se ha intentado abrir los ajustes de alarmas exactas.
     *
     * Si al volver el permiso sigue denegado, la pantalla ofrece continuar sin
     * alarmas exactas para no dejar al usuario atrapado en dispositivos que no
     * permiten concederlo.
     */
    fun onExactAlarmRequestAttempted() {
        savedStateHandle[KEY_EXACT_ALARM_ATTEMPTED] = true
        refresh(lastShowRationale)
    }

    /**
     * Completa la bienvenida si todos los permisos necesarios están resueltos.
     */
    fun complete() {
        if (!_uiState.value.canContinue) return
        finish()
    }

    /**
     * Completa la bienvenida sin alarmas exactas.
     *
     * Solo se permite cuando la pantalla ha mostrado la salida de emergencia,
     * que a su vez exige que las notificaciones estén concedidas.
     */
    fun completeWithoutExactAlarms() {
        if (!_uiState.value.showExactAlarmFallback) return
        finish()
    }

    private fun finish() {
        if (_uiState.value.isSaving) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                sessionManager.completeOnboarding()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("OnboardingViewModel", "No se pudo guardar la bienvenida", error)
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private companion object {
        const val KEY_NOTIFICATION_REQUESTED = "notification_requested"
        const val KEY_EXACT_ALARM_ATTEMPTED = "exact_alarm_attempted"
    }
}
