package com.andres.pillremind.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas del cálculo de la próxima toma de un medicamento.
 */
class ScheduleNextIntakeTest {

    // 2 de noviembre de 2026: lunes.
    private val monday = LocalDate.of(2026, 11, 2)
    private val medication = Medication(
        id = 1,
        profileId = 1,
        name = "Enalapril",
        dose = 10.0,
        unit = "mg",
        startDate = monday,
    )

    @Test
    fun `elige la hora futura más cercana del mismo día`() {
        val schedules = listOf(
            schedule(ScheduleRule.EveryNDays(1), LocalTime.of(9, 0)),
            schedule(ScheduleRule.EveryNDays(1), LocalTime.of(21, 0)),
        )

        val next = schedules.nextIntake(medication, LocalDateTime.of(monday, LocalTime.of(10, 0)))

        assertEquals(LocalDateTime.of(monday, LocalTime.of(21, 0)), next)
    }

    @Test
    fun `pasa al día siguiente cuando ya han pasado las horas`() {
        val schedules = listOf(schedule(ScheduleRule.EveryNDays(1), LocalTime.of(9, 0)))

        val next = schedules.nextIntake(medication, LocalDateTime.of(monday, LocalTime.of(22, 0)))

        assertEquals(LocalDateTime.of(monday.plusDays(1), LocalTime.of(9, 0)), next)
    }

    @Test
    fun `cada horario conserva su propia regla y su propia hora`() {
        val schedules = listOf(
            schedule(ScheduleRule.EveryNDays(1), LocalTime.of(9, 0)),
            schedule(ScheduleRule.Weekly(WeekDay.SUNDAY), LocalTime.of(21, 0)),
        )

        val next = schedules.nextIntake(medication, LocalDateTime.of(monday, LocalTime.of(10, 0)))

        assertEquals(LocalDateTime.of(monday.plusDays(1), LocalTime.of(9, 0)), next)
    }

    @Test
    fun `respeta la fecha de fin del tratamiento`() {
        val schedules = listOf(schedule(ScheduleRule.EveryNDays(1), LocalTime.of(9, 0)))

        val next = schedules.nextIntake(
            medication.copy(endDate = monday),
            LocalDateTime.of(monday, LocalTime.of(10, 0)),
        )

        assertNull(next)
    }

    @Test
    fun `no devuelve tomas de un medicamento archivado`() {
        val schedules = listOf(schedule(ScheduleRule.EveryNDays(1), LocalTime.of(9, 0)))

        val next = schedules.nextIntake(
            medication.copy(deletedAt = LocalDateTime.of(monday, LocalTime.of(8, 0))),
        )

        assertNull(next)
    }

    @Test
    fun `devuelve null cuando no hay horarios`() {
        assertNull(emptyList<Schedule>().nextIntake(medication))
    }

    private fun schedule(rule: ScheduleRule, time: LocalTime) = Schedule(
        medicationId = medication.id,
        rule = rule,
        time = time,
    )
}
