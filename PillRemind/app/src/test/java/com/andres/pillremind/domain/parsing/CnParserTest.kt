package com.andres.pillremind.domain.parsing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del extractor de CN a partir del texto de OCR.
 */
class CnParserTest {

    @Test
    fun `extrae el CN con la etiqueta C N y el digito de control`() {
        assertEquals(
            listOf("215936"),
            CnParser.extractCandidates("C.N. 215936.5")
        )
    }

    @Test
    fun `extrae el CN con la etiqueta CN`() {
        assertEquals(
            listOf("663526"),
            CnParser.extractCandidates("CN 663526.1")
        )
    }

    @Test
    fun `extrae el CN cuando el OCR anade una letra al final`() {
        assertEquals(
            listOf("663526"),
            CnParser.extractCandidates("663526.1o")
        )
    }

    @Test
    fun `extrae el CN sin digito de control`() {
        assertEquals(
            listOf("205642"),
            CnParser.extractCandidates("C.N. 205642")
        )
    }

    @Test
    fun `no confunde un EAN con un CN`() {
        assertTrue(
            CnParser.extractCandidates("EAN 8470007223214").isEmpty()
        )
    }

    @Test
    fun `devuelve una lista vacia si no hay CN`() {
        assertTrue(
            CnParser.extractCandidates("Sin código nacional en este texto").isEmpty()
        )
    }

    @Test
    fun `normaliza seis y siete digitos`() {
        assertEquals("663526", CnParser.normalize("663526"))
        assertEquals("663526", CnParser.normalize("6635261"))
        assertNull(CnParser.normalize("123"))
        assertNull(CnParser.normalize("abcdef"))
    }
}
