package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.ScheduleRule
import com.andres.pillremind.domain.model.ScheduleType
import java.time.LocalTime

/**
 * Convierte una entidad de horario en el modelo de dominio.
 *
 * @throws IllegalArgumentException Si la fila no contiene los campos necesarios
 * para su tipo de regla.
 */
fun ScheduleEntity.toDomain(): Schedule = Schedule(
    id = id,
    medicationId = medicationId,
    rule = toRule(),
    time = time
)

/**
 * Convierte un horario de dominio en su entidad de Room.
 */
fun Schedule.toEntity(): ScheduleEntity = rule.toEntity(
    id = id,
    medicationId = medicationId,
    time = time
)

/**
 * Reconstruye la regla de recurrencia a partir de las columnas de la entidad.
 */
fun ScheduleEntity.toRule(): ScheduleRule = when (type) {
    ScheduleType.WEEKLY -> ScheduleRule.Weekly(
        day = requireNotNull(weekDay) {
            "El horario semanal $id no tiene día de la semana"
        }
    )

    ScheduleType.EVERY_N_DAYS -> ScheduleRule.EveryNDays(
        intervalDays = requireNotNull(intervalDays) {
            "El horario $id no tiene intervalo de días"
        }
    )

    ScheduleType.MONTHLY_DAY -> ScheduleRule.MonthlyDay(
        dayOfMonth = requireNotNull(dayOfMonth) {
            "El horario $id no tiene día del mes"
        },
        clampToLastDay = clampToLastDay ?: true
    )

    ScheduleType.MONTHLY_WEEKDAY -> ScheduleRule.MonthlyWeekday(
        occurrence = requireNotNull(monthWeek) {
            "El horario $id no tiene ocurrencia mensual"
        },
        day = requireNotNull(weekDay) {
            "El horario mensual $id no tiene día de la semana"
        }
    )

    ScheduleType.CYCLE -> ScheduleRule.Cycle(
        onDays = requireNotNull(onDays) {
            "El ciclo $id no tiene días de toma"
        },
        offDays = requireNotNull(offDays) {
            "El ciclo $id no tiene días de descanso"
        }
    )

    ScheduleType.SINGLE -> ScheduleRule.Single
}

/**
 * Convierte una regla de dominio en columnas de Room.
 */
fun ScheduleRule.toEntity(
    id: Int,
    medicationId: Int,
    time: LocalTime
): ScheduleEntity = ScheduleEntity(
    id = id,
    medicationId = medicationId,
    type = type,
    weekDay = (this as? ScheduleRule.Weekly)?.day
        ?: (this as? ScheduleRule.MonthlyWeekday)?.day,
    intervalDays = (this as? ScheduleRule.EveryNDays)?.intervalDays,
    dayOfMonth = (this as? ScheduleRule.MonthlyDay)?.dayOfMonth,
    clampToLastDay = (this as? ScheduleRule.MonthlyDay)?.clampToLastDay,
    monthWeek = (this as? ScheduleRule.MonthlyWeekday)?.occurrence,
    onDays = (this as? ScheduleRule.Cycle)?.onDays,
    offDays = (this as? ScheduleRule.Cycle)?.offDays,
    time = time
)
