package com.andres.pillremind.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Campo de selección de fecha que muestra la fecha formateada y abre un dialogo
 * con un DatePicker para seleccionar una nueva fecha.
 *
 * @param value Fecha actualmente seleccionada (puede ser null).
 * @param onDateSelected Lambda llamada con la fecha elegida.
 * @param modifier Modificador opcional para personalizar la vista.
 * @param placeholder Texto mostrado cuando no hay fecha seleccionada.
 * @param enabled Indica si el campo está habilitado para interactuar. Cuando es
 *   `false` se muestra deshabilitado y oculta el icono del calendario.
 * @param isError Marca visual de error en el campo.
 * @param minDate Fecha mínima seleccionable, o `null` si no hay límite.
 * @param maxDate Fecha máxima seleccionable, o `null` si no hay límite.
 */
@Composable
fun AppDatePickerField(
    value: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.fecha_hint),
    enabled: Boolean = true,
    isError: Boolean = false,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null
) {

    var showDialog by remember { mutableStateOf(false) }

    val formattedDate = value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""

    // El TextField interno siempre está deshabilitado para evitar que se pueda
    // escribir; los colores y el icono dependen de `enabled`.
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    val trailingIcon: (@Composable () -> Unit)? = if (enabled) {
        {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.date_picker_content_description)
            )
        }
    } else {
        null
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp,
        color = containerColor
    ) {

        TextField(
            value = formattedDate,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled) { showDialog = true },
            enabled = false,
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailingIcon = trailingIcon,
            isError = isError,
            colors = TextFieldDefaults.colors(
                disabledContainerColor = containerColor,
                disabledTextColor = contentColor,
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(12.dp),
        )
    }


    if (showDialog) {
        val selectableDates = remember(minDate, maxDate) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()
                    val afterMin = minDate?.let { !date.isBefore(it) } ?: true
                    val beforeMax = maxDate?.let { !date.isAfter(it) } ?: true
                    return afterMin && beforeMax
                }
            }
        }
        val initialSelectedDateMillis = value
            ?.atStartOfDay(ZoneId.of("UTC"))
            ?.toInstant()
            ?.toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedDateMillis,
            selectableDates = selectableDates
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedDate =
                                Instant.ofEpochMilli(selectedMillis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                            onDateSelected(selectedDate)
                        }
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },

            ) {
            DatePicker(state = datePickerState)
        }
    }
}