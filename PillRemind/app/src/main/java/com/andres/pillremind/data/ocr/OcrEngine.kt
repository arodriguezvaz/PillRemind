package com.andres.pillremind.data.ocr

import android.net.Uri
import com.andres.pillremind.domain.parsing.OcrResult

/**
 * Reconoce el texto de una imagen.
 */
interface OcrEngine {
    suspend fun recognize(uri: Uri): OcrResult?
}
