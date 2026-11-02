package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.domain.model.WeekDay

/**
 * Grupo de chips para seleccionar días de la semana.
 *
 * Usa [FlowRow] para que los siete días se envuelvan en varias líneas cuando no
 * caben, en lugar de dejar el último chip cortado en un scroll horizontal.
 *
 * @param selectedDays Conjunto de días actualmente seleccionados.
 * @param onSelectionChange Lambda que recibe el nuevo conjunto de días al cambiar la selección.
 * @param enabled Indica si los chips están habilitados para interacción.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeekDayChipGroup(
    selectedDays: Set<WeekDay>,
    onSelectionChange: (Set<WeekDay>) -> Unit,
    enabled: Boolean = true
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WeekDay.entries.forEach { day ->
            val isSelected = selectedDays.contains(day)

            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = selectedDays.toMutableSet()

                    if (isSelected) {
                        newSelection.remove(day)
                    } else {
                        newSelection.add(day)
                    }

                    onSelectionChange(newSelection)
                },
                label = {
                    Text(text = stringResource(id = day.shortNameRes()))
                },
                enabled = enabled,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    disabledSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeekDayChipGroupPreview() {
    var selectedDays by remember {
        mutableStateOf(setOf<WeekDay>(WeekDay.MONDAY, WeekDay.TUESDAY))
    }

    WeekDayChipGroup(
        selectedDays = selectedDays,
        onSelectionChange = { selectedDays = it }
    )
}
