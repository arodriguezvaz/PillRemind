package com.andres.pillremind.domain.model

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
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

    @Test
    fun `every n days con intervalo uno coincide todos los dias`() {
        val rule = ScheduleRule.EveryNDays(1)

        assertTrue(rule.occursOn(monday, monday))
        assertTrue(rule.occursOn(monday.plusDays(1), monday))
        assertTrue(rule.occursOn(monday.plusDays(30), monday))
    }

    @Test
    fun `monthly day contempla el 29 de febrero`() {
        val rule = ScheduleRule.MonthlyDay(dayOfMonth = 29, clampToLastDay = true)

        assertTrue(rule.occursOn(LocalDate.of(2028, 2, 29), monday))
        assertTrue(rule.occursOn(LocalDate.of(2027, 2, 28), monday))
        assertFalse(rule.occursOn(LocalDate.of(2027, 3, 1), monday))
    }

    @Test
    fun `monthly weekday encuentra la segunda ocurrencia`() {
        val rule = ScheduleRule.MonthlyWeekday(occurrence = 2, day = WeekDay.MONDAY)

        assertTrue(rule.occursOn(monday.plusWeeks(1), monday))
        assertFalse(rule.occursOn(monday, monday))
        assertFalse(rule.occursOn(monday.plusWeeks(2), monday))
    }

    @Test
    fun `monthly weekday ultimo tambien funciona si el mes acaba en ese dia`() {
        val rule = ScheduleRule.MonthlyWeekday(occurrence = -1, day = WeekDay.MONDAY)
        val lastMondayOfAugust = LocalDate.of(2026, 8, 31)

        assertTrue(rule.occursOn(lastMondayOfAugust, monday))
        assertFalse(rule.occursOn(lastMondayOfAugust.minusWeeks(1), monday))
    }

    @Test
    fun `cycle sin dias de descanso coincide todos los dias`() {
        val rule = ScheduleRule.Cycle(onDays = 3, offDays = 0)

        assertTrue(rule.occursOn(monday, monday))
        assertTrue(rule.occursOn(monday.plusDays(10), monday))
    }

    @Test
    fun `reglas invalidas no pasan la validacion`() {
        assertThrows(IllegalArgumentException::class.java) {
            ScheduleRule.MonthlyDay(dayOfMonth = 0).requireValid()
        }
        assertThrows(IllegalArgumentException::class.java) {
            ScheduleRule.MonthlyDay(dayOfMonth = 32).requireValid()
        }
        assertThrows(IllegalArgumentException::class.java) {
            ScheduleRule.MonthlyWeekday(occurrence = 0, day = WeekDay.MONDAY).requireValid()
        }
        assertThrows(IllegalArgumentException::class.java) {
            ScheduleRule.MonthlyWeekday(occurrence = 6, day = WeekDay.MONDAY).requireValid()
        }
        assertThrows(IllegalArgumentException::class.java) {
            ScheduleRule.Cycle(onDays = 0, offDays = 1).requireValid()
        }
        assertThrows(IllegalArgumentException::class.java) {
            ScheduleRule.Cycle(onDays = 1, offDays = -1).requireValid()
        }
    }
}
