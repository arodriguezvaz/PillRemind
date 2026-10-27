package com.andres.pillremind.domain.model

import java.time.DayOfWeek

/**
 * Representa los días de la semana utilizados por el dominio.
 *
 * No contiene recursos ni etiquetas de interfaz: la capa UI es la encargada
 * de traducir cada valor a su texto correspondiente.
 */
enum class WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

/**
 * Convierte el WeekDay al equivalente de java.time.DayOfWeek.
 */
fun WeekDay.toDayOfWeek(): DayOfWeek = when (this) {
    WeekDay.MONDAY -> DayOfWeek.MONDAY
    WeekDay.TUESDAY -> DayOfWeek.TUESDAY
    WeekDay.WEDNESDAY -> DayOfWeek.WEDNESDAY
    WeekDay.THURSDAY -> DayOfWeek.THURSDAY
    WeekDay.FRIDAY -> DayOfWeek.FRIDAY
    WeekDay.SATURDAY -> DayOfWeek.SATURDAY
    WeekDay.SUNDAY -> DayOfWeek.SUNDAY
}

/**
 * Convierte el DayOfWeek al equivalente de WeekDay.
 */
fun DayOfWeek.toWeekDay(): WeekDay = when (this) {
    DayOfWeek.MONDAY -> WeekDay.MONDAY
    DayOfWeek.TUESDAY -> WeekDay.TUESDAY
    DayOfWeek.WEDNESDAY -> WeekDay.WEDNESDAY
    DayOfWeek.THURSDAY -> WeekDay.THURSDAY
    DayOfWeek.FRIDAY -> WeekDay.FRIDAY
    DayOfWeek.SATURDAY -> WeekDay.SATURDAY
    DayOfWeek.SUNDAY -> WeekDay.SUNDAY
}
