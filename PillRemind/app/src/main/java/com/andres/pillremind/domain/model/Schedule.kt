package com.andres.pillremind.domain.model

import java.time.LocalTime

/**
 * Representa un horario programado para la toma de un medicamento.
 *
 * @property id Identificador único del horario
 * @property medicationId Identificador del medicamento asociado
 * @property weekDay Día de la semana en el que se debe tomar
 * @property time Hora programada de la toma
 */
data class Schedule(
    val id: Int = 0,
    val medicationId: Int,
    val weekDay: WeekDay,
    val time: LocalTime
)