package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * @param primaryLoading Muestra el indicador de carga y bloquea pulsaciones repetidas.
 * @param supportingText Aviso opcional sobre el estado del formulario, como los
 *   campos que faltan por completar.
 * @param secondaryIcon Icono de la acción secundaria; si es null no se muestra.
 * @param secondaryContentDescription Descripción accesible de la acción secundaria.
 * @param secondaryContentColor Color del icono de la acción secundaria.
 * @param secondaryEnabled Indica si la acción secundaria está habilitada.
 * @param onSecondaryClick Acción que se ejecuta al pulsar el botón secundario.
 */
@Composable
fun AppFormActions(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    primaryLoading: Boolean = false,
    supportingText: String? = null,
    secondaryIcon: ImageVector? = null,
    secondaryContentDescription: String? = null,
    secondaryContentColor: Color = MaterialTheme.colorScheme.primary,
    secondaryEnabled: Boolean = true,
    onSecondaryClick: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppButton(
                text = primaryText,
                onClick = onPrimaryClick,
                modifier = Modifier.weight(1f),
                size = ButtonSize.Large,
                enabled = primaryEnabled,
                isLoading = primaryLoading,
            )
            if (secondaryIcon != null && onSecondaryClick != null) {
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Surface(
                    onClick = onSecondaryClick,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    enabled = secondaryEnabled && !primaryLoading,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = secondaryContentColor,
                    shadowElevation = AppElevation,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = secondaryIcon,
                            contentDescription = secondaryContentDescription,
                            modifier = Modifier.size(24.dp),
                        )
                    }
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
