package com.andres.pillremind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andres.pillremind.domain.model.Profile

/**
 * Card que representa un perfil de usuario con un botón de gestión.
 *
 * @param profile Datos del perfil a mostrar.
 * @param gestionar Lambda ejecutada al pulsar el botón de gestión.
 * @param modifier Modificador opcional para personalizar el layout.
 * @param buttonText Texto del botón (por defecto "Gestionar").
 * @param buttonVariant Variante visual del botón.
 */
@Composable
fun PerfilCard(
    profile: Profile,
    gestionar: () -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String = "Gestionar",
    buttonVariant: ButtonVariant = ButtonVariant.Primary,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    if (!profile.urlImagen.isNullOrEmpty()) {
                        AsyncImage(
                            model = profile.urlImagen,
                            contentDescription = "Foto de ${profile.name}",
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
                text = buttonText,
                onClick = gestionar,
                variant = buttonVariant,
                size = ButtonSize.Small,
            )
        }
    }
}

@Composable
private fun DefaultAvatar() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = "Profile avatar",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

