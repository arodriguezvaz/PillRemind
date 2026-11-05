package com.andres.pillremind.ui.components.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.DailyStatistics
import com.andres.pillremind.domain.model.toWeekDay
import com.andres.pillremind.ui.components.labelRes
import com.andres.pillremind.ui.components.shortNameRes
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDate

/**
 * Gráfico de barras apiladas con la adherencia de los últimos siete días.
 *
 * Cada barra representa un día y se divide en tomadas, sin registrar y
 * pendientes. La altura es siempre la misma para poder comparar la proporción
 * de cada estado sin ejes ni librerías externas.
 *
 * @param days Días que se representan, de más antiguo a más reciente.
 * @param modifier Modificador opcional para la card.
 */
@Composable
fun WeeklyStatisticsChart(
    days: List<DailyStatistics>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md)
        ) {
            Text(
                text = stringResource(R.string.statistics_chart_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { day ->
                    DayBar(
                        day = day,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            ChartLegend()
        }
    }
}

/**
 * Barra y etiqueta de un día del gráfico.
 */
@Composable
private fun DayBar(
    day: DailyStatistics,
    modifier: Modifier = Modifier
) {
    val fullDay = stringResource(day.date.dayOfWeek.toWeekDay().labelRes())
    val shortDay = stringResource(day.date.dayOfWeek.toWeekDay().shortNameRes())
    val description = stringResource(
        R.string.statistics_chart_bar_description,
        fullDay,
        day.taken,
        day.missed,
        day.pending
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(96.dp)
                .width(18.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clearAndSetSemantics { contentDescription = description }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (day.pending > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(day.pending.toFloat())
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
                if (day.missed > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(day.missed.toFloat())
                            .background(MaterialTheme.colorScheme.error)
                    )
                }
                if (day.taken > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(day.taken.toFloat())
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = shortDay,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Leyenda de colores del gráfico.
 */
@Composable
private fun ChartLegend() {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        ChartLegendItem(
            label = stringResource(R.string.statistics_taken_label),
            color = MaterialTheme.colorScheme.tertiary
        )
        ChartLegendItem(
            label = stringResource(R.string.statistics_missed_label),
            color = MaterialTheme.colorScheme.error
        )
        ChartLegendItem(
            label = stringResource(R.string.statistics_pending_label),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ChartLegendItem(
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(AppSpacing.xxs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyStatisticsChartPreview() {
    PillRemindTheme {
        WeeklyStatisticsChart(
            days = previewDays()
        )
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
private fun WeeklyStatisticsChartLargeTextPreview() {
    PillRemindTheme(darkTheme = true) {
        WeeklyStatisticsChart(
            days = previewDays()
        )
    }
}

private fun previewDays(): List<DailyStatistics> {
    val today = LocalDate.of(2026, 11, 5)
    return (0 until 7).map { offset ->
        val date = today.minusDays((6 - offset).toLong())
        DailyStatistics(
            date = date,
            taken = if (offset == 6) 2 else 3,
            missed = if (offset == 2) 1 else 0,
            pending = if (offset == 6) 1 else 0
        )
    }
}
