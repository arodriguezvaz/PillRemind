package com.andres.pillremind.data.ocr

import android.content.Context
import android.net.Uri
import com.andres.pillremind.data.image.BitmapLoader
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
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

    override suspend fun recognize(uri: Uri): String? = withContext(Dispatchers.IO) {
        val bitmap = BitmapLoader.decode(context, uri) ?: return@withContext null
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            Tasks.await(
                recognizer.process(InputImage.fromBitmap(bitmap, 0)),
                TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            ).text
        } catch (e: Exception) {
            null
        } finally {
            recognizer.close()
            bitmap.recycle()
        }
    }

    private companion object {
        const val TIMEOUT_SECONDS = 20L
    }
}
