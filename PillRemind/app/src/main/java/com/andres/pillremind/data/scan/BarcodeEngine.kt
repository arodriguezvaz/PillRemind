package com.andres.pillremind.data.scan

import android.net.Uri

/**
 * Código detectado en una imagen.
 *
 * @property raw Valor tal y como lo devuelve el lector.
 * @property format Formato del código (EAN-13, DataMatrix, QR...).
 */
data class ScannedCode(
    val raw: String,
    val format: Int
)

/**
 * Lee códigos de barras y códigos 2D de una imagen.
 */
interface BarcodeEngine {
    suspend fun scan(uri: Uri): List<ScannedCode>
}
