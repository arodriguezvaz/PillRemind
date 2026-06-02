package com.andres.pillremind.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andres.pillremind.R
import com.andres.pillremind.data.local.projections.IntakeVisualState
import com.andres.pillremind.data.local.projections.IntakeWithMedication
import java.time.format.DateTimeFormatter

/**
 * Tarjeta que muestra una toma de medicación con su estado visual.
 *
 * Permite visualizar la información del medicamento y marcar la toma como realizada
 * según su estado actual (pendiente, retrasada o tomada).
 *
 * @param intake Información de la toma junto con el medicamento asociado.
 * @param visualState Estado visual de la toma (pendiente, tomada o retrasada).
 * @param onTake Acción ejecutada al pulsar el botón de tomar.
 * @param modifier Modificador para personalizar el layout.
 */
@Composable
fun IntakeCard(
    intake: IntakeWithMedication,
    visualState: IntakeVisualState,
    onTake: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (buttonText, buttonVariant) = when (visualState) {
        IntakeVisualState.TAKEN -> Pair(stringResource(R.string.tomado), ButtonVariant.Secondary)
        IntakeVisualState.DELAYED -> Pair(stringResource(R.string.tomar), ButtonVariant.Danger)
        IntakeVisualState.PENDING -> Pair(stringResource(R.string.tomar), ButtonVariant.Primary)
    }

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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    if (!intake.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = intake.imageUrl,
                            contentDescription = "Foto de ${intake.medicationName}",
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
                            intake.scheduledAt.format(
                                DateTimeFormatter.ofPattern("HH:mm")
                            )
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
                enabled = visualState != IntakeVisualState.TAKEN,
            )
        }
    }
}

/**
 * Icono por defecto utilizado cuando un medicamento no tiene imagen asociada.
 */
@Composable
fun DefaultPill() {
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
            painter = painterResource(R.drawable.ic_pill),
            contentDescription = "Pill icon",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}