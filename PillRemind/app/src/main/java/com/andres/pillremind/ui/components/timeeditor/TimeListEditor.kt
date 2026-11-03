package com.andres.pillremind.ui.components.timeeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.andres.pillremind.R
import java.time.LocalTime
import java.util.Calendar

/**
 * Editor de lista de horas; muestra `TimeItem`s y permite añadir, editar o
 * eliminar horas mediante un selector de tiempo.
 *
 * @param times Lista de `LocalTime` a mostrar.
 * @param onAddClick Lambda llamada con la hora seleccionada para añadir.
 * @param onRemoveTime Lambda llamada para eliminar una hora.
 * @param onUpdateTime Lambda llamada con la posición y la nueva hora al editar.
 * @param enabled Indica si el editor permite añadir, editar o eliminar horas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeListEditor(
    times: List<LocalTime>,
    onAddClick: (LocalTime) -> Unit,
    onRemoveTime: (LocalTime) -> Unit,
    onUpdateTime: (index: Int, time: LocalTime) -> Unit,
    enabled: Boolean = true
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val currentTime = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        times.forEachIndexed { index, time ->
            TimeItem(
                time = time,
                enabled = enabled,
                onClick = { editingIndex = index },
                onRemove = { onRemoveTime(time) }
            )
        }

        if (enabled) {
            AddTimeItem(
                onClick = { showAddDialog = true }
            )
        }
    }

    if (showAddDialog) {
        TimePickerDialogContent(
            initialTime = LocalTime.of(
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE)
            ),
            onDismiss = { showAddDialog = false },
            onConfirm = { time ->
                onAddClick(time)
                showAddDialog = false
            }
        )
    }

    val editIndex = editingIndex
    val editTime = editIndex?.let { times.getOrNull(it) }
    if (editIndex != null && editTime != null) {
        TimePickerDialogContent(
            initialTime = editTime,
            onDismiss = { editingIndex = null },
            onConfirm = { time ->
                onUpdateTime(editIndex, time)
                editingIndex = null
            }
        )
    }
}

/**
 * Selector de hora reutilizable para añadir o editar una entrada de la lista.
 *
 * @param initialTime Hora con la que se abre el selector.
 * @param onDismiss Acción ejecutada al cancelar.
 * @param onConfirm Acción ejecutada con la hora elegida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogContent(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        LocalTime.of(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                    )
                }
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
        title = { Text(stringResource(R.string.seleccion_la_hora)) }
    ) {
        TimePicker(state = timePickerState)
    }
}
