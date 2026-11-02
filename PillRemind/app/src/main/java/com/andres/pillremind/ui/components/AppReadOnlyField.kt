package com.andres.pillremind.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Campo de solo lectura: muestra la etiqueta y el valor como texto, sin el
 * aspecto de un input deshabilitado.
 *
 * @param label Etiqueta del campo.
 * @param value Valor mostrado.
 * @param modifier Modificador para el contenedor.
 * @param valueColor Color del valor; por defecto, el color de texto principal.
 */
@Composable
fun AppReadOnlyField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    AppFormField(label = label, modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
        )
    }
}
