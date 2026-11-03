package com.andres.pillremind.ui.components
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Card genérica para mostrar un estado vacío o mensaje centrado.
 *
 * Se usa en pantallas donde no hay contenido y se desea mostrar un mensaje
 * informativo al usuario.
 *
 * @param text Texto a mostrar centrado en la card.
 * @param modifier Modificador opcional para personalizar la vista.
 */
@Composable
fun BlankCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.xxl), // altura visual
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}