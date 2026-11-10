package com.andres.pillremind.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Fila de navegación con icono, título, descripción y flecha.
 *
 * Se usa en las secciones de «Seguimiento» de la ficha del perfil y del
 * medicamento para abrir estadísticas o historial.
 *
 * @param icon Icono representativo de la sección.
 * @param label Texto principal de la fila.
 * @param description Texto secundario que explica el contenido.
 * @param onClick Acción ejecutada al pulsar la fila.
 * @param modifier Modificador opcional para la fila.
 */
@Composable
fun AppNavigationRow(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = label, onClick = onClick)
            .padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_keyboard_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true, name = "Fila de navegación")
@Composable
private fun AppNavigationRowPreview() {
    PillRemindTheme {
        Column {
            AppNavigationRow(
                icon = ImageVector.vectorResource(R.drawable.ic_bar_chart),
                label = "Ver estadísticas",
                description = "Adherencia y resumen de tomas",
                onClick = {},
                modifier = Modifier.padding(AppSpacing.md),
            )
            AppNavigationRow(
                icon = ImageVector.vectorResource(R.drawable.ic_history),
                label = "Ver historial de tomas",
                description = "Busca y filtra las tomas registradas",
                onClick = {},
                modifier = Modifier.padding(AppSpacing.md),
            )
        }
    }
}
