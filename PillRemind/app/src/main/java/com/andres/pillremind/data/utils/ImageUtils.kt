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
 * colisiones con imágenes existentes. Si la copia falla, se borra el archivo
 * parcial para no dejar restos en el almacenamiento.
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
    val file = File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg")
    return try {
        // `use` garantiza que el InputStream se cierre también si falla la copia.
        val copied = context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            true
        } ?: false
        if (copied) file.absolutePath else null
    } catch (e: Exception) {
        // Registra el error y elimina el archivo a medias si llegó a crearse.
        Log.e("ImageStorage", "Error copying image to internal storage", e)
        file.delete()
        null
    }
}
