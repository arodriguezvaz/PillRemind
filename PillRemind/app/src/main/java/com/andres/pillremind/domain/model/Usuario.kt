package com.andres.pillremind.domain.model

import java.util.UUID
import java.time.LocalDateTime

data class Usuario(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val esPremium: Boolean = false,
    val fechaPremium: LocalDateTime? = null,
    val aceptaComunicaciones: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)