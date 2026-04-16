package com.andres.pillremind.domain.model

import java.util.UUID
import java.time.LocalDateTime

data class Perfil(
    val id: UUID = UUID.randomUUID(),
    val usuarioId: UUID,
    val nombre: String,
    val urlImagen: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
