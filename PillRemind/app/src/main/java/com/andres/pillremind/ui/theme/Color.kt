package com.andres.pillremind.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores de la aplicación (claro/oscuro).
 *
 * Los colores de marca se mantienen; los roles de Material3 que faltaban se
 * definen aquí para que el sistema no mezcle la paleta con la de por defecto.
 */

// ─── Marca ───────────────────────────────────────────────
val BrandPurple = Color(0xFF32138D)   // Botones primarios, títulos, iconos activos
val ActionOrange = Color(0xFFFF6F00)  // Aviso: toma pendiente / urgente
val SuccessGreen = Color(0xFF81C784)  // Confirmación: toma realizada
val NeutralGray = Color(0xFF5F5F5F)   // Texto secundario legible
val TextBlack = Color(0xFF1A1A1A)     // Texto principal sobre superficies claras
val SurfaceWhite = Color.White        // Fondo de cards y campos
val BackgroundOff = Color(0xFFF7F7F6) // Fondo general de la app

// ─── Light Theme ─────────────────────────────────────────
val LightPrimary = BrandPurple
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFEDE7F6)
val LightOnPrimaryContainer = BrandPurple

val LightSecondary = ActionOrange
val LightOnSecondary = Color(0xFF2B1700)
val LightSecondaryContainer = Color(0xFFFFF3E0)
val LightOnSecondaryContainer = Color(0xFF6B2E00)

val LightTertiary = SuccessGreen
val LightOnTertiary = Color(0xFF00391C)
val LightTertiaryContainer = Color(0xFFE8F5E9)
val LightOnTertiaryContainer = Color(0xFF1B5E20)

val LightBackground = BackgroundOff
val LightOnBackground = TextBlack
val LightSurface = SurfaceWhite
val LightOnSurface = TextBlack
val LightSurfaceVariant = Color(0xFFEEEEEE)
val LightOnSurfaceVariant = NeutralGray

val LightError = Color(0xFFB3261E)
val LightOnError = Color.White
val LightErrorContainer = Color(0xFFF9DEDC)
val LightOnErrorContainer = Color(0xFF410E0B)

val LightOutline = Color(0xFF7A7A7A)
val LightOutlineVariant = Color(0xFFD9D9D7)
val LightScrim = Color.Black
val LightInverseSurface = Color(0xFF303030)
val LightInverseOnSurface = Color(0xFFF5F5F5)
val LightInversePrimary = Color(0xFFBB9EFF)

val LightSurfaceDim = Color(0xFFDEDEDC)
val LightSurfaceBright = Color(0xFFFFFFFF)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF7F7F6)
val LightSurfaceContainer = Color(0xFFF1F1EF)
val LightSurfaceContainerHigh = Color(0xFFEAEAE8)
val LightSurfaceContainerHighest = Color(0xFFE2E2E0)

// ─── Dark Theme ──────────────────────────────────────────
val DarkPrimary = Color(0xFFBB9EFF)
val DarkOnPrimary = Color(0xFF1E0070)
val DarkPrimaryContainer = Color(0xFF32138D)
val DarkOnPrimaryContainer = Color(0xFFEDE7F6)

val DarkSecondary = Color(0xFFFFB74D)
val DarkOnSecondary = Color(0xFF4D2C00)
val DarkSecondaryContainer = Color(0xFF704000)
val DarkOnSecondaryContainer = Color(0xFFFFDDB3)

val DarkTertiary = Color(0xFFB9F6CA)
val DarkOnTertiary = Color(0xFF00391C)
val DarkTertiaryContainer = Color(0xFF00522C)
val DarkOnTertiaryContainer = Color(0xFFD7FFD9)

val DarkBackground = Color(0xFF121212)
val DarkOnBackground = Color(0xFFEEEEEE)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnSurface = Color(0xFFEEEEEE)
val DarkSurfaceVariant = Color(0xFF2C2C2C)
val DarkOnSurfaceVariant = Color(0xFFBDBDBD)

val DarkError = Color(0xFFF2B8B5)
val DarkOnError = Color(0xFF601410)
val DarkErrorContainer = Color(0xFF8C1D18)
val DarkOnErrorContainer = Color(0xFFF9DEDC)

val DarkOutline = Color(0xFF8A8A8A)
val DarkOutlineVariant = Color(0xFF444444)
val DarkScrim = Color.Black
val DarkInverseSurface = Color(0xFFE6E6E6)
val DarkInverseOnSurface = Color(0xFF303030)
val DarkInversePrimary = Color(0xFF32138D)

val DarkSurfaceDim = Color(0xFF121212)
val DarkSurfaceBright = Color(0xFF3A3A3A)
val DarkSurfaceContainerLowest = Color(0xFF0E0E0E)
val DarkSurfaceContainerLow = Color(0xFF1A1A1A)
val DarkSurfaceContainer = Color(0xFF242424)
val DarkSurfaceContainerHigh = Color(0xFF2C2C2C)
val DarkSurfaceContainerHighest = Color(0xFF333333)
