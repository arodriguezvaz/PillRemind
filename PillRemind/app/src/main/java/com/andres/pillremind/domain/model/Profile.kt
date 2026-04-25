package com.andres.pillremind.domain.model

import java.time.LocalDateTime

/**
 * Clase que representa un profile de la aplicación
 * @property id Identificador único del profile
 * @property name Nombre del profile
 * @property urlImagen URL de la imagen del profile
 * @property createdAt Fecha de creación del profile
 * @property updatedAt Fecha de última actualización del profile
 */
data class Profile(
    val id: Int = 0,
    val name: String,
    val urlImagen: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
