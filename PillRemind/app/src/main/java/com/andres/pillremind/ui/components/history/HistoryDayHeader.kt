package com.andres.pillremind.ui.components.history

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Cabecera de un día del historial.
 *
 * @param date Día que se representa.
 * @param today Día actual según el momento de referencia de la pantalla.
 * @param modifier Modificador opcional para la cabecera.
 */
@Composable
fun HistoryDayHeader(
    date: LocalDate,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val text = when (date) {
        today -> stringResource(R.string.history_day_today)
        today.minusDays(1) -> stringResource(R.string.history_day_yesterday)
        else -> {
            val pattern = stringResource(R.string.common_day_month_pattern)
            date.format(DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("es")))
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = AppSpacing.xs)
    )
}

@Preview(showBackground = true)
@Composable
private fun HistoryDayHeaderPreview() {
    PillRemindTheme {
        HistoryDayHeader(
            date = LocalDate.of(2026, 11, 5),
            today = LocalDate.of(2026, 11, 6),
        )
    }
}
