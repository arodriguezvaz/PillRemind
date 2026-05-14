package com.andres.pillremind.domain.model

import com.andres.pillremind.R
import java.time.DayOfWeek

/**
 * Representa los días de la semana utilizados en la aplicación.
 * Incluye recursos de texto para su representación en la interfaz de usuario,
 * tanto en formato completo como abreviado.
 */
enum class WeekDay(val label: Int, val shortName: Int) {
    MONDAY(R.string.lunes, R.string.lunesShort),
    TUESDAY(R.string.martes, R.string.martesShort),
    WEDNESDAY(R.string.miercoles, R.string.miercolesShort),
    THURSDAY(R.string.jueves, R.string.juevesShort),
    FRIDAY(R.string.viernes, R.string.viernesShort),
    SATURDAY(R.string.sabado, R.string.sabadoShort),
    SUNDAY(R.string.domingo, R.string.domingoShort)
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