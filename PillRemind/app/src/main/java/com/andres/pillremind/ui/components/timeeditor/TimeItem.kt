package com.andres.pillremind.ui.components.timeeditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Fila que muestra una hora y las acciones de edición y eliminación.
 *
 * Usa la forma de los campos del formulario y un [IconButton] para que el área
 * táctil de eliminar llegue a 48dp. Toda la fila es pulsable para editar la hora.
 *
 * @param time Hora mostrada (se trunca a minutos para visualización).
 * @param enabled Si es true la fila permite editar y muestra el icono de eliminar.
 * @param onClick Callback ejecutado cuando se pulsa la fila para editar la hora.
 * @param onRemove Callback ejecutado cuando se solicita eliminar este tiempo.
 */
@Composable
fun TimeItem(
    time: LocalTime,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    onClickLabel = stringResource(R.string.edit_time),
                    onClick = onClick,
                )
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = time.truncatedTo(ChronoUnit.MINUTES).toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (enabled) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.delete_time),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
