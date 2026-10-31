package com.andres.pillremind.data.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del contenedor de copia y del cifrado opcional.
 */
class BackupCryptoTest {

    private val payload = "contenido de prueba del backup".toByteArray()

    @Test
    fun `cabecera sin cifrado conserva el payload`() {
        val output = ByteArrayOutputStream()
        BackupCrypto.writeHeader(output, encrypted = false)
        output.write(payload)

        val input = ByteArrayInputStream(output.toByteArray())
        val header = BackupCrypto.readHeader(input)
        val rest = input.readBytes()

        assertFalse(header.encrypted)
        assertArrayEquals(payload, rest)
    }

    @Test
    fun `ida y vuelta con contrasena`() {
        val password = "clave-segura-123".toCharArray()
        val output = ByteArrayOutputStream()

        BackupCrypto.writeHeader(output, encrypted = true)
        BackupCrypto.encryptingStream(output, password).use { stream ->
            stream.write(payload)
        }

        val input = ByteArrayInputStream(output.toByteArray())
        val header = BackupCrypto.readHeader(input)
        val decrypted = BackupCrypto.decryptingStream(input, password).use { it.readBytes() }

        assertTrue(header.encrypted)
        assertArrayEquals(payload, decrypted)
    }

    @Test
    fun `contrasena incorrecta no descifra`() {
        val output = ByteArrayOutputStream()
        BackupCrypto.writeHeader(output, encrypted = true)
        BackupCrypto.encryptingStream(output, "clave-correcta".toCharArray()).use { stream ->
            stream.write(payload)
        }

        val bytes = output.toByteArray()
        val result = runCatching {
            val input = ByteArrayInputStream(bytes)
            BackupCrypto.readHeader(input)
            BackupCrypto.decryptingStream(input, "clave-incorrecta".toCharArray())
                .use { it.readBytes() }
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun `payload manipulado no descifra`() {
        val password = "clave-segura-123".toCharArray()
        val output = ByteArrayOutputStream()
        BackupCrypto.writeHeader(output, encrypted = true)
        BackupCrypto.encryptingStream(output, password).use { stream ->
            stream.write(payload)
        }

        val bytes = output.toByteArray()
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 0x01).toByte()

        val result = runCatching {
            val input = ByteArrayInputStream(bytes)
            BackupCrypto.readHeader(input)
            BackupCrypto.decryptingStream(input, password).use { it.readBytes() }
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun `rechaza un archivo que no es una copia`() {
        val result = runCatching {
            BackupCrypto.readHeader(ByteArrayInputStream("otro archivo".toByteArray()))
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BackupException)
    }
}
