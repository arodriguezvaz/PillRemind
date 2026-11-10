package com.andres.pillremind.ui.screens.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.UpdateInfo
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Pantalla que impide usar la aplicación hasta instalar una versión mínima.
 *
 * No tiene acción de descarte. El botón de salir cierra la actividad para que
 * el usuario no quede atrapado si no puede instalar la actualización; los
 * recordatorios ya programados siguen funcionando mediante alarmas del sistema.
 *
 * @param info Información de la versión publicada.
 * @param currentVersionName Versión instalada actualmente.
 * @param onUpdate Abre la descarga del APK.
 * @param onOpenReleaseNotes Abre la página de la release.
 * @param onExit Cierra la aplicación.
 */
@Composable
fun UpdateRequiredScreen(
    info: UpdateInfo,
    currentVersionName: String,
    onUpdate: () -> Unit,
    onOpenReleaseNotes: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_system_update),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp),
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = stringResource(R.string.update_required_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Text(
                text = stringResource(
                    R.string.update_required_message,
                    currentVersionName,
                    info.versionName,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            AppButton(
                text = stringResource(R.string.update_now),
                onClick = onUpdate,
                size = ButtonSize.Large,
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            AppButton(
                text = stringResource(R.string.update_release_notes),
                onClick = onOpenReleaseNotes,
                variant = ButtonVariant.Tonal,
                size = ButtonSize.Large,
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            TextButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.update_exit),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = stringResource(R.string.update_required_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, name = "Actualización obligatoria")
@Composable
private fun UpdateRequiredScreenPreview() {
    PillRemindTheme {
        UpdateRequiredScreen(
            info = UpdateInfo(
                versionCode = 2,
                versionName = "1.2.0",
                minimumVersionCode = 2,
                apkUrl = "https://github.com/arodriguezvaz/PillRemind/releases/download/v1.2.0/PillRemind-1.2.0.apk",
                releaseUrl = "https://github.com/arodriguezvaz/PillRemind/releases/tag/v1.2.0",
            ),
            currentVersionName = "1.1.0",
            onUpdate = {},
            onOpenReleaseNotes = {},
            onExit = {},
        )
    }
}
