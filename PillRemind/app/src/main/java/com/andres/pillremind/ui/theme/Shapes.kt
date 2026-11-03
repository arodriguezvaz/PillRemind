package com.andres.pillremind.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Formas base de la aplicación.
 *
 * - `medium` (12dp): campos, botones y controles.
 * - `large` (20dp): cards de contenido y estados vacíos.
 * - `extraLarge` (28dp): hojas y contenedores grandes.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
