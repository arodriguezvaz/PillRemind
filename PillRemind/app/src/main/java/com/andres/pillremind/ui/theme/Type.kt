package com.andres.pillremind.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Escala tipográfica base de la aplicación.
 *
 * Todos los estilos usan Lexend para evitar que Material3 caiga en la
 * tipografía del sistema en los roles que no se definan aquí.
 */
private fun lexend(
    fontWeight: FontWeight,
    fontSize: Int,
    lineHeight: Int,
    letterSpacing: Double = 0.0,
): TextStyle = TextStyle(
    fontFamily = Lexend,
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

val Typography = Typography(
    displayLarge = lexend(FontWeight.Normal, 57, 64, -0.25),
    displayMedium = lexend(FontWeight.Normal, 45, 52, 0.0),
    displaySmall = lexend(FontWeight.Normal, 36, 44, 0.0),

    headlineLarge = lexend(FontWeight.SemiBold, 32, 40, 0.0),
    headlineMedium = lexend(FontWeight.SemiBold, 28, 36, 0.0),
    headlineSmall = lexend(FontWeight.SemiBold, 24, 32, 0.0),

    titleLarge = lexend(FontWeight.Bold, 18, 24, 0.0),
    titleMedium = lexend(FontWeight.SemiBold, 16, 24, 0.1),
    titleSmall = lexend(FontWeight.SemiBold, 14, 20, 0.1),

    bodyLarge = lexend(FontWeight.Normal, 16, 24, 0.5),
    bodyMedium = lexend(FontWeight.Normal, 14, 20, 0.25),
    bodySmall = lexend(FontWeight.Normal, 12, 16, 0.4),

    labelLarge = lexend(FontWeight.Bold, 14, 20, 0.1),
    labelMedium = lexend(FontWeight.Medium, 12, 16, 0.5),
    labelSmall = lexend(FontWeight.Medium, 11, 16, 0.5),
)
