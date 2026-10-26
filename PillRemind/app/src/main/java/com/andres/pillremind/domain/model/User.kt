package com.andres.pillremind.domain.model

import java.time.LocalDateTime

/**
 * Representa al usuario local propietario de los perfiles.
 *
 * La aplicación funciona únicamente con datos locales, por lo que no hay
 * cuentas, correo ni estado de sincronización asociados.
 *
 * @property id Identificador único del usuario
 * @property createdAt Fecha de creación del usuario
 * @property updatedAt Fecha de última actualización del usuario
 */
data class User(
    val id: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
