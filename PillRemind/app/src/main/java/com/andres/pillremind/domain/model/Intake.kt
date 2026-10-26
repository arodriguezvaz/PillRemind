package com.andres.pillremind.domain.model

import java.time.LocalDateTime

/**
 * Representa una toma de medicación programada.
 * Registra la hora prevista y la hora real en la que se realizó,
 * así como el estado de la toma.
 *
 * @property id Identificador único de la toma
 * @property scheduleId Identificador del horario asociado
 * @property scheduledAt Fecha y hora programada de la toma
 * @property takenAt Fecha y hora real en la que se realizó la toma (opcional)
 * @property createdAt Fecha de creación del registro
 * @property updatedAt Fecha de última actualización del registro
 */
data class Intake(
    val id: Int = 0,
    val scheduleId: Int,
    val scheduledAt: LocalDateTime,
    val takenAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)