package com.andres.pillremind.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.andres.pillremind.ui.theme.AppElevation

/**
 * Botón flotante reutilizable de la aplicación.
 *
 * Normaliza el estilo de todos los FAB: fondo con el violeta principal del tema
 * y contenido en el color de contraste, de modo que el icono se vea blanco en
 * el tema claro.
 *
 * @param onClick Lambda ejecutada al pulsar el botón.
 * @param modifier Modificador para personalizar el layout del botón.
 * @param content Contenido del botón, normalmente un icono de añadir.
 */
@Composable
fun AppFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = AppElevation,
            pressedElevation = AppElevation,
        ),
        content = content,
    )
}
