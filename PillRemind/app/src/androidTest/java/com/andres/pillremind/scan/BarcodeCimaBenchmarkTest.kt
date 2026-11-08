package com.andres.pillremind.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.andres.pillremind.domain.code.MedicineCodeParser
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmark del flujo "código del envase → CN → CIMA".
 *
 * Por defecto usa las imágenes de `androidTest/assets/barcodes` (EAN-13 y
 * DataMatrix generados). Si hay fotos reales en
 * `Android/data/com.andres.pillremind/files/barcodes`, se usan esas en su
 * lugar, lo que permite probar fotos de envases sin tocar el APK.
 *
 * Detecta los códigos con ML Kit, extrae el CN y consulta la API pública de
 * CIMA. Guarda el resultado en `barcode-results.json`.
 *
 * Para probar fotos reales:
 * ```
 * adb install -r app/build/outputs/apk/debug/app-debug.apk
 * adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
 * adb shell mkdir -p /sdcard/Android/data/com.andres.pillremind/files/barcodes
 * adb push fotos/. /sdcard/Android/data/com.andres.pillremind/files/barcodes/
 * adb shell am instrument -w \
 *   -e class com.andres.pillremind.scan.BarcodeCimaBenchmarkTest \
 *   com.andres.pillremind.test/androidx.test.runner.AndroidJUnitRunner
 * adb pull /sdcard/Android/data/com.andres.pillremind/files/barcodes/barcode-results.json
 * ```
 *
 * No uses `connectedDebugAndroidTest` si has empujado fotos reales: desinstala
 * la app y borra el directorio antes de que el test pueda leerlas.
 */
@RunWith(AndroidJUnit4::class)
class BarcodeCimaBenchmarkTest {

    private companion object {
        const val TAG = "BarcodeCima"
        const val MAX_DIMENSION = 2048
        const val TIMEOUT_SECONDS = 30L
        const val CIMA_URL = "https://cima.aemps.es/cima/rest/medicamento?cn="
    }

    @Test
    fun cimaRespondePorCn() {
        val found = lookupCima("720932")
        assertEquals(found.toString(), "found", found.optString("status"))
        assertTrue(
            "Nombre inesperado: $found",
            found.optString("nombre").contains("ESOMEPRAZOL", ignoreCase = true)
        )

        // 000000 no está asignado; 999999 sí (Ambroxol Ratiopharm), así que no
        // sirve como caso "no encontrado".
        val missing = lookupCima("000000")
        assertEquals(missing.toString(), "not_found", missing.optString("status"))

        val directory = resultsDirectory()
        File(directory, "cima-direct.json").writeText(
            JSONObject()
                .put("encontrado", found)
                .put("no_encontrado", missing)
                .toString(2)
        )
    }

    @Test
    fun escaneaCodigosDeEnvases() {
        val directory = resultsDirectory()
        val images = prepareImages(directory)

        require(images.isNotEmpty()) {
            "No hay imágenes en ${directory.absolutePath} ni en " +
                "androidTest/assets/barcodes."
        }

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_DATA_MATRIX)
            .build()
        val scanner = BarcodeScanning.getClient(options)
        val results = JSONArray()

        try {
            images.forEach { file ->
                val bitmap = decodeDownscaled(file)
                if (bitmap == null) {
                    results.put(JSONObject().put("file", file.name).put("error", "No se pudo decodificar"))
                    return@forEach
                }

                val start = System.currentTimeMillis()
                var barcodes = try {
                    Tasks.await(
                        scanner.process(InputImage.fromBitmap(bitmap, 0)),
                        TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                    )
                } catch (e: Exception) {
                    results.put(JSONObject().put("file", file.name).put("error", e.message))
                    bitmap.recycle()
                    return@forEach
                }
                var inverted = false
                if (barcodes.isEmpty()) {
                    val invertedBitmap = invert(bitmap)
                    barcodes = Tasks.await(
                        scanner.process(InputImage.fromBitmap(invertedBitmap, 0)),
                        TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                    )
                    invertedBitmap.recycle()
                    inverted = barcodes.isNotEmpty()
                }
                val elapsed = System.currentTimeMillis() - start

                val found = JSONArray()
                var hasCn = false
                barcodes.forEach { barcode ->
                    val raw = barcode.rawValue.orEmpty()
                    val cn = when (barcode.format) {
                        Barcode.FORMAT_EAN_13 -> MedicineCodeParser.fromEan13(raw)
                        Barcode.FORMAT_DATA_MATRIX -> MedicineCodeParser.fromGs1(raw)
                        else -> null
                    }
                    val entry = JSONObject()
                        .put("raw", raw)
                        .put("format", formatName(barcode.format))
                        .put("cn", cn ?: JSONObject.NULL)
                    if (cn != null) {
                        hasCn = true
                        entry.put("cima", lookupCima(cn))
                    }
                    found.put(entry)
                    Log.i(TAG, "${file.name}: $entry")
                }

                val ocr = if (!hasCn) runOcr(bitmap) else null
                results.put(
                    JSONObject()
                        .put("file", file.name)
                        .put("ms", elapsed)
                        .put("inverted", inverted)
                        .put("barcodes", found)
                        .put("ocr", ocr ?: JSONObject.NULL)
                )
                bitmap.recycle()
            }
        } finally {
            scanner.close()
        }

        File(directory, "barcode-results.json").writeText(results.toString(2))
        Log.i(TAG, "Resultados: ${results}")
    }

    /**
     * Consulta la API pública de CIMA por CN.
     *
     * Devuelve `found`, `not_found` (CIMA responde 200 con cuerpo vacío) o
     * `error`. No lanza excepciones para que el benchmark pueda continuar.
     */
    private fun lookupCima(cn: String): JSONObject {
        val start = System.currentTimeMillis()
        val connection = (URL(CIMA_URL + cn).openConnection() as HttpURLConnection).apply {
            connectTimeout = 7_000
            readTimeout = 7_000
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "PillRemind-BarcodeTest")
        }

        return try {
            val code = connection.responseCode
            val body = if (code == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            val result = JSONObject()
                .put("http", code)
                .put("ms", System.currentTimeMillis() - start)

            if (code != HttpURLConnection.HTTP_OK || body.isBlank()) {
                result.put("status", "not_found")
            } else {
                val json = JSONObject(body)
                result.put("status", "found")
                    .put("nregistro", json.optString("nregistro"))
                    .put("nombre", json.optString("nombre"))
                    .put("dosis", json.optString("dosis"))
                    .put(
                        "forma",
                        json.optJSONObject("formaFarmaceutica")?.optString("nombre").orEmpty()
                    )

                val presentations = json.optJSONArray("presentaciones")
                if (presentations != null) {
                    for (index in 0 until presentations.length()) {
                        val presentation = presentations.optJSONObject(index) ?: continue
                        if (presentation.optString("cn") == cn) {
                            result.put("presentacion", presentation.optString("nombre"))
                            break
                        }
                    }
                }
            }
            result
        } catch (e: Exception) {
            JSONObject()
                .put("status", "error")
                .put("error", e.message ?: e.javaClass.simpleName)
                .put("ms", System.currentTimeMillis() - start)
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Devuelve las fotos reales del almacenamiento externo o, si no hay,
     * copia los códigos generados que vienen en los assets del test.
     */
    private fun prepareImages(directory: File): List<File> {
        val existing = imageFiles(directory)
        if (existing.isNotEmpty()) return existing

        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        assets.list("barcodes").orEmpty().forEach { name ->
            assets.open("barcodes/$name").use { input ->
                File(directory, name).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return imageFiles(directory)
    }

    private fun imageFiles(directory: File): List<File> =
        directory.listFiles { file ->
            file.isFile && file.extension.lowercase() in setOf("png", "jpg", "jpeg")
        }?.sortedBy { it.name }.orEmpty()

    private fun resultsDirectory(): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.getExternalFilesDir("barcodes")
            ?: error("El dispositivo no tiene almacenamiento externo")
    }

    private fun formatName(format: Int): String = when (format) {
        Barcode.FORMAT_EAN_13 -> "EAN_13"
        Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
        else -> "FORMAT_$format"
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

    private fun runOcr(bitmap: Bitmap): String? {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            Tasks.await(
                recognizer.process(InputImage.fromBitmap(bitmap, 0)),
                TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            ).text
        } catch (e: Exception) {
            null
        } finally {
            recognizer.close()
        }
    }

    private fun decodeDownscaled(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_DIMENSION) {
            sample *= 2
        }

        val decoded = BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: return null

        val longest = maxOf(decoded.width, decoded.height)
        if (longest <= MAX_DIMENSION) return decoded

        val scale = MAX_DIMENSION.toFloat() / longest
        val scaled = Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true
        )
        if (scaled !== decoded) decoded.recycle()
        return scaled
    }
}
