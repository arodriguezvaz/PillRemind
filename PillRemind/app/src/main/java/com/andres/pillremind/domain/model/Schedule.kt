package com.andres.pillremind.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Tipo de regla de recurrencia de un horario.
 */
enum class ScheduleType {
    WEEKLY,
    EVERY_N_DAYS,
    MONTHLY_DAY,
    MONTHLY_WEEKDAY,
    CYCLE,
    SINGLE
}

/**
 * Regla que determina en qué fechas se aplica un horario.
 *
 * La regla no conoce la fecha de inicio del medicamento: se le pasa al evaluar
 * [occursOn] para que las reglas relativas (cada N días, ciclos o toma única)
 * tengan un punto de referencia estable.
 */
sealed interface ScheduleRule {

    val type: ScheduleType

    /**
     * Un día concreto de la semana.
     */
    data class Weekly(val day: WeekDay) : ScheduleRule {
        override val type: ScheduleType = ScheduleType.WEEKLY
    }

    /**
     * Cada [intervalDays] días desde la fecha de inicio del medicamento.
     */
    data class EveryNDays(val intervalDays: Int) : ScheduleRule {
        override val type: ScheduleType = ScheduleType.EVERY_N_DAYS
    }

    /**
     * Un día del mes.
     *
     * @param dayOfMonth Día del mes, entre 1 y 31.
     * @param clampToLastDay Si es `true`, en meses sin ese día se usa el último
     * día del mes. Si es `false`, ese mes se omite.
     */
    data class MonthlyDay(
        val dayOfMonth: Int,
        val clampToLastDay: Boolean = true
    ) : ScheduleRule {
        override val type: ScheduleType = ScheduleType.MONTHLY_DAY
    }

    /**
     * Una ocurrencia concreta de un día de la semana dentro del mes.
     *
     * @param occurrence 1 para el primero, 2 para el segundo... y -1 para el último.
     * @param day Día de la semana.
     */
    data class MonthlyWeekday(
        val occurrence: Int,
        val day: WeekDay
    ) : ScheduleRule {
        override val type: ScheduleType = ScheduleType.MONTHLY_WEEKDAY
    }

    /**
     * Ciclo de días de toma seguidos de días de descanso.
     *
     * @param onDays Días consecutivos de toma.
     * @param offDays Días consecutivos de descanso.
     */
    data class Cycle(
        val onDays: Int,
        val offDays: Int
    ) : ScheduleRule {
        override val type: ScheduleType = ScheduleType.CYCLE
    }

    /**
     * Una única toma en la fecha de inicio del medicamento.
     */
    data object Single : ScheduleRule {
        override val type: ScheduleType = ScheduleType.SINGLE
    }
}

/**
 * Indica si la regla se aplica en [date].
 *
 * @param date Fecha que se quiere comprobar.
 * @param startDate Fecha de inicio del medicamento, usada como ancla.
 */
fun ScheduleRule.occursOn(date: LocalDate, startDate: LocalDate): Boolean = when (this) {
    is ScheduleRule.Weekly -> date.dayOfWeek.toWeekDay() == day

    is ScheduleRule.EveryNDays -> {
        val elapsedDays = ChronoUnit.DAYS.between(startDate, date)
        elapsedDays >= 0 && intervalDays > 0 && elapsedDays % intervalDays == 0L
    }

    is ScheduleRule.MonthlyDay -> {
        if (clampToLastDay) {
            date.dayOfMonth == dayOfMonth.coerceAtMost(date.lengthOfMonth())
        } else {
            date.dayOfMonth == dayOfMonth
        }
    }

    is ScheduleRule.MonthlyWeekday -> {
        if (date.dayOfWeek.toWeekDay() != day) {
            false
        } else if (occurrence == -1) {
            date.plusWeeks(1).month != date.month
        } else {
            (date.dayOfMonth - 1) / 7 + 1 == occurrence
        }
    }

    is ScheduleRule.Cycle -> {
        val cycleLength = onDays + offDays
        val elapsedDays = ChronoUnit.DAYS.between(startDate, date)
        elapsedDays >= 0 && cycleLength > 0 && elapsedDays % cycleLength < onDays
    }

    ScheduleRule.Single -> date == startDate
}

/**
 * Comprueba que los parámetros de la regla son coherentes.
 *
 * @throws IllegalArgumentException Si la regla no es válida.
 */
fun ScheduleRule.requireValid() {
    when (this) {
        is ScheduleRule.Weekly -> Unit

        is ScheduleRule.EveryNDays -> require(intervalDays in 1..365) {
            "El intervalo debe estar entre 1 y 365 días"
        }

        is ScheduleRule.MonthlyDay -> require(dayOfMonth in 1..31) {
            "El día del mes debe estar entre 1 y 31"
        }

        is ScheduleRule.MonthlyWeekday -> require(occurrence in 1..5 || occurrence == -1) {
            "La ocurrencia mensual debe estar entre 1 y 5, o ser -1 para el último"
        }

        is ScheduleRule.Cycle -> require(onDays >= 1 && offDays >= 0) {
            "El ciclo debe tener al menos un día de toma y ningún día negativo"
        }

        ScheduleRule.Single -> Unit
    }
}

/**
 * Representa un horario programado para la toma de un medicamento.
 *
 * @property id Identificador único del horario.
 * @property medicationId Identificador del medicamento asociado.
 * @property rule Regla de recurrencia del horario.
 * @property time Hora programada de la toma.
 */
data class Schedule(
    val id: Int = 0,
    val medicationId: Int,
    val rule: ScheduleRule,
    val time: LocalTime
) {
    init {
        rule.requireValid()
    }
}
