package com.andres.pillremind.domain.model

import java.time.LocalDateTime

/**
 * Representa una toma de medicación programada.
 * Registra la hora prevista y la hora real en la que se realizó,
 * así como el estado de la toma.
 *
 * La toma pertenece siempre a un medicamento. El horario es opcional:
 * si el horario se elimina, la toma conserva su historial.
 *
 * @property id Identificador único de la toma
 * @property medicationId Identificador del medicamento al que pertenece
 * @property scheduleId Identificador del horario que generó la toma (opcional)
 * @property scheduledAt Fecha y hora programada de la toma
 * @property takenAt Fecha y hora real en la que se realizó la toma (opcional)
 * @property medicationNameSnapshot Nombre del medicamento en el momento de crear la toma
 * @property doseSnapshot Dosis del medicamento en el momento de crear la toma
 * @property unitSnapshot Unidad de la dosis en el momento de crear la toma
 * @property createdAt Fecha de creación del registro
 * @property updatedAt Fecha de última actualización del registro
 */
data class Intake(
    val id: Int = 0,
    val medicationId: Int = 0,
    val scheduleId: Int? = null,
    val scheduledAt: LocalDateTime,
    val takenAt: LocalDateTime? = null,
    val medicationNameSnapshot: String = "",
    val doseSnapshot: Double = 0.0,
    val unitSnapshot: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
