package com.andres.pillremind.ui.screens.update

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.UpdateInfo
import com.andres.pillremind.domain.model.UpdateStatus
import com.andres.pillremind.ui.theme.AppSpacing
import kotlinx.coroutines.launch

/**
 * Compuerta que aplica la política de actualizaciones sobre el contenido
 * normal de la aplicación.
 *
 * Si hay una actualización obligatoria, sustituye el contenido por una pantalla
 * que no se puede descartar: solo permite actualizar o salir de la aplicación.
 * Si es opcional, muestra un diálogo descartable. En ambos casos el botón de
 * actualizar abre la descarga del APK en el navegador, de modo que sea el
 * instalador del sistema quien confirme la instalación.
 *
 * @param updateViewModel ViewModel de actualizaciones.
 * @param content Contenido normal de la aplicación.
 */
@Composable
fun UpdateGate(
    updateViewModel: UpdateViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by updateViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val openErrorMessage = stringResource(R.string.update_open_error)

    val openUrl: (String) -> Unit = { url ->
        val opened = runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }.isSuccess
        if (!opened) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(openErrorMessage)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val status = state.status) {
            is UpdateStatus.Required -> UpdateRequiredScreen(
                info = status.info,
                currentVersionName = state.currentVersionName,
                onUpdate = { openUrl(status.info.apkUrl) },
                onOpenReleaseNotes = { openUrl(status.info.releaseUrl) },
                onExit = { activity?.finishAffinity() },
            )

            else -> {
                content()

                if (status is UpdateStatus.Optional) {
                    UpdateAvailableDialog(
                        info = status.info,
                        onUpdate = { openUrl(status.info.apkUrl) },
                        onDismiss = {
                            updateViewModel.dismissUpdate(status.info.versionCode)
                        },
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(AppSpacing.md),
        )
    }
}

/**
 * Diálogo para una actualización que no es obligatoria.
 */
@Composable
private fun UpdateAvailableDialog(
    info: UpdateInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.update_optional_title))
        },
        text = {
            Text(text = stringResource(R.string.update_optional_message, info.versionName))
        },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(text = stringResource(R.string.update_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.update_later))
            }
        },
    )
}
