package com.andres.pillremind.data.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

/**
 * Copia una imagen seleccionada mediante un [Uri] al almacenamiento interno
 * de la aplicación y devuelve la ruta absoluta del archivo generado.
 *
 * Se crea un nombre único basado en la marca temporal actual para evitar
 * colisiones con imágenes existentes.
 *
 * @param context Contexto de Android utilizado para acceder al ContentResolver,
 * que permite abrir el Uri de la imagen, y también para acceder al
 * almacenamiento interno de la aplicación donde se guardará el archivo.
 * @param uri Uri de la imagen seleccionada por el usuario.
 *
 * @return Ruta absoluta del archivo copiado o `null` si ocurre algún error
 * durante el proceso.
 */
fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
// Obtiene un InputStream para leer el contenido de la imagen seleccionada.
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        // Genera un nombre único para la imagen usando la fecha actual.
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        // Crea el archivo dentro del almacenamiento interno de la aplicación.
        val file = File(context.filesDir, fileName)
        // Copia el contenido de la imagen al nuevo archivo.
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        // Devuelve la ruta absoluta del archivo guardado.
        file.absolutePath
    } catch (e: Exception) {
        // Registra el error y devuelve null si falla la copia.
        Log.e("ImageStorage", "Error copying image to internal storage", e)
        null
    }
}