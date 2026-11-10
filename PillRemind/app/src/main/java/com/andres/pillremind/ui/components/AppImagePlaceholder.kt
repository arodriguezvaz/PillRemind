package com.andres.pillremind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppImageShape
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Placeholder de una imagen que todavía no existe.
 *
 * Lo comparten las fichas de solo lectura (a través de [AppPhotoPicker]) y las
 * cards de perfil y medicamento, para que el hueco de la imagen se vea igual en
 * toda la aplicación.
 *
 * @param icon Icono que representa el contenido (perfil o medicamento).
 * @param contentDescription Descripción accesible del placeholder.
 * @param modifier Modificador para personalizar el layout.
 * @param size Tamaño del contenedor; el icono ocupa el 40 % de ese tamaño.
 */
@Composable
fun AppImagePlaceholder(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(AppImageShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.4f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppImagePlaceholderPreview() {
    PillRemindTheme {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            AppImagePlaceholder(
                icon = ImageVector.vectorResource(R.drawable.ic_person),
                contentDescription = null
            )
            AppImagePlaceholder(
                icon = ImageVector.vectorResource(R.drawable.ic_medication),
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppImagePlaceholderDarkPreview() {
    PillRemindTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            AppImagePlaceholder(
                icon = ImageVector.vectorResource(R.drawable.ic_person),
                contentDescription = null
            )
            AppImagePlaceholder(
                icon = ImageVector.vectorResource(R.drawable.ic_medication),
                contentDescription = null
            )
        }
    }
}
