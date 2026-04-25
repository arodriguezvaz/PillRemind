package com.andres.pillremind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Profile
import java.time.LocalDate

@Composable
fun MedicationCard(
    medication: Medication,
    tomar: () -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String = "Tomar",
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
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    medication.imageUrl
                }

                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            AppButton(
                text = buttonText,
                onClick = tomar,
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
                shape = RoundedCornerShape(14.dp),
            ),
    )
}

@Preview(showBackground = true)
@Composable
private fun MedicationPreview() {
    val medicationFake = Medication(
        id = 1,
        name = "Dalsy",
        imageUrl = "",
        profileId = 1,
        dose = 1.0,
        unit = "mg",
        startDate = LocalDate.of(2023, 5, 12)
    )

    MedicationCard(
        medication = medicationFake,
        tomar = {}
    )
}