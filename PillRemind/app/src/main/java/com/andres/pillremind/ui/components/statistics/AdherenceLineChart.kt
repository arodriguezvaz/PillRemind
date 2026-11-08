package com.andres.pillremind.ui.components.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.AdherencePoint
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PlotHeight = 150.dp
private val YAxisWidth = 52.dp

/**
 * Gráfico de líneas con la adherencia de un periodo.
 *
 * Cada punto representa un día o una semana y la altura indica el porcentaje
 * de adherencia. Los huecos sin tomas evaluadas no se dibujan.
 *
 * @param points Puntos de adherencia de más antiguo a más reciente.
 * @param title Título ya resuelto con el periodo que se muestra.
 * @param subtitle Texto ya resuelto que explica cómo se agrupan los puntos.
 * @param modifier Modificador opcional para la card.
 */
@Composable
fun AdherenceLineChart(
    points: List<AdherencePoint>,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val percentages = points.mapNotNull { it.adherencePercent }
    val description = if (percentages.isEmpty()) {
        stringResource(R.string.statistics_chart_no_data)
    } else {
        stringResource(
            R.string.statistics_adherence_chart_description,
            percentages.min(),
            percentages.max()
        )
    }
    val datePattern = stringResource(R.string.common_day_month_pattern)
    val dateFormatter = remember(datePattern) {
        DateTimeFormatter.ofPattern(datePattern, Locale.forLanguageTag("es"))
    }
    val labelIndices = when {
        points.isEmpty() -> emptyList()
        points.size <= 2 -> points.indices.toList()
        else -> listOf(0, points.lastIndex / 2, points.lastIndex)
    }.distinct()
    val labels = labelIndices.map { index ->
        points[index].date.format(dateFormatter)
    }

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
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(AppSpacing.xxs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .width(YAxisWidth)
                        .height(PlotHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    AxisLabel(stringResource(R.string.statistics_percent_value, 100))
                    AxisLabel(stringResource(R.string.statistics_percent_value, 50))
                    AxisLabel(stringResource(R.string.statistics_percent_value, 0))
                }
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Column(modifier = Modifier.weight(1f)) {
                    AdherencePlot(
                        points = points,
                        description = description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PlotHeight)
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        labels.forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Línea, puntos y guías del gráfico.
 */
@Composable
private fun AdherencePlot(
    points: List<AdherencePoint>,
    description: String,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val areaColor = lineColor.copy(alpha = 0.12f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dotRadius = if (points.size <= 15) 2.5.dp else 1.75.dp

    Canvas(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = description
        }
    ) {
        val inset = 4.dp.toPx()
        val top = inset
        val bottom = size.height - inset
        val plotHeight = bottom - top
        val gridStroke = 1.dp.toPx()
        val middle = top + plotHeight / 2f

        drawLine(gridColor, Offset(0f, top), Offset(size.width, top), gridStroke)
        drawLine(gridColor, Offset(0f, middle), Offset(size.width, middle), gridStroke)
        drawLine(gridColor, Offset(0f, bottom), Offset(size.width, bottom), gridStroke)

        val stepX = if (points.size > 1) size.width / (points.size - 1) else 0f
        val segments = mutableListOf<MutableList<Offset>>()
        var currentSegment = mutableListOf<Offset>()

        points.forEachIndexed { index, point ->
            val percent = point.adherencePercent
            if (percent == null) {
                if (currentSegment.isNotEmpty()) {
                    segments += currentSegment
                    currentSegment = mutableListOf()
                }
                return@forEachIndexed
            }

            val x = if (points.size == 1) size.width / 2f else stepX * index
            currentSegment += Offset(x, bottom - plotHeight * (percent / 100f))
        }
        if (currentSegment.isNotEmpty()) {
            segments += currentSegment
        }

        segments.forEach { segment ->
            if (segment.size > 1) {
                val linePath = buildSmoothPath(segment)
                val areaPath = Path().apply {
                    addPath(linePath)
                    lineTo(segment.last().x, bottom)
                    lineTo(segment.first().x, bottom)
                    close()
                }

                drawPath(areaPath, areaColor)
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            segment.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = dotRadius.toPx(),
                    center = point
                )
            }
        }
    }
}

/**
 * Construye una curva suave que pasa por todos los puntos sin salirse del
 * rango vertical que marcan.
 */
private fun buildSmoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points.first().x, points.first().y)
    for (index in 1 until points.size) {
        val previous = points[index - 1]
        val current = points[index]
        val middleX = (previous.x + current.x) / 2f
        path.cubicTo(
            middleX, previous.y,
            middleX, current.y,
            current.x, current.y
        )
    }
    return path
}

/**
 * Etiqueta de un valor del eje vertical.
 */
@Composable
private fun AxisLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End,
        maxLines = 1
    )
}

@Preview(showBackground = true, name = "Adherencia de 30 días")
@Composable
private fun AdherenceLineChartPreview() {
    PillRemindTheme {
        AdherenceLineChart(
            points = previewPoints(count = 5),
            title = stringResource(R.string.statistics_adherence_chart_title, 30),
            subtitle = stringResource(R.string.statistics_adherence_chart_subtitle)
        )
    }
}

@Preview(showBackground = true, fontScale = 1.5f, name = "Adherencia de 90 días")
@Composable
private fun AdherenceLineChartWeeklyPreview() {
    PillRemindTheme(darkTheme = true) {
        AdherenceLineChart(
            points = previewPoints(count = 13),
            title = stringResource(R.string.statistics_adherence_chart_title, 90),
            subtitle = stringResource(R.string.statistics_adherence_chart_subtitle)
        )
    }
}

private fun previewPoints(count: Int): List<AdherencePoint> {
    val today = LocalDate.of(2026, 11, 5)
    return (0 until count).map { index ->
        AdherencePoint(
            date = today.minusDays((count - 1 - index).toLong()),
            adherencePercent = when {
                index == 4 -> null
                index % 7 == 0 -> 100
                index % 5 == 0 -> 45
                else -> 75 + (index % 3) * 10
            }
        )
    }
}
