package com.andres.pillremind.data.ocr

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.util.Log
import com.andres.pillremind.data.image.BitmapLoader
import com.andres.pillremind.domain.parsing.OcrLine
import com.andres.pillremind.domain.parsing.OcrResult
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación de [OcrEngine] con ML Kit Text Recognition.
 *
 * La imagen se decodifica ya reducida y orientada por [BitmapLoader], así el
 * modelo no recibe una foto girada ni ocupa memoria de más.
 */
@Singleton
class MlKitOcrEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : OcrEngine {

    override suspend fun recognize(uri: Uri): OcrResult? = withContext(Dispatchers.IO) {
        val bitmap = BitmapLoader.decode(context, uri) ?: return@withContext null
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            val recognized = Tasks.await(
                recognizer.process(InputImage.fromBitmap(bitmap, 0)),
                TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            val lines = toLines(recognized, bitmap.width, bitmap.height)
            if (isDebug) {
                Log.d(TAG, "OCR ${lines.size} líneas")
                lines.forEach { line ->
                    Log.d(
                        TAG,
                        "línea=\"${line.text}\" caja=(" +
                            "%.3f, %.3f, %.3f, %.3f)".format(
                                line.left,
                                line.top,
                                line.right,
                                line.bottom
                            )
                    )
                }
            }
            OcrResult(text = recognized.text, lines = lines)
        } catch (e: Exception) {
            null
        } finally {
            recognizer.close()
            bitmap.recycle()
        }
    }

    /**
     * Convierte los bloques de ML Kit en líneas con coordenadas normalizadas.
     * El parser descarta las líneas sin caja porque no puede puntuarlas.
     */
    private fun toLines(result: Text, width: Int, height: Int): List<OcrLine> {
        if (width <= 0 || height <= 0) return emptyList()
        val imageWidth = width.toFloat()
        val imageHeight = height.toFloat()
        return result.textBlocks.flatMap { block ->
            block.lines.mapNotNull { line ->
                val text = line.text.trim()
                val box = line.boundingBox
                if (text.isBlank() || box == null) {
                    null
                } else {
                    OcrLine(
                        text = text,
                        left = (box.left / imageWidth).coerceIn(0f, 1f),
                        top = (box.top / imageHeight).coerceIn(0f, 1f),
                        right = (box.right / imageWidth).coerceIn(0f, 1f),
                        bottom = (box.bottom / imageHeight).coerceIn(0f, 1f)
                    )
                }
            }
        }
    }

    private val isDebug: Boolean
        get() = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    private companion object {
        const val TIMEOUT_SECONDS = 20L
        const val TAG = "PillRemindOcr"
    }
}
