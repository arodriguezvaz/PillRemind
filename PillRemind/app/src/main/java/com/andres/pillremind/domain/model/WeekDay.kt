package com.andres.pillremind.domain.model

import com.andres.pillremind.R

enum class WeekDay(val label: Int, val shortName: Int) {
    MONDAY(R.string.lunes, R.string.lunesShort),
    TUESDAY(R.string.martes, R.string.martesShort),
    WEDNESDAY(R.string.miercoles, R.string.miercolesShort),
    THURSDAY(R.string.jueves, R.string.juevesShort),
    FRIDAY(R.string.viernes, R.string.viernesShort),
    SATURDAY(R.string.sabado, R.string.sabadoShort),
    SUNDAY(R.string.domingo, R.string.domingoShort)
}