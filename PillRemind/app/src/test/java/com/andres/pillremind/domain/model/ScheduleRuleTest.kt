package com.andres.pillremind.domain.model

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de las reglas de recurrencia de los horarios.
 */
class ScheduleRuleTest {

    // 2 de noviembre de 2026: lunes.
    private val monday = LocalDate.of(2026, 11, 2)

    @Test
    fun `weekly solo coincide con su dia`() {
        val rule = ScheduleRule.Weekly(WeekDay.MONDAY)

        assertTrue(rule.occursOn(monday, monday))
        assertFalse(rule.occursOn(monday.plusDays(1), monday))
    }

    @Test
    fun `every n days cuenta desde la fecha de inicio`() {
        val rule = ScheduleRule.EveryNDays(2)

        assertTrue(rule.occursOn(monday, monday))
        assertFalse(rule.occursOn(monday.plusDays(1), monday))
        assertTrue(rule.occursOn(monday.plusDays(2), monday))
        assertTrue(rule.occursOn(monday.plusDays(4), monday))
        assertFalse(rule.occursOn(monday.minusDays(2), monday))
    }

    @Test
    fun `monthly day ajusta al ultimo dia del mes`() {
        val rule = ScheduleRule.MonthlyDay(dayOfMonth = 31, clampToLastDay = true)

        assertTrue(rule.occursOn(LocalDate.of(2026, 1, 31), monday))
        assertTrue(rule.occursOn(LocalDate.of(2026, 2, 28), monday))
        assertTrue(rule.occursOn(LocalDate.of(2026, 4, 30), monday))
        assertFalse(rule.occursOn(LocalDate.of(2026, 2, 27), monday))
    }

    @Test
    fun `monthly day sin ajuste omite los meses cortos`() {
        val rule = ScheduleRule.MonthlyDay(dayOfMonth = 31, clampToLastDay = false)

        assertTrue(rule.occursOn(LocalDate.of(2026, 1, 31), monday))
        assertFalse(rule.occursOn(LocalDate.of(2026, 2, 28), monday))
        assertFalse(rule.occursOn(LocalDate.of(2026, 4, 30), monday))
    }

    @Test
    fun `monthly weekday encuentra la primera y la ultima ocurrencia`() {
        val firstMonday = ScheduleRule.MonthlyWeekday(occurrence = 1, day = WeekDay.MONDAY)
        val lastMonday = ScheduleRule.MonthlyWeekday(occurrence = -1, day = WeekDay.MONDAY)

        assertTrue(firstMonday.occursOn(monday, monday))
        assertFalse(firstMonday.occursOn(monday.plusWeeks(1), monday))

        assertTrue(lastMonday.occursOn(LocalDate.of(2026, 11, 30), monday))
        assertFalse(lastMonday.occursOn(LocalDate.of(2026, 11, 23), monday))
    }

    @Test
    fun `cycle alterna dias de toma y descanso`() {
        val rule = ScheduleRule.Cycle(onDays = 5, offDays = 2)

        assertTrue(rule.occursOn(monday, monday))
        assertTrue(rule.occursOn(monday.plusDays(4), monday))
        assertFalse(rule.occursOn(monday.plusDays(5), monday))
        assertFalse(rule.occursOn(monday.plusDays(6), monday))
        assertTrue(rule.occursOn(monday.plusDays(7), monday))
    }

    @Test
    fun `single solo coincide con la fecha de inicio`() {
        val rule = ScheduleRule.Single

        assertTrue(rule.occursOn(monday, monday))
        assertFalse(rule.occursOn(monday.plusDays(1), monday))
    }

    @Test
    fun `every n days rechaza intervalos no validos`() {
        val result = runCatching { ScheduleRule.EveryNDays(0).requireValid() }

        assertTrue(result.isFailure)
    }
}
