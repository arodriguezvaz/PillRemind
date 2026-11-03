package com.andres.pillremind.ui.components
import com.andres.pillremind.ui.theme.AppElevation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Composable que representa un campo de texto personalizado.
 * @param value Valor actual del campo de texto.
 * @param onValueChange Función a ejecutar cuando se cambia el valor del campo de texto.
 * @param modifier Modificador para personalizar la vista.
 * @param placeholder Texto a mostrar como marcador de posición.
 * @param enabled Indica si el campo de texto está habilitado.
 * @param keyboardType Tipo de teclado que se muestra al editar el campo.
 * @param singleLine Indica si el campo debe ocupar una sola línea.
 * @param minLines Número mínimo de líneas visibles cuando no es de una sola línea.
 * @param maxLines Número máximo de líneas visibles cuando no es de una sola línea.
 * @param leadingIcon Icono opcional a la izquierda.
 * @param trailingIcon Contenido opcional a la derecha.
 * @param imeAction Acción del teclado (buscar, siguiente, etc.).
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Default,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        shadowElevation = AppElevation,
        color = MaterialTheme.colorScheme.surface
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            leadingIcon = leadingIcon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                    )
                }
            },
            trailingIcon = trailingIcon,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            shape = MaterialTheme.shapes.medium,
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
