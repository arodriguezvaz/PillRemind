package com.andres.pillremind.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Elemento clicable de ajustes tipo enlace.
 *
 * Muestra un texto interactivo que ejecuta una acción al pulsarlo.
 *
 * @param label Texto mostrado como enlace.
 * @param onClick Acción ejecutada al hacer clic.
 * @param modifier Modificador para personalizar el layout.
 * @param enabled Indica si el elemento se puede pulsar.
 */
@Composable
fun SettingsLinkItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp)
    )
}
