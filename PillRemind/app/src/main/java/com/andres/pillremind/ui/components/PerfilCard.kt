package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppImageShape
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Card que representa un perfil de usuario con un botón de gestión.
 *
 * @param profile Datos del perfil a mostrar.
 * @param gestionar Lambda ejecutada al pulsar el botón de gestión.
 * @param modifier Modificador opcional para personalizar el layout.
 * @param buttonText Texto del botón; si es `null`, se usa el texto de gestionar.
 * @param buttonVariant Variante visual del botón.
 */
@Composable
fun PerfilCard(
    profile: Profile,
    gestionar: () -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    buttonVariant: ButtonVariant = ButtonVariant.Primary,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(AppImageShape),
                ) {
                    if (!profile.urlImagen.isNullOrEmpty()) {
                        AsyncImage(
                            model = profile.urlImagen,
                            contentDescription = stringResource(
                                R.string.profile_photo_description,
                                profile.name
                            ),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        DefaultAvatar()
                    }
                }

                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1                    )
            }

            AppButton(
                text = buttonText ?: stringResource(R.string.profile_manage),
                onClick = gestionar,
                variant = buttonVariant,
                size = ButtonSize.Small,
            )
        }
    }
}

@Composable
private fun DefaultAvatar() {
    AppImagePlaceholder(
        icon = ImageVector.vectorResource(R.drawable.ic_person),
        contentDescription = stringResource(R.string.profile_avatar_description)
    )
}

