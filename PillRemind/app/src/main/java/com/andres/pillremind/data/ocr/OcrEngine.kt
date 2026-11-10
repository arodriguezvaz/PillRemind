package com.andres.pillremind.data.ocr

import android.net.Uri

/**
 * Reconoce el texto de una imagen.
 */
interface OcrEngine {
    suspend fun recognize(uri: Uri): String?
}
