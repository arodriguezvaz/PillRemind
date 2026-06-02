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
 */
@Composable
fun SettingsLinkItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}