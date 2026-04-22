package com.andres.pillremind.ui.theme

import androidx.compose.ui.graphics.Color

// Brand colors - son los colores pincipales de la aplicaciṕn
val BrandPurple = Color(0xFF32138D)   // Botones primarios, títulos, iconos activos
val ActionOrange = Color(0xFFFF6F00)   // Botón "Tomar" pendiente (urgente)
val SuccessGreen = Color(0xFF81C784)   // Chip "Tomada" / confirmación

// Neutros ─────────────────────────────────────────────
val NeutralGray = Color(0xFF969696)   // Info secundaria, hora programada, inactivos
val TextBlack = Color(0xFF1A1A1A)   // Texto principal de cards (name pastilla, etc.)
val SurfaceWhite = Color.White         // Fondo de cards
val BackgroundOff = Color(0xFFF7F7F6)   // Fondo general de la app

// ─── Light Theme ─────────────────────────────────────────
val LightPrimary = BrandPurple
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFEDE7F6)   // Fondo suave para iconos/chips purple
val LightOnPrimaryContainer = BrandPurple

val LightSecondary = ActionOrange
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFFFF3E0)
val LightOnSecondaryContainer = ActionOrange

val LightTertiary = SuccessGreen
val LightOnTertiary = Color.White
val LightTertiaryContainer = Color(0xFFE8F5E9)
val LightOnTertiaryContainer = Color(0xFF2E7D32)

val LightBackground = BackgroundOff
val LightOnBackground = TextBlack
val LightSurface = SurfaceWhite
val LightOnSurface = TextBlack
val LightSurfaceVariant = Color(0xFFEEEEEE)
val LightOnSurfaceVariant = NeutralGray

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
val DarkOnSurfaceVariant = Color(0xFF969696)