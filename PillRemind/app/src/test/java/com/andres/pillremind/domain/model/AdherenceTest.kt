package com.andres.pillremind.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas del cálculo de adherencia.
 */
class AdherenceTest {

    @Test
    fun `el porcentaje usa solo las tomas que ya vencieron`() {
        val adherence = adherence(taken = 24, missed = 3, total = 30)

        assertEquals(3, adherence.pending)
        assertEquals(89, adherence.adherencePercent)
    }

    @Test
    fun `sin tomas vencidas el porcentaje es null`() {
        val adherence = adherence(taken = 0, missed = 0, total = 5)

        assertEquals(5, adherence.pending)
        assertNull(adherence.adherencePercent)
    }

    @Test
    fun `el resumen agrega tomas y calcula el porcentaje global`() {
        val summary = listOf(
            adherence(taken = 8, missed = 2, total = 10),
            adherence(taken = 10, missed = 0, total = 10),
        ).toAdherenceSummary()

        assertEquals(18, summary.taken)
        assertEquals(2, summary.missed)
        assertEquals(0, summary.pending)
        assertEquals(90, summary.adherencePercent)
    }

    @Test
    fun `el resumen vacio no tiene porcentaje`() {
        val summary = emptyList<MedicationAdherence>().toAdherenceSummary()

        assertEquals(0, summary.taken)
        assertEquals(0, summary.missed)
        assertEquals(0, summary.pending)
        assertNull(summary.adherencePercent)
    }

    private fun adherence(taken: Int, missed: Int, total: Int) = MedicationAdherence(
        medicationId = 1,
        medicationName = "Ibuprofeno",
        dose = 600.0,
        unit = "mg",
        imageUrl = null,
        isArchived = false,
        total = total,
        taken = taken,
        missed = missed,
    )
}
