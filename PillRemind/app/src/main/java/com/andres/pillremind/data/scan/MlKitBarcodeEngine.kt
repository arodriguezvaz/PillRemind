package com.andres.pillremind.data.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import com.andres.pillremind.data.image.BitmapLoader
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación de [BarcodeEngine] con ML Kit Barcode Scanning.
 *
 * Lee los códigos que pueden identificar un medicamento español: EAN-13,
 * DataMatrix (CN en `(712)` o NTIN) y QR como extra. Si el primer intento no
 * encuentra nada, repite la lectura con la imagen invertida para los códigos
 * claros sobre fondo oscuro, habituales en algunos envases.
 */
@Singleton
class MlKitBarcodeEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : BarcodeEngine {

    override suspend fun scan(uri: Uri): List<ScannedCode> = withContext(Dispatchers.IO) {
        val bitmap = BitmapLoader.decode(context, uri) ?: return@withContext emptyList()
        val scanner = BarcodeScanning.getClient(options)

        try {
            val direct = process(scanner, bitmap)
            if (direct.isNotEmpty()) {
                direct
            } else {
                val inverted = invert(bitmap)
                try {
                    process(scanner, inverted)
                } finally {
                    inverted.recycle()
                }
            }
        } catch (e: Exception) {
            emptyList()
        } finally {
            scanner.close()
            bitmap.recycle()
        }
    }

    private fun process(scanner: BarcodeScanner, bitmap: Bitmap): List<ScannedCode> = Tasks.await(
        scanner.process(InputImage.fromBitmap(bitmap, 0)),
        TIMEOUT_SECONDS,
        TimeUnit.SECONDS
    ).mapNotNull { barcode ->
        barcode.rawValue
            ?.takeIf { it.isNotBlank() }
            ?.let { ScannedCode(it, barcode.format) }
    }

    private fun invert(source: Bitmap): Bitmap {
        val matrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(source, 0f, 0f, paint)
        return result
    }

    private companion object {
        const val TIMEOUT_SECONDS = 20L

        val options: BarcodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_QR_CODE
            )
            .build()
    }
}
