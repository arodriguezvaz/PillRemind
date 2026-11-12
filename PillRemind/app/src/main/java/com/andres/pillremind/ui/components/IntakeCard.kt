package com.andres.pillremind.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.andres.pillremind.domain.model.IntakeVisualState
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppImageShape
import com.andres.pillremind.ui.theme.AppSpacing
import java.time.format.DateTimeFormatter

/**
 * Tarjeta que muestra una toma de medicación con su estado visual.
 *
 * Permite visualizar la información del medicamento y marcar la toma como realizada
 * según su estado actual (pendiente, retrasada o tomada).
 *
 * @param intake Información de la toma junto con el medicamento asociado.
 * @param visualState Estado visual de la toma (pendiente, tomada o retrasada).
 * @param onTake Acción ejecutada al pulsar el botón de estado: marca la toma
 * como realizada o la deshace si ya estaba registrada.
 * @param modifier Modificador para personalizar el layout.
 * @param onCardClick Acción ejecutada al pulsar la card completa para abrir la
 *   información del medicamento; si es `null`, solo responde el botón.
 */
@Composable
fun IntakeCard(
    intake: IntakeWithMedication,
    visualState: IntakeVisualState,
    onTake: () -> Unit,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null,
) {
    val cardClickModifier = if (onCardClick != null) {
        Modifier.clickable(
            onClickLabel = stringResource(
                R.string.medication_open_info,
                intake.medicationName
            ),
            onClick = onCardClick,
        )
    } else {
        Modifier
    }
    val (buttonText, buttonVariant) = when (visualState) {
        IntakeVisualState.TAKEN -> Pair(stringResource(R.string.tomado), ButtonVariant.Secondary)
        IntakeVisualState.DELAYED -> Pair(stringResource(R.string.tomar), ButtonVariant.Danger)
        IntakeVisualState.PENDING -> Pair(stringResource(R.string.tomar), ButtonVariant.Primary)
    }
    val timeFormatter = DateTimeFormatter.ofPattern(stringResource(R.string.common_time_pattern))

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
                .then(cardClickModifier)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(AppImageShape),
                ) {
                    if (!intake.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = intake.imageUrl,
                            contentDescription = stringResource(
                                R.string.medication_photo_description,
                                intake.medicationName
                            ),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        DefaultPill()
                    }
                }

                Column {
                    Text(
                        text = intake.medicationName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        text = "${intake.dose} ${intake.unit} · ${
                            intake.scheduledAt.format(timeFormatter)
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AppButton(
                text = buttonText,
                onClick = onTake,
                variant = buttonVariant,
                size = ButtonSize.Small,
                looksDisabled = visualState == IntakeVisualState.TAKEN,
            )
        }
    }
}

/**
 * Imagen por defecto utilizada cuando un medicamento no tiene foto asociada.
 */
@Composable
fun DefaultPill() {
    AppImagePlaceholder(
        icon = ImageVector.vectorResource(R.drawable.ic_medication),
        contentDescription = stringResource(R.string.medication_icon_description)
    )
}
