package com.andres.pillremind.notifications

import com.andres.pillremind.domain.model.WeekDay
import com.andres.pillremind.domain.model.toDayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Función auxiliar que calcula el próximo instante en el que debe dispararse
 * una alarma de recordatorio de medicación según el día de la semana y la hora.
 *
 * Si la hora calculada ya ha pasado en la semana actual, se programa para la siguiente semana.
 *
 * @param weekDay Día de la semana en el que debe ejecutarse la alarma.
 * @param time Hora programada de la alarma.
 * @return Tiempo en milisegundos (epoch) correspondiente a la próxima ejecución.
 */
fun nextTriggerMillis(weekDay: WeekDay, time: LocalTime): Long {
    val now = LocalDateTime.now()
    val targetDow = weekDay.toDayOfWeek()

    var next = now
        .with(TemporalAdjusters.nextOrSame(targetDow))
        .withHour(time.hour)
        .withMinute(time.minute)
        .withSecond(0)
        .withNano(0)

    if (!next.isAfter(now)) {
        next = next.plusWeeks(1)
    }

    return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}