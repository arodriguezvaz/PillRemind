package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Campo de formulario con una etiqueta y su contenido.
 *
 * La etiqueta usa el rol [MaterialTheme.typography.labelLarge] y el color de texto
 * principal del tema. Así queda en negro en claro y en blanco en oscuro.
 *
 * @param label Etiqueta del campo.
 * @param modifier Modificador para el contenedor.
 * @param labelColor Color de la etiqueta; por defecto, el color de texto principal.
 * @param labelStyle Estilo de la etiqueta; por defecto, `labelLarge`.
 * @param content Contenido del campo (normalmente un [AppTextField] o similar).
 */
@Composable
fun AppFormField(
    label: String,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    labelStyle: TextStyle = MaterialTheme.typography.labelLarge,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = labelStyle,
            color = labelColor,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        content()
    }
}
