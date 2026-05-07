package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Botón reutilizable con variantes y tamaños configurables.
 *
 * Este composable normaliza estilos de botones usados en la app y añade
 * soporte para estados de carga.
 *
 * @param text Texto que se mostrará dentro del botón.
 * @param onClick Lambda ejecutada al pulsar el botón.
 * @param modifier Modificador para personalizar el layout del botón.
 * @param variant Variante visual del botón (Primary, Secondary, Ghost, Danger).
 * @param size Tamaño visual del botón (Small, Medium, Large).
 * @param enabled Indica si el botón está habilitado.
 * @param isLoading Si es true se muestra un indicador de progreso en lugar del texto.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val (containerColor, contentColor) = when (variant) {
        ButtonVariant.Primary -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        ButtonVariant.Secondary -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        ButtonVariant.Ghost -> Color.Transparent to MaterialTheme.colorScheme.primary
        ButtonVariant.Danger -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }
    val widthFraction = when (size) {
        ButtonSize.Small -> 0.5f
        ButtonSize.Medium -> 0.5f
        ButtonSize.Large -> 1f
    }
    val (horizontalPadding, verticalPadding, textStyle) = when (size) {
        ButtonSize.Small -> Triple(12.dp, 6.dp, MaterialTheme.typography.labelMedium)
        ButtonSize.Medium -> Triple(20.dp, 12.dp, MaterialTheme.typography.labelLarge)
        ButtonSize.Large -> Triple(28.dp, 16.dp, MaterialTheme.typography.titleSmall)
    }

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.fillMaxWidth(widthFraction),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(
            horizontal = horizontalPadding,
            vertical = verticalPadding,
        ),
    ) {
        //Si el cambio de esatdo esta cargando se muestra un símbolo de carga
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = text, style = textStyle)
        }
    }
}

/**
 * Variantes visuales para [AppButton].
 */
enum class ButtonVariant { Primary, Secondary, Ghost, Danger }

/**
 * Tamaños predefinidos para [AppButton].
 */
enum class ButtonSize { Small, Medium, Large }