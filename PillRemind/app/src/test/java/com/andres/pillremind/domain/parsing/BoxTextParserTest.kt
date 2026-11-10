package com.andres.pillremind.domain.parsing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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

    /**
     * Una marca de 8 % de alto sigue siendo pequeña para la franja superior,
     * pero la penalización no basta para bajar de `MIN_NAME_SCORE`. Si no hay
     * otro nombre, la app lo rellenaría sin enseñar candidatos.
     */
    @Test
    fun `una marca pequena de la franja superior no se da por segura`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "EFECTO",
                lines = listOf(ocrLine("EFECTO", 0.04f, 0.02f, 0.30f, 0.10f))
            )
        )

        assertNull(fields.name)
        assertFalse(fields.nameConfident)
        assertEquals(listOf("EFECTO"), fields.nameCandidates)
    }

    /**
     * El filtro de descriptores se aplica con `matches`, así que solo descarta
     * la línea que contiene exactamente una palabra. Un descriptor compuesto
     * sigue siendo candidato y, si es grande, se elige como nombre.
     */
    @Test
    fun `los descriptores compuestos no se ofrecen como nombre`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "EFECTO\nCANNABIDIOL HIDRÓFILA\n120 ml",
                lines = listOf(
                    ocrLine("EFECTO", 0.04f, 0.02f, 0.18f, 0.06f),
                    ocrLine("CANNABIDIOL HIDRÓFILA", 0.10f, 0.30f, 0.90f, 0.44f),
                    ocrLine("120 ml", 0.04f, 0.70f, 0.20f, 0.74f)
                )
            )
        )

        assertNotEquals("CANNABIDIOL HIDRÓFILA", fields.name)
        assertFalse(fields.nameCandidates.any { it.contains("CANNABIDIOL", true) })
    }

    /**
     * Solo se descarta la línea exacta que ha ganado como `formLine`. Otra
     * línea de forma farmacéutica sigue siendo candidata y puede acabar como
     * nombre si es la más grande.
     */
    @Test
    fun `las lineas de forma farmaceutica no se ofrecen como nombre`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "EFECTO\nCOMPRIMIDOS RECUBIERTOS\nCOMPRIMIDOS",
                lines = listOf(
                    ocrLine("EFECTO", 0.04f, 0.02f, 0.18f, 0.06f),
                    ocrLine("COMPRIMIDOS RECUBIERTOS", 0.10f, 0.30f, 0.90f, 0.40f),
                    ocrLine("COMPRIMIDOS", 0.20f, 0.50f, 0.80f, 0.60f)
                )
            )
        )

        assertNotEquals("COMPRIMIDOS", fields.name)
        assertFalse(fields.nameCandidates.contains("COMPRIMIDOS"))
    }

    /**
     * Con una sola línea grande que contiene nombre, concentración y forma, el
     * fallback ya sabe extraer el nombre. Al exigir geometría se descarta ese
     * fallback y el nombre queda vacío aunque el texto sea inequívoco.
     */
    @Test
    fun `una sola linea grande con nombre concentracion y forma conserva el nombre`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "IBUPROFENO 600 MG COMPRIMIDOS",
                lines = listOf(
                    ocrLine("IBUPROFENO 600 MG COMPRIMIDOS", 0.10f, 0.30f, 0.90f, 0.42f)
                )
            )
        )

        assertEquals("IBUPROFENO", fields.name)
        assertTrue(fields.nameConfident)
    }

    /**
     * Un nombre grande y único, con marca registrada, debe rellenarse sin
     * depender de candidatos.
     */
    @Test
    fun `un unico nombre grande con marca registrada se da por seguro`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "MABOdol®",
                lines = listOf(ocrLine("MABOdol®", 0.10f, 0.30f, 0.60f, 0.45f))
            )
        )

        assertEquals("MABOdol", fields.name)
        assertTrue(fields.nameConfident)
        assertTrue(fields.nameCandidates.contains("MABOdol"))
    }

    /**
     * Las líneas con caja parcial no deben hacer que se ignore el resto del
     * texto: el motor devuelve `text` completo aunque alguna línea no tenga
     * `boundingBox`.
     */
    @Test
    fun `una linea sin caja no impide usar el nombre del texto completo`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "MABOdol\n120 ml",
                lines = listOf(ocrLine("120 ml", 0.04f, 0.70f, 0.20f, 0.74f))
            )
        )

        assertFalse(fields.nameConfident)
        assertTrue(
            "el nombre del texto completo debe ofrecerse como candidato",
            fields.nameCandidates.contains("MABOdol")
        )
    }

    /**
     * `10%` en una línea suelta se descarta como ruido porque tiene más dígitos
     * que letras. Es una concentración válida y debe conservarse como tal.
     */
    @Test
    fun `un porcentaje suelto se conserva como concentracion`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "MABOdol\n10%",
                lines = listOf(
                    ocrLine("MABOdol", 0.10f, 0.30f, 0.50f, 0.42f),
                    ocrLine("10%", 0.10f, 0.50f, 0.22f, 0.54f)
                )
            )
        )

        assertEquals("10", fields.strengthDoseText)
        assertEquals("%", fields.strengthUnit)
        assertEquals("10%", fields.strengthText)
    }

    /**
     * El mismo porcentaje con un espacio sí supera el filtro de ruido; así se
     * comprueba que la regla de dosis no lo usa en ningún caso.
     */
    @Test
    fun `un porcentaje legible no se usa como dosis`() {
        val fields = BoxTextParser.parse("MABOdol\n10 %")
        val draft = MedicationDraftMapper.fromOcr(fields, cn = null)

        assertEquals("10", fields.strengthDoseText)
        assertEquals("%", fields.strengthUnit)
        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
    }

    /**
     * Un volumen suelto es una presentación, no una toma. Debe conservarse
     * como presentación y no rellenar dosis ni unidad.
     */
    @Test
    fun `un volumen suelto se guarda como presentacion y no como dosis`() {
        val fields = BoxTextParser.parse("MABOdol\n5 ml")
        val draft = MedicationDraftMapper.fromOcr(fields, cn = null)

        assertEquals("5 ml", fields.presentationText)
        assertNull(fields.strengthDoseText)
        assertNull(fields.intakeDoseText)
        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
    }

    /**
     * Una concentración por mililitro no es una toma. Debe conservarse como
     * concentración y el mapeador no debe usarla como dosis.
     */
    @Test
    fun `una concentracion por mililitro no se usa como dosis`() {
        val fields = BoxTextParser.parse("MABOdol\n6 mg/ml")
        val draft = MedicationDraftMapper.fromOcr(fields, cn = null)

        assertEquals("6", fields.strengthDoseText)
        assertEquals("mg", fields.strengthUnit)
        assertEquals("6 mg/ml", fields.strengthText)
        assertEquals("", draft.doseText)
        assertEquals("", draft.unit)
    }

    /**
     * "gotas" empieza por "g", así que el regex de concentración puede leer
     * "5 g". La toma explícita debe seguir siendo 5 gotas y no inventar gramos.
     */
    @Test
    fun `las gotas no se confunden con gramos`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "5 gotas",
                lines = listOf(ocrLine("5 gotas", 0.10f, 0.30f, 0.40f, 0.40f))
            )
        )

        assertNull(fields.strengthText)
        assertNull(fields.strengthDoseText)
        assertNull(fields.strengthUnit)
        assertEquals("5", fields.intakeDoseText)
        assertEquals("gotas", fields.intakeUnit)
    }

    /**
     * El tamaño del envase ("20 comprimidos") no es una toma. La pauta por
     * defecto de un comprimido debe prevalecer frente al recuento del envase.
     */
    @Test
    fun `el tamano del envase no se usa como toma`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "IBUPROFENO 600 MG 20 COMPRIMIDOS",
                lines = listOf(
                    ocrLine("IBUPROFENO 600 MG 20 COMPRIMIDOS", 0.10f, 0.30f, 0.90f, 0.42f)
                )
            )
        )

        assertEquals("1", fields.intakeDoseText)
        assertEquals("comprimidos", fields.intakeUnit)
    }

    @Test
    fun `limpia comillas al final del nombre`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "IRBOSYN'\"",
                lines = listOf(
                    ocrLine("IRBOSYN'\"", 0.10f, 0.30f, 0.90f, 0.42f)
                )
            )
        )

        assertEquals("IRBOSYN", fields.name)
    }

    @Test
    fun `la concentracion compuesta no arrastra texto anterior`() {
        val fields = BoxTextParser.parse(
            OcrResult(
                text = "Silvederma\nA 10 mg/g Crema",
                lines = listOf(
                    ocrLine("Silvederma", 0.10f, 0.30f, 0.55f, 0.42f),
                    ocrLine("A 10 mg/g Crema", 0.10f, 0.50f, 0.60f, 0.60f)
                )
            )
        )

        assertEquals("Silvederma", fields.name)
        assertEquals("10 mg/g Crema", fields.strengthText)
    }

    private fun ocrLine(
        text: String,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) = OcrLine(text, left, top, right, bottom)
}
