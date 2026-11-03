package com.andres.pillremind.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppImageShape
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Componente para seleccionar o mostrar una imagen de perfil.
 *
 * Permite al usuario elegir una imagen desde la galería y mostrarla
 * dentro de un contenedor circular o rectangular según el tamaño.
 *
 * @param photoUri URI de la imagen seleccionada (si existe).
 * @param onPhotoSelected Callback que se ejecuta al seleccionar una imagen.
 * @param modifier Modificador para personalizar el layout.
 * @param enabled Indica si el selector está habilitado.
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
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { onPhotoSelected(it) } }

    Box(
        modifier = modifier
            .size(size)
            .clip(AppImageShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (enabled) Modifier.clickable {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = stringResource(photoDescriptionRes),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Badge Edit: solo si está habilitado
            if (enabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .padding(AppSpacing.xxs)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.photo_edit),
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            Icon(
                imageVector = placeholderIcon,
                contentDescription = stringResource(
                    if (enabled) R.string.photo_add else R.string.photo_none
                ),
                modifier = Modifier.size(size * 0.4f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}