package com.andres.pillremind.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Fila seleccionable para grupos de opciones exclusivas.
 *
 * La fila completa es clicable y el botón de radio es solo indicativo, de modo
 * que el área táctil cumple el mínimo de 48dp.
 *
 * @param label Texto principal de la opción.
 * @param selected Indica si la opción está seleccionada.
 * @param onClick Acción que se ejecuta al pulsar la fila.
 * @param modifier Modificador para personalizar el layout.
 * @param description Texto secundario opcional.
 */
@Composable
fun SettingsRadioItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        RadioButton(selected = selected, onClick = null)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Selector de tema")
@Composable
private fun SettingsRadioItemPreview() {
    PillRemindTheme {
        Column(modifier = Modifier.selectableGroup()) {
            SettingsRadioItem(
                label = stringResource(R.string.settings_theme_system),
                description = stringResource(R.string.settings_theme_system_description),
                selected = true,
                onClick = {},
            )
            SettingsRadioItem(
                label = stringResource(R.string.settings_theme_light),
                selected = false,
                onClick = {},
            )
            SettingsRadioItem(
                label = stringResource(R.string.settings_theme_dark),
                selected = false,
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Selector de tema oscuro")
@Composable
private fun SettingsRadioItemDarkPreview() {
    PillRemindTheme(darkTheme = true) {
        Column(modifier = Modifier.selectableGroup()) {
            SettingsRadioItem(
                label = stringResource(R.string.settings_theme_system),
                description = stringResource(R.string.settings_theme_system_description),
                selected = false,
                onClick = {},
            )
            SettingsRadioItem(
                label = stringResource(R.string.settings_theme_light),
                selected = false,
                onClick = {},
            )
            SettingsRadioItem(
                label = stringResource(R.string.settings_theme_dark),
                selected = true,
                onClick = {},
            )
        }
    }
}
