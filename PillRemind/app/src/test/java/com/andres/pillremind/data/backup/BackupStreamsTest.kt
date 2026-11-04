package com.andres.pillremind.data.backup

import java.io.ByteArrayInputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del límite de lectura de copias de seguridad.
 */
class BackupStreamsTest {

    @Test
    fun `lee el contenido cuando cabe en el limite`() {
        val input = ByteArrayInputStream(byteArrayOf(1, 2, 3, 4))

        val result = input.readBytesLimited(4)

        assertArrayEquals(byteArrayOf(1, 2, 3, 4), result)
    }

    @Test
    fun `rechaza un flujo que supera el limite`() {
        val input = ByteArrayInputStream(ByteArray(10) { 7 })

        val error = assertThrows(BackupException::class.java) {
            input.readBytesLimited(5)
        }

        assertEquals(BackupError.TOO_LARGE, error.error)
    }

    @Test
    fun `un limite invalido se rechaza`() {
        val error = assertThrows(BackupException::class.java) {
            ByteArrayInputStream(byteArrayOf(1)).readBytesLimited(0)
        }

        assertEquals(BackupError.TOO_LARGE, error.error)
    }

    @Test
    fun `el limite por defecto deja margen sobre el ZIP maximo`() {
        assertTrue(MAX_BACKUP_PAYLOAD_BYTES > 50L * 1024 * 1024)
    }
}
