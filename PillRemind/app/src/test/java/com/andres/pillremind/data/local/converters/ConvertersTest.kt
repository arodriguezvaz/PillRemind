package com.andres.pillremind.data.local.converters

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas de precisión de las fechas guardadas por Room.
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `guarda las fechas con precision de minutos`() {
        val value = LocalDateTime.of(2026, 10, 31, 8, 15, 42, 123_000_000)

        val stored = converters.fromLocalDateTime(value)

        assertEquals("2026-10-31T08:15", stored)
    }

    @Test
    fun `recupera y normaliza fechas con segundos antiguos`() {
        val stored = "2026-10-31T08:15:42"

        val value = converters.toLocalDateTime(stored)

        assertEquals(LocalDateTime.of(2026, 10, 31, 8, 15), value)
    }

    @Test
    fun `mantiene los nulos`() {
        assertNull(converters.fromLocalDateTime(null))
        assertNull(converters.toLocalDateTime(null))
    }
}
