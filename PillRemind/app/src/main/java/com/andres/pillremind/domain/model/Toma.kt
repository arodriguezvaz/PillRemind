package com.andres.pillremind.domain.model

import java.util.UUID
import java.time.LocalDateTime

data class Toma(
    val id: UUID = UUID.randomUUID(),
    val horarioId: UUID,
    val fechaHoraProgramada: LocalDateTime,
    val fechaHoraReal: LocalDateTime? = null,
    val estado: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)