package com.andres.pillremind.ui.components

import android.R.color.black
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.ui.theme.LightBackground
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Composable que representa un campo de texto personalizado.
 * @param value Valor actual del campo de texto.
 * @param onValueChange Función a ejecutar cuando se cambia el valor del campo de texto.
 * @param modifier Modificador para personalizar la vista.
 * @param placeholder Texto a mostrar como marcador de posición.
 * @param enabled Indica si el campo de texto está habilitado.
 * @param isError Indica si el campo de texto está en un estado de error.
 * @param singleLine Indica si el campo de texto está limitado a una sola línea.
 * @param visualTransformation Transformación visual aplicada al texto (contraseñas).
 * @param trailingIcon Composable opcional mostrado al final del campo.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it) } },
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            isError = isError,
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = enabled
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AppTextFieldPreview() {
    PillRemindTheme {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AppTextField(
                value = "Dalsy",
                onValueChange = { },
                placeholder = "Escribe algo...",


                )
        }
    }
}
