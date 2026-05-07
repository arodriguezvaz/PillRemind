package com.andres.pillremind.ui.components.timeeditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Item que muestra una hora y una acción de eliminación opcional.
 *
 * @param time Hora mostrada (se trunca a minutos para visualización).
 * @param enabled Si es true se muestra el icono de eliminar.
 * @param onRemove Callback ejecutado cuando se solicita eliminar este tiempo.
 */
@Composable
fun TimeItem(
    time: LocalTime,
    enabled: Boolean = true,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = time.truncatedTo(ChronoUnit.MINUTES).toString())

            if (enabled) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Eliminar",
                    modifier = Modifier.clickable { onRemove() },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}