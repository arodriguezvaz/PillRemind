package com.andres.pillremind.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Pruebas de la validación de dosis y fechas de un medicamento.
 */
class MedicationValidationTest {

    @Test
    fun `parseDose acepta enteros y decimales con punto`() {
        assertEquals(500.0, parseDose("500")!!, 0.0)
        assertEquals(1.5, parseDose("1.5")!!, 0.0)
    }

    @Test
    fun `parseDose acepta la coma decimal espanola`() {
        assertEquals(1.5, parseDose("1,5")!!, 0.0)
    }

    @Test
    fun `parseDose recorta los espacios`() {
        assertEquals(2.25, parseDose("  2,25  ")!!, 0.0)
    }

    @Test
    fun `parseDose rechaza textos y valores no finitos`() {
        listOf("", "   ", "abc", "0", "-1", "NaN", "Infinity", "-Infinity")
            .forEach { input ->
                assertNull("Debería rechazar '$input'", parseDose(input))
            }
    }

    @Test
    fun `requireValidForSaving acepta un medicamento correcto`() {
        val medication = medication()

        medication.requireValidForSaving()
    }

    @Test
    fun `requireValidForSaving rechaza dosis no positiva`() {
        assertThrows(IllegalArgumentException::class.java) {
            medication(dose = 0.0).requireValidForSaving()
        }
        assertThrows(IllegalArgumentException::class.java) {
            medication(dose = -5.0).requireValidForSaving()
        }
    }

    @Test
    fun `requireValidForSaving rechaza una fecha de fin anterior al inicio`() {
        assertThrows(IllegalArgumentException::class.java) {
            medication(
                startDate = LocalDate.of(2026, 10, 10),
                endDate = LocalDate.of(2026, 10, 9),
            ).requireValidForSaving()
        }
    }

    @Test
    fun `requireValidForSaving permite fecha de fin igual al inicio`() {
        medication(
            startDate = LocalDate.of(2026, 10, 10),
            endDate = LocalDate.of(2026, 10, 10),
        ).requireValidForSaving()
    }

    @Test
    fun `requireValidForSaving rechaza nombre o unidad vacios`() {
        assertThrows(IllegalArgumentException::class.java) {
            medication(name = "  ").requireValidForSaving()
        }
        assertThrows(IllegalArgumentException::class.java) {
            medication(unit = " ").requireValidForSaving()
        }
    }

    @Test
    fun `isFinished distingue tratamientos terminados`() {
        val today = LocalDate.of(2026, 10, 20)

        assertEquals(
            true,
            medication(endDate = LocalDate.of(2026, 10, 19)).isFinished(today)
        )
        assertEquals(
            false,
            medication(endDate = LocalDate.of(2026, 10, 20)).isFinished(today)
        )
        assertEquals(
            false,
            medication(isChronic = true, endDate = LocalDate.of(2026, 10, 1))
                .isFinished(today)
        )
        assertEquals(false, medication(endDate = null).isFinished(today))
    }

    private fun medication(
        name: String = "Ibuprofeno",
        dose: Double = 600.0,
        unit: String = "mg",
        isChronic: Boolean = false,
        startDate: LocalDate = LocalDate.of(2026, 10, 1),
        endDate: LocalDate? = null,
    ) = Medication(
        profileId = 1,
        name = name,
        dose = dose,
        unit = unit,
        isChronic = isChronic,
        startDate = startDate,
        endDate = endDate,
    )
}
