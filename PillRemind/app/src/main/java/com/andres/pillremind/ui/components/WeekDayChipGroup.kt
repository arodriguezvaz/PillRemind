package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.andres.pillremind.domain.model.WeekDay

/**
 * Grupo de chips para seleccionar días de la semana.
 *
 * Los siete días comparten el ancho disponible en una sola fila. Se usan chips
 * compactos para que quepan todos sin scroll ni cortes.
 *
 * @param selectedDays Conjunto de días actualmente seleccionados.
 * @param onSelectionChange Lambda que recibe el nuevo conjunto de días al cambiar la selección.
 * @param enabled Indica si los chips están habilitados para interacción.
 */
@Composable
fun WeekDayChipGroup(
    selectedDays: Set<WeekDay>,
    onSelectionChange: (Set<WeekDay>) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        WeekDay.entries.forEach { day ->
            val isSelected = selectedDays.contains(day)

            WeekDayChip(
                modifier = Modifier.weight(1f),
                label = stringResource(day.shortNameRes()),
                selected = isSelected,
                enabled = enabled,
                onClick = {
                    val newSelection = selectedDays.toMutableSet()

                    if (isSelected) {
                        newSelection.remove(day)
                    } else {
                        newSelection.add(day)
                    }

                    onSelectionChange(newSelection)
                }
            )
        }
    }
}

@Composable
private fun WeekDayChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(40.dp)
            .alpha(if (enabled) 1f else 0.5f),
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
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
