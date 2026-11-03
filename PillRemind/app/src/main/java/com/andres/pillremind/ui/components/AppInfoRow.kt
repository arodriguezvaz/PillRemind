package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andres.pillremind.R

/**
 * Fila de información de solo lectura con etiqueta y valor en la misma línea.
 *
 * La etiqueta va en color secundario y el valor en el color principal, ambos
 * con el mismo tamaño para que la fila se lea de un vistazo.
 *
 * @param label Etiqueta de la fila.
 * @param value Valor mostrado; si es nulo o está vacío, la fila no se muestra.
 * @param modifier Modificador para la fila.
 */
@Composable
fun AppInfoRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    val displayValue = value?.takeIf { it.isNotBlank() } ?: return

    val labelStyle = SpanStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )
    val valueStyle = SpanStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
    )
    val formattedLabel = stringResource(R.string.common_info_row_format, label)

    Text(
        text = buildAnnotatedString {
            withStyle(labelStyle) {
                append(formattedLabel)
            }
            withStyle(valueStyle) {
                append(displayValue)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            lineHeight = 26.sp,
        ),
    )
}
