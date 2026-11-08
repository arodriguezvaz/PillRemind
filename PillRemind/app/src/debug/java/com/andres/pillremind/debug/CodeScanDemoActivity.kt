package com.andres.pillremind.debug

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andres.pillremind.domain.code.MedicineCodeParser
import com.andres.pillremind.ui.theme.PillRemindTheme
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Pantalla de demostración de la fase 0: código del envase → CN → CIMA.
 *
 * Solo existe en la variante debug. Permite probar los códigos generados que
 * vienen en `assets/barcodes` o elegir una foto de la galería del emulador.
 *
 * Los PNG de `app/src/debug/assets/barcodes` son una copia de los de
 * `app/src/androidTest/assets/barcodes`; mantén ambos sincronizados.
 */
class CodeScanDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PillRemindTheme {
                CodeScanDemoScreen()
            }
        }
    }
}

private val testAssets = listOf(
    "ean_oss_720932.png" to "EAN OSS 8470007209324",
    "ean_rotated.png" to "EAN rotado 12°",
    "datamatrix_712.png" to "DataMatrix con (712)",
    "datamatrix_ntin.png" to "DataMatrix NTIN",
    "ean_non_oss.png" to "EAN no OSS (sin CN)",
    "ean_invalid_check.png" to "EAN con check inválido",
    "ean_cn_000000.png" to "CN 000000 (no existe en CIMA)",
    "tritace_fmd_datamatrix_3.jpg" to "Tritace (DataMatrix FMD, sin CN)",
    "tritab_ean_no_es.jpg" to "Tritab (EAN no español, foto real)",
)

@Composable
private fun CodeScanDemoScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<ScanUiState>(ScanUiState.Idle) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            state = ScanUiState.Loading("Foto de la galería")
            scope.launch {
                state = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use {
                        scan(it, "Foto de la galería")
                    } ?: ScanUiState.Error("Foto de la galería", "No se pudo abrir la imagen")
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Demo: código → CN → CIMA", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Solo debug. Toca un código de prueba o elige una foto de la galería.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Códigos de prueba", style = MaterialTheme.typography.titleMedium)
            testAssets.forEach { (file, label) ->
                OutlinedButton(
                    onClick = {
                        state = ScanUiState.Loading(label)
                        scope.launch {
                            state = withContext(Dispatchers.IO) {
                                context.assets.open("barcodes/$file").use { scan(it, label) }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is ScanUiState.Loading
                ) {
                    Text(label)
                }
            }

            Button(
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is ScanUiState.Loading
            ) {
                Text("Elegir foto de la galería")
            }

            when (val current = state) {
                ScanUiState.Idle -> Unit
                is ScanUiState.Loading -> LoadingRow(current.source)
                is ScanUiState.Error -> Text(
                    text = "Error: ${current.message}",
                    color = MaterialTheme.colorScheme.error
                )
                is ScanUiState.Success -> ScanResult(current)
            }

            Text(
                "Imágenes de prueba: códigos generados, una foto de Zylla / " +
                    "Wikimedia Commons (CC BY-SA 4.0) y una foto del usuario.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingRow(source: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Text("Leyendo $source…")
    }
}

@Composable
private fun ScanResult(result: ScanUiState.Success) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(result.source, style = MaterialTheme.typography.titleMedium)
            Text("${result.ms} ms", style = MaterialTheme.typography.bodySmall)

            Image(
                bitmap = result.image,
                contentDescription = "Código escaneado",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Fit
            )

            if (result.barcodes.isEmpty()) {
                Text("No se ha detectado ningún EAN-13 ni DataMatrix.")
            }

            result.barcodes.forEach { barcode ->
                HorizontalDivider()
                Text(barcode.format, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = barcode.raw.replace('\u001d', '|'),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "CN: ${barcode.cn ?: "no contiene CN"}",
                    fontWeight = FontWeight.Bold
                )
                when {
                    barcode.cn == null -> Text(
                        "El código no sigue el formato 847000 ni lleva (712)/NTIN.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    barcode.cima == null -> Unit

                    barcode.cima.status == "encontrado" -> CimaResultCard(barcode.cima)

                    barcode.cima.status == "no_encontrado" -> Text(
                        "CN no encontrado en CIMA (HTTP ${barcode.cima.http}).",
                        style = MaterialTheme.typography.bodySmall
                    )

                    else -> Text(
                        text = "Error consultando CIMA: ${barcode.cima.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!result.ocrText.isNullOrBlank()) {
                HorizontalDivider()
                Text("Texto del envase (OCR)", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = result.ocrText.take(800),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "El código no da CN; el OCR se usaría para rellenar nombre y dosis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CimaResultCard(cima: CimaUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = cima.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (cima.dosis.isNotBlank()) Text("Dosis: ${cima.dosis}")
            if (cima.forma.isNotBlank()) Text("Forma: ${cima.forma}")
            if (cima.presentacion.isNotBlank()) Text("Presentación: ${cima.presentacion}")
            Text(
                text = "HTTP ${cima.http} · ${cima.ms} ms",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Fuente: AEMPS - CIMA. Consultado el ${cima.retrievedAt}.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun scan(input: InputStream, source: String): ScanUiState {
    val bitmap = decode(input)
        ?: return ScanUiState.Error(source, "No se pudo decodificar la imagen")

    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_DATA_MATRIX)
        .build()
    val scanner = BarcodeScanning.getClient(options)
    val start = System.currentTimeMillis()

    return try {
        val barcodes = Tasks.await(
            scanner.process(InputImage.fromBitmap(bitmap, 0)),
            20,
            TimeUnit.SECONDS
        )
        val results = barcodes.map { barcode ->
            val raw = barcode.rawValue.orEmpty()
            val cn = when (barcode.format) {
                Barcode.FORMAT_EAN_13 -> MedicineCodeParser.fromEan13(raw)
                Barcode.FORMAT_DATA_MATRIX -> MedicineCodeParser.fromGs1(raw)
                else -> null
            }
            BarcodeUi(
                raw = raw,
                format = formatName(barcode.format),
                cn = cn,
                cima = cn?.let(::lookupCima)
            )
        }
        val needsOcr = results.none { it.cn != null }
        ScanUiState.Success(
            source = source,
            ms = System.currentTimeMillis() - start,
            barcodes = results,
            image = bitmap.asImageBitmap(),
            ocrText = if (needsOcr) runOcr(bitmap) else null
        )
    } catch (e: Exception) {
        ScanUiState.Error(source, e.message ?: e.javaClass.simpleName)
    } finally {
        scanner.close()
    }
}

private fun runOcr(bitmap: Bitmap): String? {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    return try {
        Tasks.await(
            recognizer.process(InputImage.fromBitmap(bitmap, 0)),
            20,
            TimeUnit.SECONDS
        ).text
    } catch (e: Exception) {
        null
    } finally {
        recognizer.close()
    }
}

private fun decode(input: InputStream): Bitmap? {
    val bitmap = BitmapFactory.decodeStream(input) ?: return null
    val longest = maxOf(bitmap.width, bitmap.height)
    if (longest <= 2048) return bitmap

    val scale = 2048f / longest
    val scaled = Bitmap.createScaledBitmap(
        bitmap,
        (bitmap.width * scale).toInt().coerceAtLeast(1),
        (bitmap.height * scale).toInt().coerceAtLeast(1),
        true
    )
    if (scaled !== bitmap) bitmap.recycle()
    return scaled
}

private fun lookupCima(cn: String): CimaUi {
    val start = System.currentTimeMillis()
    val retrievedAt = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val connection = (
        URL("https://cima.aemps.es/cima/rest/medicamento?cn=$cn").openConnection()
            as HttpURLConnection
        ).apply {
        connectTimeout = 7_000
        readTimeout = 7_000
        requestMethod = "GET"
        instanceFollowRedirects = true
        setRequestProperty("Accept", "application/json")
        setRequestProperty("User-Agent", "PillRemind-Demo")
    }

    return try {
        val http = connection.responseCode
        val body = if (http == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            ""
        }

        when {
            http == HttpURLConnection.HTTP_NO_CONTENT ||
                http == HttpURLConnection.HTTP_NOT_FOUND ||
                (http == HttpURLConnection.HTTP_OK && body.isBlank()) -> CimaUi(
                status = "no_encontrado",
                http = http,
                ms = System.currentTimeMillis() - start,
                retrievedAt = retrievedAt
            )

            http == HttpURLConnection.HTTP_OK -> {
                val json = JSONObject(body)
                var presentacion = ""
                val presentations = json.optJSONArray("presentaciones")
                if (presentations != null) {
                    for (index in 0 until presentations.length()) {
                        val item = presentations.optJSONObject(index) ?: continue
                        if (item.optString("cn") == cn) {
                            presentacion = item.optString("nombre")
                            break
                        }
                    }
                }
                CimaUi(
                    status = "encontrado",
                    http = http,
                    ms = System.currentTimeMillis() - start,
                    nombre = json.optString("nombre"),
                    dosis = json.optString("dosis"),
                    forma = json.optJSONObject("formaFarmaceutica")
                        ?.optString("nombre")
                        .orEmpty(),
                    presentacion = presentacion,
                    retrievedAt = retrievedAt
                )
            }

            else -> CimaUi(
                status = "error",
                http = http,
                ms = System.currentTimeMillis() - start,
                error = "HTTP $http",
                retrievedAt = retrievedAt
            )
        }
    } catch (e: Exception) {
        CimaUi(
            status = "error",
            http = 0,
            ms = System.currentTimeMillis() - start,
            error = e.message ?: e.javaClass.simpleName,
            retrievedAt = retrievedAt
        )
    } finally {
        connection.disconnect()
    }
}

private fun formatName(format: Int): String = when (format) {
    Barcode.FORMAT_EAN_13 -> "EAN_13"
    Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
    else -> "FORMAT_$format"
}

private sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class Loading(val source: String) : ScanUiState
    data class Success(
        val source: String,
        val ms: Long,
        val barcodes: List<BarcodeUi>,
        val image: ImageBitmap,
        val ocrText: String?
    ) : ScanUiState

    data class Error(val source: String, val message: String) : ScanUiState
}

private data class BarcodeUi(
    val raw: String,
    val format: String,
    val cn: String?,
    val cima: CimaUi?
)

private data class CimaUi(
    val status: String,
    val http: Int,
    val ms: Long,
    val nombre: String = "",
    val dosis: String = "",
    val forma: String = "",
    val presentacion: String = "",
    val error: String = "",
    val retrievedAt: String
)
