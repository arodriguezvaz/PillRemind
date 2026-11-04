package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Acciones que cierran un formulario.
 *
 * Se colocan al final del contenido desplazable para que el usuario recorra el
 * formulario completo antes de guardar. La acción principal ocupa todo el ancho
 * disponible y la secundaria se muestra como un botón compacto con icono a su
 * derecha, separada y menos expuesta a pulsaciones accidentales.
 *
 * @param primaryText Texto de la acción principal.
 * @param onPrimaryClick Acción que se ejecuta al pulsar el botón principal.
 * @param modifier Modificador para personalizar el layout.
 * @param primaryEnabled Indica si la acción principal está habilitada.
 * @param supportingText Aviso opcional sobre el estado del formulario, como los
 *   campos que faltan por completar.
 * @param secondaryIcon Icono de la acción secundaria; si es null no se muestra.
 * @param secondaryContentDescription Descripción accesible de la acción secundaria.
 * @param secondaryContentColor Color del icono de la acción secundaria.
 * @param onSecondaryClick Acción que se ejecuta al pulsar el botón secundario.
 */
@Composable
fun AppFormActions(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    supportingText: String? = null,
    secondaryIcon: ImageVector? = null,
    secondaryContentDescription: String? = null,
    secondaryContentColor: Color = MaterialTheme.colorScheme.primary,
    onSecondaryClick: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppButton(
                text = primaryText,
                onClick = onPrimaryClick,
                modifier = Modifier.weight(1f),
                size = ButtonSize.Large,
                enabled = primaryEnabled,
            )
            if (secondaryIcon != null && onSecondaryClick != null) {
                Spacer(modifier = Modifier.width(AppSpacing.md))
                FilledTonalIconButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(
                            elevation = AppElevation,
                            shape = MaterialTheme.shapes.medium,
                        ),
                    shape = MaterialTheme.shapes.medium,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = secondaryContentColor,
                    ),
                ) {
                    Icon(
                        imageVector = secondaryIcon,
                        contentDescription = secondaryContentDescription,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.xs)
            )
        }
    }
}
