package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andres.pillremind.domain.model.Medication
import java.time.LocalDate

/**
 * Card que muestra la información principal de un medicamento.
 *
 * @param medication objeto con los datos del medicamento a mostrar.
 * @param accion acción que se ejecuta al pulsar el botón principal.
 * @param modifier modificador opcional para personalizar el layout.
 * @param buttonText texto del botón de acción.
 * @param buttonVariant variante visual del botón.
 */
@Composable
fun MedicationCard(
    medication: Medication,
    accion: () -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String,
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
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    if (!medication.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = medication.imageUrl,
                            contentDescription = "Foto de ${medication.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        DefaultPill()
                    }
                }

                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }

            AppButton(
                text = buttonText,
                onClick = accion,
                variant = buttonVariant,
                size = ButtonSize.Small,
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun MedicationPreview() {
    val medicationFake = Medication(
        id = 1,
        name = "Dalsy",
        profileId = 1,
        dose = 1.0,
        unit = "mg",
        startDate = LocalDate.of(2023, 5, 12)
    )

    MedicationCard(
        medication = medicationFake,
        accion = {},
        buttonText = ""
    )
}