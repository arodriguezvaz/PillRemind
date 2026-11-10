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
    fun `mucibron deja la dosis vacia porque solo da una concentracion`() {
        val draft = MedicationDraftMapper.fromCima(
            cima(
                name = "MUCIBRON FORTE 6 MG/ML SOLUCION ORAL",
                dosis = "6 mg/ml",
                form = "SOLUCIÓN ORAL"
            )
        )

        assertEquals("MUCIBRON FORTE 6 MG/ML", draft.name)
        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
        assertNull(draft.intakeDoseText)
        assertEquals("6", draft.strengthDoseText)
        assertEquals("mg", draft.strengthUnit)
        assertEquals("6 mg/ml", draft.strengthText)
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
    fun `el ocr con concentracion compuesta no inventa una dosis`() {
        val draft = MedicationDraftMapper.fromOcr(
            fields = BoxFields(
                name = "Tritab",
                strengthDoseText = "200",
                strengthUnit = "mg",
                strengthText = "200 mg/5 ml",
                form = "polvo"
            ),
            cn = null
        )

        assertEquals("Tritab", draft.name)
        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
        assertEquals("200 mg/5 ml", draft.strengthText)
    }

    @Test
    fun `cima no usa un porcentaje como dosis`() {
        val draft = MedicationDraftMapper.fromCima(
            cima(
                name = "GEL DERMATOLÓGICO 10 %",
                dosis = "10 %",
                form = "GEL"
            )
        )

        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
        assertEquals("10", draft.strengthDoseText)
        assertEquals("%", draft.strengthUnit)
    }

    @Test
    fun `cima no usa un volumen como dosis`() {
        val draft = MedicationDraftMapper.fromCima(
            cima(
                name = "SOLUCIÓN ORAL 120 ML",
                dosis = "120 ml",
                form = "SOLUCIÓN ORAL"
            )
        )

        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
        assertEquals("120", draft.strengthDoseText)
        assertEquals("ml", draft.strengthUnit)
    }

    @Test
    fun `el ocr no usa un volumen como dosis`() {
        val draft = MedicationDraftMapper.fromOcr(
            fields = BoxFields(
                name = "MABOdol",
                strengthDoseText = "120",
                strengthUnit = "ml",
                strengthText = "120 ml"
            ),
            cn = null
        )

        assertEquals("MABOdol", draft.name)
        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
        assertEquals("120 ml", draft.strengthText)
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

    @Test
    fun `el ocr separa la presentacion de la dosis`() {
        val draft = MedicationDraftMapper.fromOcr(
            fields = BoxFields(
                name = "MABOdol",
                nameCandidates = listOf("MABOdol", "EFECTO"),
                nameConfident = true,
                presentationText = "120 ml"
            ),
            cn = null
        )

        assertEquals("MABOdol", draft.name)
        assertEquals(listOf("MABOdol", "EFECTO"), draft.nameCandidates)
        assertTrue(draft.nameConfident)
        assertEquals("120 ml", draft.presentation)
        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
        assertNull(draft.strengthText)
    }

    @Test
    fun `el ocr no usa un porcentaje como dosis`() {
        val draft = MedicationDraftMapper.fromOcr(
            fields = BoxFields(
                name = "Producto",
                strengthDoseText = "10",
                strengthUnit = "%",
                strengthText = "10%"
            ),
            cn = null
        )

        assertEquals("Producto", draft.name)
        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
        assertEquals("10%", draft.strengthText)
    }
}
