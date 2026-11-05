package com.andres.pillremind.domain.model

/**
 * Preferencia de tema de la aplicación.
 *
 * [SYSTEM] sigue el modo claro u oscuro configurado en Android; [LIGHT] y
 * [DARK] fuerzan un tema concreto.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}
