package com.andres.pillremind.data.mapper

import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.ScheduleRule
import com.andres.pillremind.domain.model.ScheduleType
import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Pruebas de conversión entre las reglas de dominio y las columnas de Room.
 */
class ScheduleMapperTest {

    private val time = LocalTime.of(8, 0)

    @Test
    fun `cada regla sobrevive a la ida y vuelta`() {
        val rules = listOf(
            ScheduleRule.Weekly(WeekDay.MONDAY),
            ScheduleRule.EveryNDays(2),
            ScheduleRule.MonthlyDay(dayOfMonth = 31, clampToLastDay = true),
            ScheduleRule.MonthlyDay(dayOfMonth = 15, clampToLastDay = false),
            ScheduleRule.MonthlyWeekday(occurrence = -1, day = WeekDay.FRIDAY),
            ScheduleRule.Cycle(onDays = 5, offDays = 2),
            ScheduleRule.Single
        )

        rules.forEach { rule ->
            val schedule = Schedule(
                id = 1,
                medicationId = 2,
                rule = rule,
                time = time
            )

            assertEquals(schedule, schedule.toEntity().toDomain())
        }
    }

    @Test
    fun `monthly day sin valor de ajuste usa el ultimo dia`() {
        val entity = ScheduleEntity(
            id = 1,
            medicationId = 2,
            type = ScheduleType.MONTHLY_DAY,
            dayOfMonth = 31,
            clampToLastDay = null,
            time = time
        )

        assertEquals(
            ScheduleRule.MonthlyDay(dayOfMonth = 31, clampToLastDay = true),
            entity.toDomain().rule
        )
    }

    @Test
    fun `una entidad semanal sin dia no se puede mapear`() {
        val entity = ScheduleEntity(
            id = 1,
            medicationId = 2,
            type = ScheduleType.WEEKLY,
            weekDay = null,
            time = time
        )

        assertThrows(IllegalArgumentException::class.java) { entity.toDomain() }
    }

    @Test
    fun `una entidad recurrente sin intervalo no se puede mapear`() {
        val entity = ScheduleEntity(
            id = 1,
            medicationId = 2,
            type = ScheduleType.EVERY_N_DAYS,
            intervalDays = null,
            time = time
        )

        assertThrows(IllegalArgumentException::class.java) { entity.toDomain() }
    }

    @Test
    fun `un ciclo incompleto no se puede mapear`() {
        val entity = ScheduleEntity(
            id = 1,
            medicationId = 2,
            type = ScheduleType.CYCLE,
            onDays = 5,
            offDays = null,
            time = time
        )

        assertThrows(IllegalArgumentException::class.java) { entity.toDomain() }
    }
}
