package com.andres.pillremind.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
/**
 * Elemento de ajustes con interruptor (toggle).
 *
 * Muestra una etiqueta junto a un Switch para activar o desactivar una opción.
 *
 * @param label Texto descriptivo de la opción.
 * @param checked Estado actual del switch.
 * @param onCheckedChange Callback que se ejecuta al cambiar el estado.
 * @param modifier Modificador para personalizar el layout.
 * @param enabled Indica si el switch está habilitado o deshabilitado.
 */
@Composable
fun SettingsToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}