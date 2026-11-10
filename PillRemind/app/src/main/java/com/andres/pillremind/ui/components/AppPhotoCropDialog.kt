package com.andres.pillremind.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Carpeta de caché donde viven las fotos temporales de cámara y recorte. */
internal const val PHOTO_TEMP_DIR = "photos"

private const val MAX_DECODE_SIZE = 2048
private const val MAX_OUTPUT_SIZE = 1024
private const val JPEG_QUALITY = 90

/**
 * Diálogo a pantalla completa para recortar una foto en formato cuadrado.
 *
 * La imagen se carga con Coil (respetando la orientación EXIF) y se muestra
 * dentro de un cuadrado fijo. El usuario puede arrastrarla, ampliarla con dos
 * dedos, girarla de 90° en 90° o volver al encuadre inicial. Al confirmar, el
 * resultado se guarda como JPEG cuadrado en la caché y se entrega mediante
 * [onCropped].
 *
 * @param sourceUri Imagen original, de cámara o galería.
 * @param onDismiss Se llama al cancelar o cerrar el diálogo.
 * @param onCropped Se llama con el archivo JPEG ya recortado.
 */
@Composable
fun AppPhotoCropDialog(
    sourceUri: Uri,
    onDismiss: () -> Unit,
    onCropped: (Uri) -> Unit,
) {
    val context = LocalContext.current
    var bitmap by remember(sourceUri) { mutableStateOf<Bitmap?>(null) }
    var loadFailed by remember(sourceUri) { mutableStateOf(false) }

    LaunchedEffect(sourceUri) {
        loadFailed = false
        val loaded = runCatching {
            val request = ImageRequest.Builder(context)
                .data(sourceUri)
                .size(MAX_DECODE_SIZE)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            (result as? SuccessResult)?.drawable?.toBitmap()
        }.getOrNull()
        bitmap = loaded
        loadFailed = loaded == null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            val currentBitmap = bitmap
            when {
                currentBitmap != null -> PhotoCropContent(
                    bitmap = currentBitmap,
                    onCancel = onDismiss,
                    onCropped = onCropped,
                )

                loadFailed -> PhotoCropError(onDismiss = onDismiss)
                else -> PhotoCropLoading()
            }
        }
    }
}

/**
 * Contenido del recorte una vez decodificada la imagen.
 */
@Composable
private fun PhotoCropContent(
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onCropped: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var quarterTurns by rememberSaveable { mutableIntStateOf(0) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var isSaving by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }
    var cropAreaSize by remember { mutableStateOf(IntSize.Zero) }

    val viewportPx = min(cropAreaSize.width, cropAreaSize.height).toFloat()

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)
    val borderColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCancel, enabled = !isSaving) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.common_close),
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = stringResource(R.string.photo_crop_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .onSizeChanged { cropAreaSize = it },
            contentAlignment = Alignment.Center,
        ) {
            // Al girar o cambiar de tamaño, el desplazamiento anterior puede
            // dejar huecos; se vuelve a limitar al nuevo encuadre.
            LaunchedEffect(bitmap, viewportPx, zoom, quarterTurns) {
                if (viewportPx > 0f) {
                    val clamped = PhotoCropMath.clampOffset(
                        offset = CropOffset(offsetX, offsetY),
                        zoom = zoom,
                        imageWidth = bitmap.width,
                        imageHeight = bitmap.height,
                        quarterTurns = quarterTurns,
                        viewportSize = viewportPx,
                    )
                    offsetX = clamped.x
                    offsetY = clamped.y
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(bitmap, viewportPx) {
                        if (viewportPx <= 0f) return@pointerInput
                        detectTransformGestures { centroid, pan, gestureZoom, _ ->
                            val previousZoom = zoom
                            val newZoom = (previousZoom * gestureZoom)
                                .coerceIn(1f, PhotoCropMath.MAX_ZOOM)
                            val zoomRatio = newZoom / previousZoom
                            // Mantiene fijo el punto bajo los dedos al hacer zoom.
                            val focusX = centroid.x - size.width / 2f - offsetX
                            val focusY = centroid.y - size.height / 2f - offsetY
                            val newOffset = CropOffset(
                                x = offsetX + (1f - zoomRatio) * focusX + pan.x,
                                y = offsetY + (1f - zoomRatio) * focusY + pan.y,
                            )
                            zoom = newZoom
                            val clamped = PhotoCropMath.clampOffset(
                                offset = newOffset,
                                zoom = newZoom,
                                imageWidth = bitmap.width,
                                imageHeight = bitmap.height,
                                quarterTurns = quarterTurns,
                                viewportSize = viewportPx,
                            )
                            offsetX = clamped.x
                            offsetY = clamped.y
                        }
                    },
            ) {
                if (viewportPx <= 0f) return@Canvas
                val squareLeft = (size.width - viewportPx) / 2f
                val squareTop = (size.height - viewportPx) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                val totalScale = PhotoCropMath.baseScale(
                    imageWidth = bitmap.width,
                    imageHeight = bitmap.height,
                    viewportSize = viewportPx,
                ) * zoom

                withTransform({
                    translate(center.x + offsetX, center.y + offsetY)
                    rotate(quarterTurns * 90f, pivot = Offset.Zero)
                    scale(totalScale, totalScale, pivot = Offset.Zero)
                    translate(-bitmap.width / 2f, -bitmap.height / 2f)
                }) {
                    drawImage(imageBitmap)
                }

                // Atenúa la parte de la imagen que queda fuera del cuadrado.
                drawRect(
                    color = scrimColor,
                    topLeft = Offset.Zero,
                    size = Size(size.width, squareTop),
                )
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(0f, squareTop + viewportPx),
                    size = Size(size.width, size.height - squareTop - viewportPx),
                )
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(0f, squareTop),
                    size = Size(squareLeft, viewportPx),
                )
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(squareLeft + viewportPx, squareTop),
                    size = Size(size.width - squareLeft - viewportPx, viewportPx),
                )
                drawRect(
                    color = borderColor,
                    topLeft = Offset(squareLeft, squareTop),
                    size = Size(viewportPx, viewportPx),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }

        Text(
            text = stringResource(R.string.photo_crop_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    quarterTurns = PhotoCropMath.normalizeQuarterTurns(quarterTurns + 1)
                    offsetX = 0f
                    offsetY = 0f
                },
                enabled = !isSaving,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_rotate_90_degrees_cw),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(AppSpacing.xxs))
                Text(text = stringResource(R.string.photo_crop_rotate))
            }
            TextButton(
                onClick = {
                    quarterTurns = 0
                    zoom = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                enabled = !isSaving,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_restart_alt),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(AppSpacing.xxs))
                Text(text = stringResource(R.string.photo_crop_reset))
            }
        }

        if (saveFailed) {
            Text(
                text = stringResource(R.string.photo_crop_save_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppButton(
                text = stringResource(R.string.common_cancel),
                onClick = onCancel,
                enabled = !isSaving,
                variant = ButtonVariant.Tonal,
                size = ButtonSize.Small,
            )
            AppButton(
                text = stringResource(R.string.photo_crop_use),
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        saveFailed = false
                        scope.launch {
                            val crop = PhotoCropMath.cropRect(
                                zoom = zoom,
                                offset = CropOffset(offsetX, offsetY),
                                imageWidth = bitmap.width,
                                imageHeight = bitmap.height,
                                quarterTurns = quarterTurns,
                                viewportSize = viewportPx,
                            )
                            val result = withContext(Dispatchers.IO) {
                                saveCroppedPhoto(context, bitmap, quarterTurns, crop)
                            }
                            isSaving = false
                            if (result != null) {
                                onCropped(result)
                            } else {
                                saveFailed = true
                            }
                        }
                    }
                },
                enabled = !isSaving && viewportPx > 0f,
                isLoading = isSaving,
                size = ButtonSize.Medium,
            )
        }
    }
}

@Composable
private fun PhotoCropLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.size(AppSpacing.md))
        Text(
            text = stringResource(R.string.photo_crop_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun PhotoCropError(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.photo_crop_load_error),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(AppSpacing.md))
        AppButton(
            text = stringResource(R.string.common_close),
            onClick = onDismiss,
            variant = ButtonVariant.Tonal,
            size = ButtonSize.Medium,
        )
    }
}

/**
 * Aplica la rotación y el recorte y guarda el resultado como JPEG en caché.
 * Devuelve `null` si algo falla.
 */
private fun saveCroppedPhoto(
    context: Context,
    source: Bitmap,
    quarterTurns: Int,
    crop: CropRect,
): Uri? = runCatching {
    val turns = PhotoCropMath.normalizeQuarterTurns(quarterTurns)
    val rotated = if (turns == 0) {
        source
    } else {
        val matrix = Matrix().apply { postRotate(turns * 90f) }
        Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    val square = crop.size.roundToInt()
        .coerceIn(1, min(rotated.width, rotated.height))
    val left = crop.left.roundToInt().coerceIn(0, rotated.width - square)
    val top = crop.top.roundToInt().coerceIn(0, rotated.height - square)
    val cropped = Bitmap.createBitmap(rotated, left, top, square, square)
    val output = if (square > MAX_OUTPUT_SIZE) {
        cropped.scale(MAX_OUTPUT_SIZE, MAX_OUTPUT_SIZE, filter = true)
    } else {
        cropped
    }

    val directory = File(context.cacheDir, PHOTO_TEMP_DIR).apply { mkdirs() }
    val file = File(directory, "crop_${System.currentTimeMillis()}.jpg")
    val saved = FileOutputStream(file).use { stream ->
        output.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
    }
    if (saved) {
        Uri.fromFile(file)
    } else {
        file.delete()
        null
    }
}.getOrNull()

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PhotoCropContentPreview() {
    PillRemindTheme {
        PhotoCropContent(
            bitmap = remember {
                createBitmap(800, 600).apply {
                    eraseColor(android.graphics.Color.LTGRAY)
                }
            },
            onCancel = {},
            onCropped = {},
        )
    }
}
