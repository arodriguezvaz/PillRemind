package com.andres.pillremind.data.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

/**
 * Copia una imagen desde una [Uri] a la memoria interna del dispositivo.
 */
fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        file.absolutePath
    } catch (e: Exception) {
        Log.e("ImageStorage", "Error copying image to internal storage", e)
        null
    }
}