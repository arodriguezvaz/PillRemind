package com.andres.pillremind.data.backup

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Empaqueta y desempaqueta el ZIP interno de la copia de seguridad.
 *
 * Estructura:
 * - `backup.json`
 * - `images/img_0001.jpg`, `images/img_0002.png`, ...
 *
 * Se aplican límites de tamaño y se rechazan entradas fuera de la estructura
 * esperada para evitar archivos maliciosos o bombas de descompresión.
 */
object BackupArchive {

    /** Nombre del archivo JSON dentro del ZIP. */
    const val JSON_ENTRY = "backup.json"

    private const val MAX_JSON_BYTES = 10L * 1024 * 1024
    private const val MAX_TOTAL_BYTES = 50L * 1024 * 1024
    private const val MAX_IMAGES = 500
    private const val BUFFER_SIZE = 8 * 1024

    private val imageNameRegex =
        Regex("^images/[A-Za-z0-9._-]+\\.(jpg|jpeg|png|webp)$", RegexOption.IGNORE_CASE)

    /**
     * Contenido leído del ZIP.
     *
     * @property json Texto de `backup.json`.
     * @property images Imágenes indexadas por su ruta relativa.
     */
    data class Contents(
        val json: String,
        val images: Map<String, ByteArray>
    )

    /**
     * Escribe el ZIP con el JSON y las imágenes indicadas.
     *
     * @param output Flujo de salida, posiblemente cifrado.
     * @param json Contenido de `backup.json`.
     * @param images Mapa de ruta relativa a archivo de imagen.
     * @throws BackupException Si la copia supera los límites soportados.
     */
    fun write(output: OutputStream, json: String, images: Map<String, File>) {
        val jsonBytes = json.toByteArray(Charsets.UTF_8)
        if (jsonBytes.size > MAX_JSON_BYTES) {
            throw BackupException(
                BackupError.TOO_LARGE,
                "El archivo de datos de la copia es demasiado grande"
            )
        }
        if (images.size > MAX_IMAGES) {
            throw BackupException(
                BackupError.TOO_LARGE,
                "La copia contiene demasiadas imágenes"
            )
        }
        val totalBytes = jsonBytes.size.toLong() + images.values.sumOf { it.length() }
        if (totalBytes > MAX_TOTAL_BYTES) {
            throw BackupException(
                BackupError.TOO_LARGE,
                "La copia supera el tamaño máximo permitido"
            )
        }

        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            zip.putNextEntry(ZipEntry(JSON_ENTRY))
            zip.write(jsonBytes)
            zip.closeEntry()

            images.forEach { (name, file) ->
                zip.putNextEntry(ZipEntry(name))
                file.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    /**
     * Lee el ZIP y devuelve el JSON y las imágenes.
     *
     * @throws BackupException Si faltan archivos, hay entradas no permitidas o
     * se superan los límites de tamaño.
     */
    fun read(input: InputStream): Contents {
        ZipInputStream(BufferedInputStream(input)).use { zip ->
            var json: String? = null
            val images = mutableMapOf<String, ByteArray>()
            val entryNames = mutableSetOf<String>()
            var totalBytes = 0L

            while (true) {
                val entry = zip.nextEntry ?: break
                val name = entry.name

                if (entry.isDirectory) {
                    zip.closeEntry()
                    continue
                }
                if (!entryNames.add(name)) {
                    throw BackupException(
                        BackupError.CORRUPTED,
                        "La copia contiene entradas duplicadas"
                    )
                }

                when {
                    name == JSON_ENTRY -> {
                        if (json != null) {
                            throw BackupException(
                                BackupError.CORRUPTED,
                                "La copia contiene varios archivos de datos"
                            )
                        }
                        val remaining = MAX_JSON_BYTES.coerceAtMost(MAX_TOTAL_BYTES - totalBytes)
                        val bytes = readEntry(zip, remaining)
                        totalBytes += bytes.size
                        json = bytes.toString(Charsets.UTF_8)
                    }

                    imageNameRegex.matches(name) && !name.contains("..") -> {
                        if (images.size >= MAX_IMAGES) {
                            throw BackupException(
                                BackupError.TOO_LARGE,
                                "La copia contiene demasiadas imágenes"
                            )
                        }
                        val remaining = MAX_TOTAL_BYTES - totalBytes
                        val bytes = readEntry(zip, remaining)
                        totalBytes += bytes.size
                        images[name] = bytes
                    }

                    else -> throw BackupException(
                        BackupError.CORRUPTED,
                        "La copia contiene archivos no permitidos"
                    )
                }

                zip.closeEntry()
            }

            val content = json ?: throw BackupException(
                BackupError.CORRUPTED,
                "La copia no contiene el archivo de datos"
            )
            return Contents(content, images)
        }
    }

    private fun readEntry(zip: ZipInputStream, maxBytes: Long): ByteArray {
        if (maxBytes <= 0) {
            throw BackupException(
                BackupError.TOO_LARGE,
                "La copia es demasiado grande"
            )
        }

        val output = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        var total = 0L

        while (true) {
            val read = zip.read(buffer)
            if (read == -1) break
            total += read
            if (total > maxBytes) {
                throw BackupException(
                    BackupError.TOO_LARGE,
                    "La copia es demasiado grande"
                )
            }
            output.write(buffer, 0, read)
        }

        return output.toByteArray()
    }
}
