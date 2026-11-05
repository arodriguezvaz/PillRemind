package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Fila de solo lectura con etiqueta y valor.
 *
 * En su variante normal la etiqueta y el valor comparten línea; en [stacked] el
 * valor se coloca debajo para textos largos como alergias o notas médicas. Con
 * texto grande (`fontScale >= 1.3`) se apila automáticamente para que los datos
 * no se partan. La fila no es pulsable: el alto mínimo de 48 dp es solo ritmo
 * visual.
 *
 * @param label Etiqueta de la fila.
 * @param value Valor mostrado; si es nulo o está vacío, la fila no se muestra.
 * @param modifier Modificador para el contenedor.
 * @param stacked Coloca el valor debajo de la etiqueta.
 * @param showDivider Dibuja un divisor debajo de la fila.
 */
@Composable
fun AppDetailRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    stacked: Boolean = false,
    showDivider: Boolean = true,
) {
    val displayValue = value?.takeIf { it.isNotBlank() } ?: return
    val useStacked = stacked || LocalDensity.current.fontScale >= 1.3f

    Column(modifier = modifier.fillMaxWidth()) {
        if (useStacked) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.xs, bottom = AppSpacing.md),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(vertical = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Text(
                    text = displayValue,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "Fila de datos")
@Composable
private fun AppDetailRowPreview() {
    PillRemindTheme {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            AppDetailRow(label = "Nacimiento", value = "12/03/1958")
            AppDetailRow(label = "Grupo sanguíneo", value = "A+")
            AppDetailRow(
                label = "Notas médicas",
                value = "Hipertensión en tratamiento. Revisión de tensión cada 6 meses.",
                stacked = true,
                showDivider = false,
            )
        }
    }
}
