package com.andres.pillremind.ui.screens.medication

import com.andres.pillremind.domain.model.WeekDay
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del estado editable de la pauta.
 */
class ScheduleFormStateTest {

    private val morning = LocalTime.of(8, 0)
    private val night = LocalTime.of(20, 0)

    @Test
    fun `addTime evita horas duplicadas`() {
        val form = ScheduleFormState()

        assertTrue(form.addTime(morning))
        assertFalse(form.addTime(morning))
        assertEquals(listOf(morning), form.times)
    }

    @Test
    fun `updateTime elimina la entrada editada si la hora ya existe`() {
        val form = ScheduleFormState()
        form.addTime(morning)
        form.addTime(night)

        form.updateTime(0, night)

        assertEquals(listOf(night), form.times)
    }

    @Test
    fun `la pauta necesita al menos una hora y un dia`() {
        val form = ScheduleFormState()
        form.periodicity = SchedulePeriodicity.WEEKLY

        assertFalse(form.isValid())

        form.selectedDays = setOf(WeekDay.MONDAY)
        assertFalse(form.isValid())

        form.addTime(morning)
        assertTrue(form.isValid())
    }

    @Test
    fun `valida los intervalos y los ciclos`() {
        val form = ScheduleFormState()
        form.addTime(morning)

        form.periodicity = SchedulePeriodicity.EVERY_N_DAYS
        form.intervalDays = "0"
        assertFalse(form.isValid())
        form.intervalDays = "2"
        assertTrue(form.isValid())

        form.periodicity = SchedulePeriodicity.CYCLE
        form.onDays = "5"
        form.offDays = "0"
        assertTrue(form.isValid())
        form.offDays = "-1"
        assertFalse(form.isValid())
    }
}
