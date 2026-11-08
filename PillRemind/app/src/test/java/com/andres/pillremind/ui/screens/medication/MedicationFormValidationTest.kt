package com.andres.pillremind.ui.screens.medication

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la validación de dosis y fechas del formulario de medicamentos.
 */
class MedicationFormValidationTest {

    private val startDate = LocalDate.of(2026, 3, 12)

    @Test
    fun `acepta la dosis con coma decimal`() {
        assertEquals(1.5, requireNotNull(parseDoseInput("1,5")), 0.0)
    }

    @Test
    fun `acepta la dosis con punto decimal`() {
        assertEquals(1.5, requireNotNull(parseDoseInput("1.5")), 0.0)
    }

    @Test
    fun `recorta los espacios de la dosis`() {
        assertEquals(2.0, requireNotNull(parseDoseInput(" 2 ")), 0.0)
    }

    @Test
    fun `rechaza dosis vacias o no numericas`() {
        assertNull(parseDoseInput(""))
        assertNull(parseDoseInput("   "))
        assertNull(parseDoseInput("abc"))
        assertNull(parseDoseInput("1,2,3"))
    }

    @Test
    fun `rechaza dosis no positivas ni finitas`() {
        assertNull(parseDoseInput("0"))
        assertNull(parseDoseInput("-1"))
        assertNull(parseDoseInput("NaN"))
        assertNull(parseDoseInput("Infinity"))
    }

    @Test
    fun `acepta un rango sin fecha de fin o con el fin igual o posterior`() {
        assertTrue(isDateRangeValid(null, startDate))
        assertTrue(isDateRangeValid(startDate, null))
        assertTrue(isDateRangeValid(startDate, startDate))
        assertTrue(isDateRangeValid(startDate, startDate.plusDays(1)))
    }

    @Test
    fun `rechaza una fecha de fin anterior al inicio`() {
        assertFalse(isDateRangeValid(startDate, startDate.minusDays(1)))
    }
}
