package com.andres.pillremind.data.backup

import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Gestiona la cabecera del contenedor de copia y el cifrado opcional.
 *
 * Formato de la cabecera (texto plano):
 * - 4 bytes con el magic `PRBK`.
 * - 1 byte con la versión del contenedor.
 * - 1 byte de flags; el bit 0 indica que el payload está cifrado.
 *
 * Si el payload está cifrado, a continuación se escriben la sal (16 bytes) y el
 * IV (12 bytes). El resto es el ZIP cifrado con AES-256-GCM.
 */
object BackupCrypto {

    /** Versión del contenedor de copia. */
    const val CONTAINER_VERSION = 1

    private const val FLAG_ENCRYPTED = 1
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val TAG_BITS = 128
    private const val ITERATIONS = 210_000
    private const val KEY_BITS = 256
    private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"

    private val magic = byteArrayOf(
        'P'.code.toByte(),
        'R'.code.toByte(),
        'B'.code.toByte(),
        'K'.code.toByte()
    )

    /**
     * Datos leídos de la cabecera del contenedor.
     *
     * @property encrypted Indica si el payload requiere contraseña.
     */
    data class Header(val encrypted: Boolean)

    /**
     * Escribe la cabecera del contenedor.
     *
     * @param output Flujo de salida del archivo.
     * @param encrypted Indica si el payload se va a cifrar.
     */
    fun writeHeader(output: OutputStream, encrypted: Boolean) {
        output.write(magic)
        output.write(CONTAINER_VERSION)
        output.write(if (encrypted) FLAG_ENCRYPTED else 0)
    }

    /**
     * Lee y valida la cabecera del contenedor.
     *
     * @throws BackupException Si el archivo no es una copia compatible.
     */
    fun readHeader(input: InputStream): Header {
        val fileMagic = readFully(input, magic.size)
        if (!fileMagic.contentEquals(magic)) {
            throw BackupException("El archivo no es una copia de seguridad de PillRemind")
        }

        val version = readFully(input, 1)[0].toInt() and 0xFF
        if (version != CONTAINER_VERSION) {
            throw BackupException("Versión de copia no compatible")
        }

        val flags = readFully(input, 1)[0].toInt() and 0xFF
        if ((flags and FLAG_ENCRYPTED.inv()) != 0) {
            throw BackupException("Copia de seguridad no compatible")
        }

        return Header(encrypted = (flags and FLAG_ENCRYPTED) != 0)
    }

    /**
     * Escribe sal e IV y devuelve el flujo que cifra el payload.
     *
     * El flujo devuelto debe cerrarse para que se escriba la etiqueta GCM.
     */
    fun encryptingStream(output: OutputStream, password: CharArray): OutputStream {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        output.write(salt)
        output.write(iv)

        val cipher = Cipher.getInstance(CIPHER_ALGORITHM).apply {
            init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(deriveKey(password, salt), KEY_ALGORITHM),
                GCMParameterSpec(TAG_BITS, iv)
            )
        }
        return CipherOutputStream(output, cipher)
    }

    /**
     * Lee sal e IV y devuelve el flujo que descifra el payload.
     *
     * Si la contraseña es incorrecta, el error aparece al leer o cerrar el
     * flujo, cuando se valida la etiqueta GCM.
     */
    fun decryptingStream(input: InputStream, password: CharArray): InputStream {
        val salt = readFully(input, SALT_SIZE)
        val iv = readFully(input, IV_SIZE)

        val cipher = Cipher.getInstance(CIPHER_ALGORITHM).apply {
            init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(deriveKey(password, salt), KEY_ALGORITHM),
                GCMParameterSpec(TAG_BITS, iv)
            )
        }
        return CipherInputStream(input, cipher)
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun readFully(input: InputStream, size: Int): ByteArray {
        val buffer = ByteArray(size)
        var offset = 0
        while (offset < size) {
            val read = input.read(buffer, offset, size - offset)
            if (read == -1) {
                throw BackupException("El archivo de copia está incompleto")
            }
            offset += read
        }
        return buffer
    }
}
