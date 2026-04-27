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
 * Composable que representa un campo de formulario.
 * @param label Etiqueta del campo de formulario.
 * @param content Contenido del campo de formulario.
 */
@Composable
fun FormField(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}


enum class FormFieldSize { Small, Medium }


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