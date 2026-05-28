package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Composable que representa un campo de formulario con una etiqueta y contenido
 * (por ejemplo un TextField).
 *
 * @param label Etiqueta del campo de formulario.
 * @param content Composable que representa el contenido del campo (p. ej. TextField).
 */
@Composable
fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}


@Preview(showBackground = true)
@Composable
fun FormFieldPreview() {
    MaterialTheme {
        FormField(label = "Nombre del medicamento") {
            TextField(
                value = "Ibuprofeno",
                onValueChange = {}
            )
        }
    }
}