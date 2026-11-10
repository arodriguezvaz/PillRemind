package com.andres.pillremind.ui.components

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Visor de una foto a pantalla completa.
 *
 * Se abre al pulsar una foto en las fichas de solo lectura. La imagen se ajusta
 * completa, sin recortarla, sobre un fondo oscuro del tema. Se cierra con el
 * botón superior, tocando fuera de la imagen o con el gesto de volver.
 *
 * @param photoUri Foto que se quiere ampliar.
 * @param photoDescriptionRes Descripción accesible de la foto.
 * @param onDismiss Se llama al cerrar el visor.
 */
@Composable
fun AppPhotoViewerDialog(
    photoUri: Uri,
    @StringRes photoDescriptionRes: Int,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        PhotoViewerContent(
            photoUri = photoUri,
            photoDescriptionRes = photoDescriptionRes,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun PhotoViewerContent(
    photoUri: Uri,
    @StringRes photoDescriptionRes: Int,
    onDismiss: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val closeLabel = stringResource(R.string.photo_view_close)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = closeLabel,
                onClick = onDismiss,
            ),
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = stringResource(photoDescriptionRes),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(AppSpacing.md)
                .size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = AppElevation,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = closeLabel,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PhotoViewerContentPreview() {
    PillRemindTheme {
        PhotoViewerContent(
            photoUri = Uri.EMPTY,
            photoDescriptionRes = R.string.photo_profile,
            onDismiss = {},
        )
    }
}
