package com.andres.pillremind.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.math.roundToInt

/**
 * Decodifica una imagen de la galería o de la cámara para pasarla a ML Kit.
 *
 * Reduce el lado mayor a [maxDimension] antes de decodificar, aplica la
 * orientación EXIF y devuelve siempre un bitmap software. Así el OCR y el
 * lector de códigos reciben la foto en la orientación correcta y sin ocupar
 * memoria de más.
 */
object BitmapLoader {

    /**
     * Decodifica [uri] con un tamaño máximo de [maxDimension] píxeles por lado.
     *
     * @return El bitmap listo para ML Kit, o `null` si la imagen no se puede
     * abrir o decodificar.
     */
    fun decode(context: Context, uri: Uri, maxDimension: Int = MAX_DIMENSION): Bitmap? =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                decodeModern(context, uri, maxDimension)
            } else {
                decodeLegacy(context, uri, maxDimension)
            }
        }.getOrNull()

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeModern(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false

            val longest = maxOf(info.size.width, info.size.height)
            if (longest > maxDimension) {
                val scale = maxDimension.toFloat() / longest
                decoder.setTargetSize(
                    (info.size.width * scale).roundToInt().coerceAtLeast(1),
                    (info.size.height * scale).roundToInt().coerceAtLeast(1)
                )
            }
        }
    }

    private fun decodeLegacy(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        } ?: return null

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sampleSize > maxDimension) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: return null

        val orientation = runCatching {
            resolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        return applyOrientation(decoded, orientation)
    }

    private fun applyOrientation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(-90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(-90f)
            else -> return source
        }

        val rotated = Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
        if (rotated !== source) source.recycle()
        return rotated
    }

    private const val MAX_DIMENSION = 2048
}
