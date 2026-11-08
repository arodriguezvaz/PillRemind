package com.andres.pillremind.ui.screens.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.domain.model.UpdateInfo
import com.andres.pillremind.domain.model.UpdateStatus
import com.andres.pillremind.domain.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado que consume la compuerta de actualizaciones.
 *
 * @property status Estado calculado a partir de la versión instalada y de la
 *   última información guardada.
 * @property currentVersionName Versión instalada, para mostrarla al usuario.
 */
data class UpdateUiState(
    val status: UpdateStatus = UpdateStatus.UpToDate,
    val currentVersionName: String = "",
)

/**
 * ViewModel de la comprobación de actualizaciones.
 *
 * Al arrancar evalúa la caché (por si ya se sabía que la actualización era
 * obligatoria) y, si han pasado más de 24 horas, consulta el manifiesto en
 * segundo plano. Un fallo de red no bloquea la aplicación.
 *
 * @property repository Repositorio de actualizaciones.
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repository: UpdateRepository,
) : ViewModel() {

    private val installed = repository.installedAppInfo()
    private val installedVersionLabel = installed.versionName.ifBlank {
        installed.versionCode.toString()
    }

    /**
     * Estado observado por la interfaz.
     */
    val uiState: StateFlow<UpdateUiState> = combine(
        repository.cachedInfo,
        repository.dismissedVersionCode,
    ) { info, dismissedVersionCode ->
        UpdateUiState(
            status = evaluate(info, dismissedVersionCode),
            currentVersionName = installedVersionLabel,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UpdateUiState(currentVersionName = installedVersionLabel),
    )

    init {
        viewModelScope.launch { checkIfNeeded() }
    }

    /**
     * Guarda que el usuario ha pospuesto la versión indicada.
     *
     * @param versionCode Versión que no debe volver a avisarse.
     */
    fun dismissUpdate(versionCode: Long) {
        viewModelScope.launch { repository.dismiss(versionCode) }
    }

    /**
     * Consulta el manifiesto solo si no hay una comprobación reciente.
     *
     * Si falla, no se propaga el error: la caché anterior sigue siendo la
     * fuente de verdad y el usuario no ve un aviso por una comprobación
     * silenciosa.
     */
    private suspend fun checkIfNeeded() {
        val lastCheckAt = repository.lastCheckAt.first()
        val now = System.currentTimeMillis()
        if (lastCheckAt != null && now - lastCheckAt < CHECK_INTERVAL_MS) {
            return
        }
        repository.refresh()
    }

    /**
     * Aplica la política de versiones y los casos especiales de la app.
     */
    private fun evaluate(
        info: UpdateInfo?,
        dismissedVersionCode: Long?,
    ): UpdateStatus {
        if (info == null) {
            return UpdateStatus.UpToDate
        }

        val status = info.statusFor(installed.versionCode)
        // En debug no se bloquea la app: un APK local con versionCode 1 no debe
        // quedar inutilizado. El aviso sigue siendo visible, pero descartable.
        val effectiveStatus = if (installed.isDebuggable && status is UpdateStatus.Required) {
            UpdateStatus.Optional(status.info)
        } else {
            status
        }

        return if (effectiveStatus is UpdateStatus.Optional &&
            effectiveStatus.info.versionCode == dismissedVersionCode
        ) {
            UpdateStatus.UpToDate
        } else {
            effectiveStatus
        }
    }

    internal companion object {
        /** Intervalo mínimo entre comprobaciones automáticas. */
        const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }
}
