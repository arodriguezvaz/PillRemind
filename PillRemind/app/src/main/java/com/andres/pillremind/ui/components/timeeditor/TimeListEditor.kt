package com.andres.pillremind.ui.components.timeeditor

import AddTimeItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.util.Calendar


//TODO https://developer.android.com/develop/ui/compose/components/time-pickers-dialogs?hl=es-419
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeListEditor(
    times: List<LocalTime>,
    onAddClick: (LocalTime) -> Unit,
    onRemoveTime: (LocalTime) -> Unit,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentTime = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        times.forEach { time ->
            TimeItem(
                time = time,
                enabled = enabled,
                onRemove = { onRemoveTime(time) }
            )
        }

        if (enabled) {
            AddTimeItem(
                onClick = { showDialog = true }
            )
        }
    }

    if (showDialog) {

        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentTime.get(Calendar.MINUTE),
            is24Hour = true,
        )

        TimePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val time = LocalTime.of(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onAddClick(time)
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Seleccionar la hora") }//TODO missing stringResources
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
