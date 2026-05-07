package com.andres.pillremind.domain.model

import java.util.UUID
import java.time.LocalDateTime

/**
 * Representa una toma de medicación programada.
 * Registra la hora prevista y la hora real en la que se realizó,
 * así como el estado de la toma.
 *
 * @property id Identificador único de la toma
 * @property horarioId Identificador del horario asociado
 * @property fechaHoraProgramada Fecha y hora programada de la toma
 * @property fechaHoraReal Fecha y hora real en la que se realizó la toma (opcional)
 * @property estado Estado de la toma (por ejemplo: pendiente, tomada, omitida)
 * @property createdAt Fecha de creación del registro
 * @property updatedAt Fecha de última actualización del registro
 */
data class Toma(
    val id: UUID = UUID.randomUUID(),
    val horarioId: UUID,
    val fechaHoraProgramada: LocalDateTime,
    val fechaHoraReal: LocalDateTime? = null,
    val estado: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)