package com.andres.pillremind.domain.parsing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del parser de la caja.
 */
class BoxTextParserTest {

    private val nasonexText = """
        70099733
        663526.1o
        Nasonex
        50 microgramos
        sUspensión para
        pulverización nasal
        furoato de mometasona
        Vía intranasal.
        Agitar con cuidado
        ántes de usar.
        Leer el prospecto antes
        de utilizar
        este medicamento.
        140
        pulverizaciones, 1 frasco
        -ig ORGANON
    """.trimIndent()

    private val tritabText = """
        22808
        Tritab1)
        MHO NOISNBASNS 30
        200 mg/5 ml
        POLVO PARA PREPARAR 1
        DE SUSPENSION ORAL
        SIDUS
        FORMULA: Cada 100 ml de suspensión
        reconstituida contiene 200 mg de
        Azitromicina base
        POSOLOGIA: Ver prospecto adjunto.
    """.trimIndent()

    @Test
    fun `nasonex extrae nombre concentracion forma y toma`() {
        val fields = BoxTextParser.parse(nasonexText)

        assertEquals("Nasonex", fields.name)
        assertEquals("50", fields.strengthDoseText)
        assertEquals("mcg", fields.strengthUnit)
        assertEquals("50 microgramos", fields.strengthText)
        assertEquals("pulverización", fields.form)
        assertEquals("1", fields.intakeDoseText)
        assertEquals("puffs", fields.intakeUnit)
    }

    @Test
    fun `tritab extrae nombre concentracion y forma liquida`() {
        val fields = BoxTextParser.parse(tritabText)

        assertEquals("Tritab", fields.name)
        assertEquals("200", fields.strengthDoseText)
        assertEquals("mg", fields.strengthUnit)
        assertEquals("200 mg/5 ml", fields.strengthText)
        assertEquals("polvo", fields.form)
        assertNull(fields.intakeDoseText)
    }

    @Test
    fun `extrae el nombre cuando todo esta en la misma linea`() {
        val fields = BoxTextParser.parse("IBUPROFENO 600 MG COMPRIMIDOS")

        assertEquals("IBUPROFENO", fields.name)
        assertEquals("600", fields.strengthDoseText)
        assertEquals("mg", fields.strengthUnit)
        assertEquals("comprimidos", fields.form)
        assertEquals("1", fields.intakeDoseText)
        assertEquals("comprimidos", fields.intakeUnit)
    }

    @Test
    fun `extrae la toma cuando aparece de forma explicita`() {
        val fields = BoxTextParser.parse(
            """
                1 comprimido cada 8 horas
                500 mg
            """.trimIndent()
        )

        assertEquals("1", fields.intakeDoseText)
        assertEquals("comprimidos", fields.intakeUnit)
        assertEquals("500", fields.strengthDoseText)
        assertEquals("mg", fields.strengthUnit)
    }

    @Test
    fun `no inventa datos en el reverso con solo el CN`() {
        val fields = BoxTextParser.parse(
            """
                C.N. 215936.5
                8 470002 159365
            """.trimIndent()
        )

        assertNull(fields.name)
        assertNull(fields.strengthDoseText)
        assertNull(fields.form)
    }

    @Test
    fun `no usa prosa de la caja como nombre`() {
        val fields = BoxTextParser.parse(
            """
                ojos y mucosas. Mantener
                C.N. 205642.8
                8 470002 056428
            """.trimIndent()
        )

        assertNull(fields.name)
    }

    @Test
    fun `paracetamol en gramos`() {
        val fields = BoxTextParser.parse("PARACETAMOL 1 G COMPRIMIDOS")

        assertEquals("PARACETAMOL", fields.name)
        assertEquals("1", fields.strengthDoseText)
        assertEquals("g", fields.strengthUnit)
        assertEquals("comprimidos", fields.form)
    }

    @Test
    fun `mabodol elige el nombre grande y no la marca pequena`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "EFECTO\nCANNABIDIOL\nMABOdol\n120 ml",
                lines = listOf(
                    ocrLine("EFECTO", 0.04f, 0.03f, 0.18f, 0.07f),
                    ocrLine("CANNABIDIOL", 0.04f, 0.12f, 0.30f, 0.16f),
                    ocrLine("MABOdol", 0.30f, 0.30f, 0.75f, 0.42f),
                    ocrLine("120 ml", 0.04f, 0.70f, 0.20f, 0.74f)
                )
            )
        )

        assertEquals("MABOdol", fields.name)
        assertTrue(fields.nameConfident)
        assertTrue(fields.nameCandidates.contains("MABOdol"))
        assertFalse(fields.nameCandidates.contains("CANNABIDIOL"))
        assertNull(fields.strengthDoseText)
        assertNull(fields.strengthText)
        assertEquals("120 ml", fields.presentationText)
        assertNull(fields.intakeDoseText)
    }

    @Test
    fun `dos nombres parecidos dejan el nombre vacio y candidatos`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "ALFA\nBETA",
                lines = listOf(
                    ocrLine("ALFA", 0.20f, 0.30f, 0.50f, 0.42f),
                    ocrLine("BETA", 0.20f, 0.50f, 0.50f, 0.62f)
                )
            )
        )

        assertNull(fields.name)
        assertFalse(fields.nameConfident)
        assertEquals(setOf("ALFA", "BETA"), fields.nameCandidates.take(2).toSet())
    }

    @Test
    fun `sin geometria el ocr no rellena el nombre`() {
        val fields = BoxTextParser.parse(
            OcrResult(text = "Nasonex\n50 microgramos\nsuspensión para pulverización nasal")
        )

        assertNull(fields.name)
        assertFalse(fields.nameConfident)
        assertTrue(fields.nameCandidates.contains("Nasonex"))
    }

    @Test
    fun `un unico texto pequeno arriba no se da por seguro`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "EFECTO",
                lines = listOf(ocrLine("EFECTO", 0.04f, 0.03f, 0.18f, 0.07f))
            )
        )

        assertNull(fields.name)
        assertFalse(fields.nameConfident)
        assertEquals(listOf("EFECTO"), fields.nameCandidates)
    }

    private fun ocrLine(
        text: String,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) = OcrLine(text, left, top, right, bottom)
}
