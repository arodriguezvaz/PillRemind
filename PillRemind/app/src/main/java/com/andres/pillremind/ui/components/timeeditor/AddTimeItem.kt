package com.andres.pillremind.ui.components.timeeditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composable que representa una card "Añadir" para añadir una nueva hora.
 *
 * Al pulsarla se ejecuta la lambda [onClick] que normalmente abrirá un TimePickerDialog.
 *
 * @param onClick Lambda ejecutada al pulsar la card.
 */
@Composable
fun AddTimeItem(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }

    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center

        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Añadir hora",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}