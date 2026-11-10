package com.andres.pillremind.domain.parsing

import com.andres.pillremind.domain.model.MedicationImportWarning
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del parser de la hoja de medicación activa.
 *
 * El texto de ejemplo es ficticio: no contiene datos de ningún paciente real.
 */
class MedicationListParserTest {

    private val sergasText = """
        Folla de Medicación Activa

        Paciente: PACIENTE DE PRUEBA - 0000000000
        DNI: 00000000A
        Nº da tarxeta sanitaria: 0000000000
        Data de nacemento: 01/01/1960
        Centro: CENTRO DE PRUEBA
        Médico de cabeceira: DOCTOR DE PRUEBA

        Tratamentos de longa duración
        RYALTRIS 25/600MCG/PULSACION 1 FRASCO 30ML (240 PULVERIZACIONES) SUSP PULV NASAL
        2 PULSACIONES CADA 24 HORAS Custo Trat/mes: 4.92 €

        RELVAR ELLIPTA 92/22MCG 30 DOSIS POLVO PARA INHALACION (UNIDOSIS)
        1 DOSIS CADA 24 HORAS Custo Trat/mes: 48.53 €

        Outros Tratamentos
        ENALAPRIL 5MG 28 COMPRIMIDOS
        1 COMPRIMIDO CADA 12 HORAS

        Vostede non ten que voltar a por receitas ata o: 17/01/2027
        Impreso o 10/11/2026 por diselpro
        Este documento é un resumo informativo e non substitúe a receita.
        Páxina 1 de 1
    """.trimIndent()

    @Test
    fun `interpreta la hoja del sergas y descarta los datos del paciente`() {
        val medications = MedicationListParser.parse(sergasText)

        assertEquals(3, medications.size)

        val ryaltr = medications[0]
        assertEquals("RYALTRIS 25/600MCG/PULSACION", ryaltr.name)
        assertEquals("2", ryaltr.doseText)
        assertEquals("pulsaciones", ryaltr.unit)
        assertEquals(1, ryaltr.everyNDays)
        assertEquals(listOf(LocalTime.of(9, 0)), ryaltr.times)
        assertTrue(ryaltr.isChronic)

        val relvar = medications[1]
        assertEquals("RELVAR ELLIPTA 92/22MCG", relvar.name)
        assertEquals("1", relvar.doseText)
        assertEquals("dosis", relvar.unit)
        assertTrue(relvar.isChronic)

        val enalapril = medications[2]
        assertEquals("ENALAPRIL 5MG", enalapril.name)
        assertEquals("comprimidos", enalapril.unit)
        assertEquals(listOf(LocalTime.of(9, 0), LocalTime.of(21, 0)), enalapril.times)
        assertFalse(enalapril.isChronic)
    }

    @Test
    fun `no incluye datos identificativos ni costes en el nombre`() {
        val names = MedicationListParser.parse(sergasText).map { it.name.lowercase() }

        assertFalse(names.any { "paciente" in it })
        assertFalse(names.any { "centro" in it })
        assertFalse(names.any { "doctor" in it })
        assertFalse(names.any { "custo" in it })
        assertFalse(names.any { "páxina" in it })
        assertFalse(names.any { "dni" in it })
        assertFalse(names.any { "nacemento" in it })
        assertFalse(names.any { "tarxeta" in it })
        assertFalse(names.any { "receita" in it })
    }

    @Test
    fun `interpreta cada 48 horas como cada dos dias`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            MEDICAMENTO DE PRUEBA 1MG
            1 COMPRIMIDO CADA 48 HORAS
            """.trimIndent()
        )

        assertEquals(1, medications.size)
        assertEquals(2, medications.single().everyNDays)
        assertEquals(listOf(LocalTime.of(9, 0)), medications.single().times)
    }

    @Test
    fun `interpreta ao dia sin intervalo explicito`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            MEDICAMENTO DE PRUEBA 5MG
            1 COMPRIMIDO AO DÍA
            """.trimIndent()
        )

        assertEquals(1, medications.size)
        assertEquals(listOf(LocalTime.of(9, 0)), medications.single().times)
        assertEquals(1, medications.single().everyNDays)
    }

    @Test
    fun `interpreta cada ocho horas con tres tomas`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            MEDICAMENTO DE PRUEBA 500MG
            1 COMPRIMIDO CADA 8 HORAS
            """.trimIndent()
        )

        assertEquals(
            listOf(LocalTime.of(8, 0), LocalTime.of(16, 0), LocalTime.of(0, 0)),
            medications.single().times
        )
    }

    @Test
    fun `marca la unidad desconocida y usa dosis como respaldo`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            MEDICAMENTO DE PRUEBA 10MG
            1 ENVASE CADA 24 HORAS
            """.trimIndent()
        )

        assertEquals(1, medications.size)
        assertEquals("dosis", medications.single().unit)
        assertTrue(MedicationImportWarning.UNKNOWN_UNIT in medications.single().warnings)
    }

    @Test
    fun `devuelve una lista vacia cuando no hay medicamentos`() {
        val medications = MedicationListParser.parse(
            """
            Folla de Medicación Activa
            Paciente: PACIENTE DE PRUEBA
            Información para o Paciente:
            Esta folla non é unha receita.
            """.trimIndent()
        )

        assertTrue(medications.isEmpty())
    }

    @Test
    fun `interpreta cada seis horas con cuatro tomas`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            MEDICAMENTO DE PRUEBA 500MG
            1 COMPRIMIDO CADA 6 HORAS
            """.trimIndent()
        )

        assertEquals(
            listOf(
                LocalTime.of(6, 0),
                LocalTime.of(12, 0),
                LocalTime.of(18, 0),
                LocalTime.of(0, 0)
            ),
            medications.single().times
        )
        assertEquals(1, medications.single().everyNDays)
    }

    @Test
    fun `interpreta cada veinticuatro horas como una toma diaria`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            MEDICAMENTO DE PRUEBA 20MG
            1 COMPRIMIDO CADA 24 HORAS
            """.trimIndent()
        )

        assertEquals(listOf(LocalTime.of(9, 0)), medications.single().times)
        assertEquals(1, medications.single().everyNDays)
    }

    @Test
    fun `interpreta cada setenta y dos horas como cada tres dias`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            METOTREXATO 2,5MG
            1 COMPRIMIDO CADA 72 HORAS
            """.trimIndent()
        )

        assertEquals(3, medications.single().everyNDays)
        assertEquals(listOf(LocalTime.of(9, 0)), medications.single().times)
    }

    @Test
    fun `convierte la dosis decimal con coma`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            MEDICAMENTO DE PRUEBA 5MG
            0,5 COMPRIMIDO AO DÍA
            """.trimIndent()
        )

        assertEquals("0.5", medications.single().doseText)
        assertEquals("comprimidos", medications.single().unit)
    }

    @Test
    fun `interpreta media pastilla como cero coma cinco y no como dos`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            ENALAPRIL 5MG 28 COMPRIMIDOS
            1/2 COMPRIMIDO CADA 12 HORAS
            """.trimIndent()
        )

        assertEquals(1, medications.size)
        assertEquals(
            "Media pastilla debe guardarse como 0.5, no como 2",
            "0.5",
            medications.single().doseText
        )
        assertEquals(listOf(LocalTime.of(9, 0), LocalTime.of(21, 0)), medications.single().times)
    }

    @Test
    fun `no usa datos de filiacion como nombre cuando el medicamento no trae concentracion`() {
        val medications = MedicationListParser.parse(
            """
            Folla de Medicación Activa
            TRATAMENTOS DE LONGA DURACIÓN
            OMEPRAZOL
            Data de nacemento: 01/01/1960
            1 CÁPSULA AO DÍA
            """.trimIndent()
        )

        assertEquals(1, medications.size)
        assertEquals(
            "El nombre debe ser el medicamento, nunca una línea de filiación",
            "OMEPRAZOL",
            medications.single().name
        )
    }

    @Test
    fun `recorta el sufijo efg de un generico con presentacion`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            ATORVASTATINA 10 MG EFG 28 COMPRIMIDOS RECUBIERTOS
            1 COMPRIMIDO AO DÍA
            """.trimIndent()
        )

        assertEquals("ATORVASTATINA 10 MG", medications.single().name)
    }

    @Test
    fun `conserva una concentracion de un gramo en el nombre`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            PARACETAMOL 1 G 20 COMPRIMIDOS
            1 COMPRIMIDO AO DÍA
            """.trimIndent()
        )

        assertEquals("PARACETAMOL 1 G", medications.single().name)
    }

    @Test
    fun `no inventa un medicamento cuando la pauta aparece sin nombre`() {
        val medications = MedicationListParser.parse(
            """
            Tratamentos de longa duración
            1 COMPRIMIDO AO DÍA
            """.trimIndent()
        )

        assertTrue(medications.isEmpty())
    }
}
