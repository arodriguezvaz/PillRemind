package com.andres.pillremind.domain.model

import java.util.UUID
import java.time.LocalDateTime
import java.time.LocalDate

data class Medicamento(
    val id: UUID = UUID.randomUUID(),
    val perfilId: UUID,
    val nombre: String,
    val dosis: Double,
    val unidad: String,
    val esCronico: Boolean = false,
    val fechaInicio: LocalDate,
    val fechaFin: LocalDate? = null,
    val urlImagen: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
