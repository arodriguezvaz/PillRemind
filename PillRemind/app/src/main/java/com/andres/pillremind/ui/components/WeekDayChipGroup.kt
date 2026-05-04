package com.andres.pillremind.ui.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import com.andres.pillremind.domain.model.WeekDay
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun WeekDayChipGroup(
    selectedDays: Set<WeekDay>,
    onSelectionChange: (Set<WeekDay>) -> Unit,
    enabled: Boolean = true
) {
    val allDays = WeekDay.entries

    LazyRow(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        items(allDays) { day ->
            val isSelected = selectedDays.contains(day)

            FilterChip(
                modifier = Modifier.padding(end = 8.dp),
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
                    Text(text = stringResource(id = day.shortName))
                }
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
