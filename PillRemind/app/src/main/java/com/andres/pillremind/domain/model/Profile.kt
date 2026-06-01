package com.andres.pillremind.domain.model

import java.time.LocalDateTime

/**
 * Representa un perfil de usuario en la aplicación.
 *
 * @property id Identificador único del perfil
 * @property name Nombre del perfil
 * @property urlImagen URL de la imagen del perfil
 * @property createdAt Fecha de creación del perfil
 * @property updatedAt Fecha de última actualización del perfil
 */
data class Profile(
    val id: Int = 0,
    val usuarioId: Int,
    val supabaseId: String? = null,
    val name: String,
    val urlImagen: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
