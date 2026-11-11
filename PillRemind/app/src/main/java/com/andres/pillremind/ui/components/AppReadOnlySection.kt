package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Sección de solo lectura sin card.
 *
 * Agrupa un bloque de datos con un título y un divisor fino. La separación
 * respecto al resto de secciones la aporta el contenedor que la use: dentro de
 * una ficha se recomienda [AppSpacing.lg].
 *
 * @param title Título de la sección.
 * @param modifier Modificador para la sección.
 * @param action Acción opcional alineada con el título, como un botón de cierre.
 * @param content Filas o bloques de la sección.
 */
@Composable
fun AppReadOnlySection(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            action?.invoke()
        }
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Column(content = content)
    }
}

@Preview(showBackground = true, name = "Sección de solo lectura")
@Composable
private fun AppReadOnlySectionPreview() {
    PillRemindTheme {
        AppReadOnlySection(
            title = "Datos personales",
            modifier = Modifier.padding(AppSpacing.md),
        ) {
            AppDetailRow(label = "Nacimiento", value = "12/03/1958")
            AppDetailRow(label = "Sexo", value = "Mujer", showDivider = false)
        }
    }
}
