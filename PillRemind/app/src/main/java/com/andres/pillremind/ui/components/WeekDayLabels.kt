package com.andres.pillremind.ui.components

import androidx.annotation.StringRes
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.WeekDay

/**
 * Etiqueta completa del día de la semana para la interfaz.
 */
@StringRes
fun WeekDay.labelRes(): Int = when (this) {
    WeekDay.MONDAY -> R.string.lunes
    WeekDay.TUESDAY -> R.string.martes
    WeekDay.WEDNESDAY -> R.string.miercoles
    WeekDay.THURSDAY -> R.string.jueves
    WeekDay.FRIDAY -> R.string.viernes
    WeekDay.SATURDAY -> R.string.sabado
    WeekDay.SUNDAY -> R.string.domingo
}

/**
 * Etiqueta corta del día de la semana para los chips de selección.
 */
@StringRes
fun WeekDay.shortNameRes(): Int = when (this) {
    WeekDay.MONDAY -> R.string.lunesShort
    WeekDay.TUESDAY -> R.string.martesShort
    WeekDay.WEDNESDAY -> R.string.miercolesShort
    WeekDay.THURSDAY -> R.string.juevesShort
    WeekDay.FRIDAY -> R.string.viernesShort
    WeekDay.SATURDAY -> R.string.sabadoShort
    WeekDay.SUNDAY -> R.string.domingoShort
}
