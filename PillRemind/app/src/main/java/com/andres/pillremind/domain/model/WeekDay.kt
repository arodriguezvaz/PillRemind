package com.andres.pillremind.domain.model

import com.andres.pillremind.R
import java.time.DayOfWeek

enum class WeekDay(val label: Int, val shortName: Int) {
    MONDAY(R.string.lunes, R.string.lunesShort),
    TUESDAY(R.string.martes, R.string.martesShort),
    WEDNESDAY(R.string.miercoles, R.string.miercolesShort),
    THURSDAY(R.string.jueves, R.string.juevesShort),
    FRIDAY(R.string.viernes, R.string.viernesShort),
    SATURDAY(R.string.sabado, R.string.sabadoShort),
    SUNDAY(R.string.domingo, R.string.domingoShort)
}

fun WeekDay.toDayOfWeek(): DayOfWeek = when (this) {
    WeekDay.MONDAY -> DayOfWeek.MONDAY
    WeekDay.TUESDAY -> DayOfWeek.TUESDAY
    WeekDay.WEDNESDAY -> DayOfWeek.WEDNESDAY
    WeekDay.THURSDAY -> DayOfWeek.THURSDAY
    WeekDay.FRIDAY -> DayOfWeek.FRIDAY
    WeekDay.SATURDAY -> DayOfWeek.SATURDAY
    WeekDay.SUNDAY -> DayOfWeek.SUNDAY
}