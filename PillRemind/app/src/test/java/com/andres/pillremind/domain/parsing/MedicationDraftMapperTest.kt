package com.andres.pillremind.domain.parsing

import com.andres.pillremind.domain.model.CimaMedication
import com.andres.pillremind.domain.model.DraftSource
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del mapeo de CIMA y del OCR al formulario.
 */
class MedicationDraftMapperTest {

    private val retrievedAt = LocalDate.of(2026, 11, 8)

    private fun cima(
        name: String,
        dosis: String,
        form: String
    ) = CimaMedication(
        cn = "663526",
        name = name,
        dosis = dosis,
        form = form,
        presentation = "$name, 1 frasco",
        retrievedAt = retrievedAt
    )

    @Test
    fun `nasonex usa la toma como dosis por defecto`() {
        val draft = MedicationDraftMapper.fromCima(
            cima(
                name = "NASONEX 50 MICROGRAMOS SUSPENSIÓN PARA PULVERIZACIÓN NASAL",
                dosis = "50 µg",
                form = "SUSPENSIÓN PARA PULVERIZACIÓN NASAL"
            )
        )

        assertEquals("NASONEX 50 MICROGRAMOS", draft.name)
        assertEquals("1", draft.doseText)
        assertEquals("puffs", draft.unit)
        assertEquals("1", draft.intakeDoseText)
        assertEquals("puffs", draft.intakeUnit)
        assertEquals("50", draft.strengthDoseText)
        assertEquals("mcg", draft.strengthUnit)
        assertEquals(DraftSource.CIMA, draft.source)
        assertFalse(draft.notCatalogued)
        assertEquals(retrievedAt, draft.retrievedAt)
    }

    @Test
    fun `mucibron usa la concentracion al ser liquido`() {
        val draft = MedicationDraftMapper.fromCima(
            cima(
                name = "MUCIBRON FORTE 6 MG/ML SOLUCION ORAL",
                dosis = "6 mg/ml",
                form = "SOLUCIÓN ORAL"
            )
        )

        assertEquals("MUCIBRON FORTE 6 MG/ML", draft.name)
        assertEquals("6", draft.doseText)
        assertEquals("mg", draft.unit)
        assertNull(draft.intakeDoseText)
        assertEquals("6", draft.strengthDoseText)
        assertEquals("mg", draft.strengthUnit)
    }

    @Test
    fun `ryaltris conserva la concentracion completa en el nombre`() {
        val draft = MedicationDraftMapper.fromCima(
            cima(
                name = "RYALTRIS 25 MICROGRAMOS/600 MICROGRAMOS/PULSACION " +
                    "SUSPENSION PARA PULVERIZACION NASAL",
                dosis = "25 microgramos/600 microgramos/dosis",
                form = "SUSPENSIÓN PARA PULVERIZACIÓN NASAL"
            )
        )

        assertEquals("RYALTRIS 25 MICROGRAMOS/600 MICROGRAMOS", draft.name)
        assertEquals("1", draft.doseText)
        assertEquals("puffs", draft.unit)
        assertEquals("25", draft.strengthDoseText)
        assertEquals("mcg", draft.strengthUnit)
    }

    @Test
    fun `el ocr de la caja se marca como no catalogado`() {
        val draft = MedicationDraftMapper.fromOcr(
            fields = BoxFields(
                name = "Nasonex",
                strengthDoseText = "50",
                strengthUnit = "mcg",
                strengthText = "50 microgramos",
                form = "pulverización",
                intakeDoseText = "1",
                intakeUnit = "puffs"
            ),
            cn = "663526"
        )

        assertEquals("Nasonex", draft.name)
        assertEquals("1", draft.doseText)
        assertEquals("puffs", draft.unit)
        assertEquals("663526", draft.cn)
        assertEquals(DraftSource.OCR, draft.source)
        assertTrue(draft.notCatalogued)
    }

    @Test
    fun `shortName corta antes de la forma farmaceutica`() {
        assertEquals(
            "ESOMEPRAZOL CINFA 20 MG",
            MedicationDraftMapper.shortName(
                "ESOMEPRAZOL CINFA 20 MG CAPSULAS DURAS GASTRORRESISTENTES EFG"
            )
        )
        assertEquals(
            "MUCIBRON FORTE 6 MG/ML",
            MedicationDraftMapper.shortName("MUCIBRON FORTE 6 MG/ML SOLUCION ORAL")
        )
        assertEquals(
            "SIN FORMA",
            MedicationDraftMapper.shortName("SIN FORMA")
        )
    }

    @Test
    fun `parseDose acepta microgramos y combinaciones`() {
        assertEquals("50" to "mcg", MedicationDraftMapper.parseDose("50 µg"))
        assertEquals("6" to "mg", MedicationDraftMapper.parseDose("6 mg/ml"))
        assertEquals("10" to "%", MedicationDraftMapper.parseDose("10 %"))
        assertEquals(
            "25" to "mcg",
            MedicationDraftMapper.parseDose("25 microgramos/600 microgramos/dosis")
        )
        assertNull(MedicationDraftMapper.parseDose(null))
        assertNull(MedicationDraftMapper.parseDose("sin dosis"))
    }
}
