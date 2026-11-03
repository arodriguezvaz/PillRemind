package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Campo de solo lectura: muestra la etiqueta y el valor como texto, sin el
 * aspecto de un input deshabilitado.
 *
 * El valor es más grande y pesado que la etiqueta para que el dato se lea de
 * un vistazo, pensando también en personas mayores.
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
    AppFormField(
        label = label,
        modifier = modifier,
        labelStyle = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Medium,
        ),
    ) {
        Text(
            text = value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = valueColor,
        )
    }
}
