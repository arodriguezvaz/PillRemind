package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sección de formulario con un título y un bloque de campos.
 *
 * @param title Título de la sección.
 * @param modifier Modificador para el contenedor.
 * @param contentSpacing Separación entre los hijos del contenido. Los campos de
 *   medicamento ya llevan su propio margen inferior, así que allí se usa 0dp.
 * @param content Campos de la sección.
 */
@Composable
fun AppFormSection(
    title: String,
    modifier: Modifier = Modifier,
    contentSpacing: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
            content = content,
        )
    }
}
