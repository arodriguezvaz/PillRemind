package com.andres.pillremind.ui.components.timeeditor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.andres.pillremind.R
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant

/**
 * Acción para añadir una nueva hora a la pauta.
 *
 * Usa el botón tonal de la aplicación para que se lea como una acción
 * disponible y no como una card deshabilitada.
 *
 * @param onClick Acción ejecutada al pulsar el botón.
 */
@Composable
fun AddTimeItem(
    onClick: () -> Unit
) {
    AppButton(
        text = stringResource(R.string.add_time),
        onClick = onClick,
        variant = ButtonVariant.Tonal,
        size = ButtonSize.Large,
        leadingIcon = Icons.Rounded.Add,
    )
}
