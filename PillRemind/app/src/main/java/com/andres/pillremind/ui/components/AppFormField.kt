package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Campo de formulario con una etiqueta y su contenido.
 *
 * La etiqueta usa el color primario del tema y el rol [MaterialTheme.typography.labelLarge].
 * El color es un token del tema, no un estilo tipográfico, para que se adapte a claro/oscuro.
 *
 * @param label Etiqueta del campo.
 * @param modifier Modificador para el contenedor.
 * @param labelColor Color de la etiqueta; por defecto, el primario del tema.
 * @param content Contenido del campo (normalmente un [AppTextField] o similar).
 */
@Composable
fun AppFormField(
    label: String,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = labelColor,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
