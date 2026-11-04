package com.andres.pillremind.data.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Tamaño máximo aceptado para una imagen de perfil o medicamento. */
internal const val MAX_IMAGE_BYTES = 20L * 1024 * 1024

/** Extensiones que la app sabe restaurar y mostrar. */
private val supportedMimeTypes = mapOf(
    "image/jpeg" to "jpg",
    "image/jpg" to "jpg",
    "image/png" to "png",
    "image/webp" to "webp",
)

/**
 * Copia una imagen seleccionada mediante un [Uri] al almacenamiento interno
 * de la aplicación y devuelve la ruta absoluta del archivo generado.
 *
 * La copia se hace fuera del hilo principal, con un nombre único (UUID) y
 * conservando la extensión real según el tipo MIME. Si el archivo supera
 * [MAX_IMAGE_BYTES] o falla la lectura, se elimina el archivo parcial y se
 * devuelve `null`.
 *
 * @param context Contexto de Android usado para acceder al `ContentResolver`.
 * @param uri Uri de la imagen elegida por el usuario.
 * @return Ruta absoluta del archivo copiado, o `null` si la copia falla.
 */
suspend fun copyImageToInternalStorage(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        val extension = resolveExtension(context, uri)
        val file = File(context.filesDir, "image_${UUID.randomUUID()}.$extension")

        try {
            val copied = context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    copyWithLimit(input, output, MAX_IMAGE_BYTES)
                }
                true
            } ?: false

            if (copied) {
                file.absolutePath
            } else {
                file.delete()
                null
            }
        } catch (e: Exception) {
            runCatching { file.delete() }
            Log.e("ImageStorage", "No se pudo copiar la imagen seleccionada", e)
            null
        }
    }

/**
 * Elimina una imagen del almacenamiento interno solo si está dentro de
 * `filesDir`. Se usa después de que la operación de base de datos haya
 * terminado correctamente.
 *
 * @param context Contexto de la aplicación.
 * @param path Ruta absoluta que se quiere borrar.
 */
fun deleteInternalImage(context: Context, path: String?) {
    if (path.isNullOrBlank()) return

    val file = File(path)
    if (isInsideFilesDir(context, file)) {
        runCatching { file.delete() }
    }
}

private fun resolveExtension(context: Context, uri: Uri): String {
    val mimeType = runCatching { context.contentResolver.getType(uri) }
        .getOrNull()
        ?.lowercase()

    return supportedMimeTypes[mimeType] ?: "jpg"
}

private fun copyWithLimit(input: InputStream, output: OutputStream, maxBytes: Long) {
    val buffer = ByteArray(8 * 1024)
    var total = 0L

    while (true) {
        val read = input.read(buffer)
        if (read == -1) break

        total += read
        if (total > maxBytes) {
            throw IOException("La imagen supera el tamaño máximo permitido")
        }
        output.write(buffer, 0, read)
    }
}

private fun isInsideFilesDir(context: Context, file: File): Boolean =
    runCatching {
        val root = context.filesDir.canonicalFile
        val target = file.canonicalFile
        target.path.startsWith(root.path + File.separator)
    }.getOrDefault(false)
