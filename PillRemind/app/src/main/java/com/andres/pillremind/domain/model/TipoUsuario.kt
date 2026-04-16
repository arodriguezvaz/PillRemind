package com.andres.pillremind.domain.model

/**
 * Enumeración que representa el tipo de usuario de la aplicación
 * @property INVITADO Usuario invitado al iniciar la app
 * @property REGISTRADO Usuario registrado en la aplicación
 * @property PREMIUM Usuario premium con acceso a funciones premium
 */
enum class TipoUsuario {
    INVITADO, REGISTRADO, PREMIUM
}
