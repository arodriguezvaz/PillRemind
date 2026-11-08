package com.andres.pillremind.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Opacidad del texto de ayuda, la misma que Material 3 usa en campos deshabilitados. */
private const val PlaceholderAlpha = 0.38f

/**
 * Color de los textos de ayuda (placeholder) de los campos de formulario.
 *
 * Coincide con el marcador de posición de [AppDatePickerField]: texto principal
 * atenuado al 38 %. Así las pistas de todos los campos se leen con la misma
 * intensidad, tanto en claro como en oscuro.
 */
@Composable
internal fun appFieldPlaceholderColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = PlaceholderAlpha)
