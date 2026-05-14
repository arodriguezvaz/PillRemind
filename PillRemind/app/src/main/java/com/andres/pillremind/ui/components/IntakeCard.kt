package com.andres.pillremind.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.data.local.projections.IntakeVisualState
import com.andres.pillremind.data.local.projections.IntakeWithMedication
import java.time.format.DateTimeFormatter

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    if (intake.imageUrl == null) {
                        DefaultAvatar()
                    } else {
                        // tu AsyncImage aquí
                    }
                }

                Column {
                    Text(
                        text = intake.medicationName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
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
 * Muestra un avatar por defecto cuando el medicamento no tiene imagen.
 */
@Composable
private fun DefaultAvatar() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp),
            ),
    ) {
        Icon(
            imageVector = Icons.Rounded.Medication,
            contentDescription = "Default avatar",
        )
    }
}