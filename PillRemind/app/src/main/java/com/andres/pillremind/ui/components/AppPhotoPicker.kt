package com.andres.pillremind.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppImageShape
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.io.File

/**
 * Componente para seleccionar, recortar o ampliar la foto de un perfil o de un
 * medicamento.
 *
 * En modo edición permite hacer una foto con la cámara o elegir una de la
 * galería; después abre siempre el recorte cuadrado de [AppPhotoCropDialog]. En
 * modo lectura, si hay foto, al pulsarla se abre [AppPhotoViewerDialog] a
 * pantalla completa.
 *
 * @param photoUri URI de la imagen seleccionada (si existe).
 * @param onPhotoSelected Callback que se ejecuta al terminar el recorte.
 * @param modifier Modificador para personalizar el layout.
 * @param enabled Indica si el selector está habilitado para editar la foto.
 * @param size Tamaño del componente.
 * @param placeholderIcon Icono mostrado cuando no hay imagen seleccionada.
 * @param photoDescriptionRes Recurso que describe la imagen según su contexto.
 */
@Composable
fun AppPhotoPicker(
    photoUri: Uri?,
    onPhotoSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 96.dp,
    placeholderIcon: ImageVector = Icons.Default.AddAPhoto,
    @StringRes photoDescriptionRes: Int = R.string.photo_profile,
) {
    val context = LocalContext.current
    var showSourceDialog by rememberSaveable { mutableStateOf(false) }
    var showViewer by rememberSaveable { mutableStateOf(false) }
    var cropSourceUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCameraPath by rememberSaveable { mutableStateOf<String?>(null) }

    // Los selectores solo se registran cuando se pueden usar: en modo lectura
    // no hacen falta y así el componente se puede previsualizar sin un
    // ActivityResultRegistry.
    val galleryLauncher = if (enabled) {
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                pendingCameraPath?.let { File(it).delete() }
                pendingCameraPath = null
                cropSourceUri = uri
            }
        }
    } else {
        null
    }
    val cameraLauncher = if (enabled) {
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val path = pendingCameraPath
            if (success && path != null) {
                // El temporal se conserva hasta que termine el recorte: si se
                // cancela, clearCameraTemp() lo borra.
                cropSourceUri = Uri.fromFile(File(path))
            } else {
                path?.let { File(it).delete() }
                pendingCameraPath = null
            }
        }
    } else {
        null
    }

    val hasCamera = remember(context, enabled) {
        enabled && context.packageManager
            .queryIntentActivities(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 0)
            .isNotEmpty()
    }

    fun clearCameraTemp() {
        pendingCameraPath?.let { File(it).delete() }
        pendingCameraPath = null
    }

    fun launchCamera() {
        runCatching {
            val file = createCameraTempFile(context)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            pendingCameraPath = file.absolutePath
            cameraLauncher?.launch(uri)
        }.onFailure {
            Log.e("AppPhotoPicker", "No se pudo abrir la cámara", it)
            pendingCameraPath?.let { File(it).delete() }
            pendingCameraPath = null
        }
    }

    // El click debe funcionar también cuando todavía no hay imagen, así que se
    // comparte entre la foto y el placeholder. En modo lectura solo se abre el
    // visor si hay algo que ver.
    val clickModifier = when {
        enabled -> Modifier.clickable(
            onClickLabel = stringResource(
                if (photoUri == null) R.string.photo_add else R.string.photo_change
            ),
            onClick = { showSourceDialog = true },
        )

        photoUri != null -> Modifier.clickable(
            onClickLabel = stringResource(R.string.photo_view),
            onClick = { showViewer = true },
        )

        else -> Modifier
    }

    if (photoUri == null) {
        AppImagePlaceholder(
            icon = placeholderIcon,
            contentDescription = stringResource(
                if (enabled) R.string.photo_add else R.string.photo_none
            ),
            modifier = modifier.then(clickModifier),
            size = size
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(AppImageShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(clickModifier),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = stringResource(photoDescriptionRes),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (enabled) {
                PhotoBadge(
                    icon = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.photo_edit),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp),
                )
            }
        }
    }

    if (showSourceDialog && enabled) {
        PhotoSourceDialog(
            title = stringResource(
                if (photoUri == null) R.string.photo_add else R.string.photo_change
            ),
            hasCamera = hasCamera,
            onDismiss = { showSourceDialog = false },
            onCamera = {
                showSourceDialog = false
                launchCamera()
            },
            onGallery = {
                showSourceDialog = false
                galleryLauncher?.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        )
    }

    val cropSource = cropSourceUri
    if (cropSource != null) {
        AppPhotoCropDialog(
            sourceUri = cropSource,
            onDismiss = {
                cropSourceUri = null
                clearCameraTemp()
            },
            onCropped = { cropped ->
                cropSourceUri = null
                clearCameraTemp()
                onPhotoSelected(cropped)
            },
        )
    }

    if (showViewer && photoUri != null) {
        AppPhotoViewerDialog(
            photoUri = photoUri,
            photoDescriptionRes = photoDescriptionRes,
            onDismiss = { showViewer = false },
        )
    }
}

/**
 * Diálogo con las dos fuentes posibles de una foto.
 */
@Composable
private fun PhotoSourceDialog(
    title: String,
    hasCamera: Boolean,
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                if (hasCamera) {
                    PhotoSourceOption(
                        icon = Icons.Default.PhotoCamera,
                        text = stringResource(R.string.photo_source_camera),
                        onClick = onCamera,
                    )
                }
                PhotoSourceOption(
                    icon = Icons.Default.PhotoLibrary,
                    text = stringResource(R.string.photo_source_gallery),
                    onClick = onGallery,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

/**
 * Fila pulsable de una fuente de foto.
 */
@Composable
private fun PhotoSourceOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = text, onClick = onClick)
            .padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Insignia circular sobre la foto en modo edición (lápiz).
 */
@Composable
private fun PhotoBadge(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(AppSpacing.xxs)
            .size(26.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Crea un archivo temporal único para la foto de cámara.
 */
private fun createCameraTempFile(context: Context): File {
    val directory = File(context.cacheDir, PHOTO_TEMP_DIR).apply { mkdirs() }
    return File(directory, "camera_${System.currentTimeMillis()}.jpg")
}

@Preview(showBackground = true)
@Composable
private fun AppPhotoPickerReadOnlyPreview() {
    PillRemindTheme {
        Box(modifier = Modifier.padding(AppSpacing.md)) {
            AppPhotoPicker(
                photoUri = null,
                onPhotoSelected = {},
                enabled = false,
            )
        }
    }
}
