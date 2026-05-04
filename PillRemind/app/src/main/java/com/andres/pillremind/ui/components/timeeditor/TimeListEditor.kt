package com.andres.pillremind.ui.components.timeeditor

import AddTimeItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andres.pillremind.ui.components.AppButton
import java.time.LocalTime

enum class TimeListMode {
    VIEW,
    EDIT
}

@Composable
fun TimeListEditor(
    times: List<LocalTime>,
    mode: TimeListMode,
    onAddClick: () -> Unit,
    onRemoveTime: (LocalTime) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        times.forEach { time ->
            TimeItem(
                time = time,
                mode = mode,
                onRemove = { onRemoveTime(time) }
            )
        }

        if (mode == TimeListMode.EDIT) {
            AddTimeItem(onClick = onAddClick)
        }
    }
}