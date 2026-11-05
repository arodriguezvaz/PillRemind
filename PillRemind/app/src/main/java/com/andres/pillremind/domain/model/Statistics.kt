package com.andres.pillremind.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * Periodo disponible para consultar las estadísticas de tomas.
 *
 * @property days Número de días incluidos, contando hoy.
 */
enum class StatisticsPeriod(val days: Long) {
    SEVEN(7),
    THIRTY(30),
    NINETY(90)
}

/**
 * Estado de una toma dentro de las estadísticas.
 */
enum class StatisticsStatus {
    TAKEN,
    MISSED,
    PENDING
}

/**
 * Toma con los datos necesarios para calcular estadísticas e historial.
 *
 * Los campos de nombre, dosis y unidad son los snapshots guardados en la toma
 * para que el historial no cambie al editar el medicamento.
 *
 * @property intakeId Identificador de la toma.
 * @property medicationId Identificador del medicamento.
 * @property scheduledAt Fecha y hora programadas.
 * @property takenAt Fecha y hora en las que se registró la toma; null si no se tomó.
 * @property medicationName Nombre del medicamento en el momento de la toma.
 * @property dose Dosis en el momento de la toma.
 * @property unit Unidad de la dosis en el momento de la toma.
 * @property imageUrl Imagen actual del medicamento, si existe.
 * @property isArchived Indica si el medicamento está archivado actualmente.
 */
data class StatisticsIntake(
    val intakeId: Int,
    val medicationId: Int,
    val scheduledAt: LocalDateTime,
    val takenAt: LocalDateTime?,
    val medicationName: String,
    val dose: Double,
    val unit: String,
    val imageUrl: String?,
    val isArchived: Boolean
)

/**
 * Calcula el estado de la toma en un momento dado.
 *
 * Una toma registrada siempre cuenta como tomada. Si no lo está, se considera
 * sin registrar cuando han pasado más de 15 minutos desde la hora programada;
 * hasta entonces, incluido el límite exacto, sigue pendiente.
 *
 * @param now Momento de referencia.
 * @return Estado calculado de la toma.
 */
fun StatisticsIntake.status(now: LocalDateTime): StatisticsStatus = when {
    takenAt != null -> StatisticsStatus.TAKEN
    now > scheduledAt.plusMinutes(STATISTICS_MARGIN_MINUTES) -> StatisticsStatus.MISSED
    else -> StatisticsStatus.PENDING
}

/** Margen, en minutos, antes de considerar una toma sin registrar. */
const val STATISTICS_MARGIN_MINUTES = 15L

/**
 * Resumen global de tomas de un periodo.
 *
 * @property taken Tomas registradas.
 * @property missed Tomas pasadas sin registrar.
 * @property pending Tomas futuras o dentro del margen de 15 minutos.
 */
data class StatisticsSummary(
    val taken: Int = 0,
    val missed: Int = 0,
    val pending: Int = 0
) {
    /** Tomas que ya deberían haberse registrado. */
    val evaluated: Int get() = taken + missed

    /** Tomas totales incluidas en el periodo. */
    val total: Int get() = taken + missed + pending

    /**
     * Porcentaje de adherencia sobre las tomas evaluadas, o null si todavía
     * no hay ninguna.
     */
    val adherencePercent: Int? get() = adherencePercent(taken, missed)
}

/**
 * Resumen de tomas de un medicamento dentro del periodo.
 *
 * @property medicationId Identificador del medicamento.
 * @property medicationName Nombre del medicamento en la toma más reciente.
 * @property dose Dosis de la toma más reciente.
 * @property unit Unidad de la dosis de la toma más reciente.
 * @property imageUrl Imagen actual del medicamento, si existe.
 * @property isArchived Indica si el medicamento está archivado actualmente.
 * @property taken Tomas registradas.
 * @property missed Tomas pasadas sin registrar.
 * @property pending Tomas futuras o dentro del margen de 15 minutos.
 */
data class MedicationStatistics(
    val medicationId: Int,
    val medicationName: String,
    val dose: Double,
    val unit: String,
    val imageUrl: String?,
    val isArchived: Boolean,
    val taken: Int,
    val missed: Int,
    val pending: Int
) {
    /** Tomas que ya deberían haberse registrado. */
    val evaluated: Int get() = taken + missed

    /** Tomas totales incluidas en el periodo. */
    val total: Int get() = taken + missed + pending

    /** Porcentaje de adherencia, o null si no hay tomas evaluadas. */
    val adherencePercent: Int? get() = adherencePercent(taken, missed)
}

/**
 * Tomas de un día concreto, usadas por el gráfico semanal.
 *
 * @property date Día representado.
 * @property taken Tomas registradas.
 * @property missed Tomas pasadas sin registrar.
 * @property pending Tomas futuras o dentro del margen de 15 minutos.
 */
data class DailyStatistics(
    val date: LocalDate,
    val taken: Int,
    val missed: Int,
    val pending: Int
) {
    /** Tomas que ya deberían haberse registrado. */
    val evaluated: Int get() = taken + missed

    /** Tomas totales del día. */
    val total: Int get() = taken + missed + pending

    /** Porcentaje de adherencia del día, o null si no hay tomas evaluadas. */
    val adherencePercent: Int? get() = adherencePercent(taken, missed)
}

/**
 * Informe completo de estadísticas de un periodo.
 *
 * @property summary Resumen global.
 * @property byMedication Desglose por medicamento, con los activos primero.
 * @property lastSevenDays Tomas de los últimos siete días para el gráfico.
 * @property history Tomas del periodo ordenadas de más reciente a más antigua.
 */
data class StatisticsReport(
    val summary: StatisticsSummary = StatisticsSummary(),
    val byMedication: List<MedicationStatistics> = emptyList(),
    val lastSevenDays: List<DailyStatistics> = emptyList(),
    val history: List<StatisticsIntake> = emptyList()
)

/**
 * Calcula el resumen global de una lista de tomas.
 *
 * @param now Momento de referencia para decidir qué tomas están sin registrar.
 */
fun List<StatisticsIntake>.toStatisticsSummary(now: LocalDateTime): StatisticsSummary {
    var taken = 0
    var missed = 0
    var pending = 0

    for (intake in this) {
        when (intake.status(now)) {
            StatisticsStatus.TAKEN -> taken++
            StatisticsStatus.MISSED -> missed++
            StatisticsStatus.PENDING -> pending++
        }
    }

    return StatisticsSummary(taken = taken, missed = missed, pending = pending)
}

/**
 * Construye el informe completo de estadísticas.
 *
 * @param now Momento de referencia para decidir qué tomas están sin registrar.
 * @param chartDays Número de días que se incluyen en el gráfico diario.
 */
fun List<StatisticsIntake>.toStatisticsReport(
    now: LocalDateTime,
    chartDays: Int = 7
): StatisticsReport {
    val statuses = map { it to it.status(now) }
    val summary = toStatisticsSummary(now)

    val byMedication = statuses
        .groupBy { (intake, _) -> intake.medicationId }
        .map { (_, entries) ->
            val latest = entries.maxByOrNull { (intake, _) -> intake.scheduledAt }
                ?.first
                ?: entries.first().first
            MedicationStatistics(
                medicationId = latest.medicationId,
                medicationName = latest.medicationName,
                dose = latest.dose,
                unit = latest.unit,
                imageUrl = latest.imageUrl,
                isArchived = latest.isArchived,
                taken = entries.count { (_, status) -> status == StatisticsStatus.TAKEN },
                missed = entries.count { (_, status) -> status == StatisticsStatus.MISSED },
                pending = entries.count { (_, status) -> status == StatisticsStatus.PENDING }
            )
        }
        .sortedWith(
            compareBy<MedicationStatistics> { it.isArchived }
                .thenBy { it.medicationName.lowercase() }
        )

    val endDate = now.toLocalDate()
    val safeChartDays = chartDays.coerceAtLeast(0)
    val startDate = endDate.minusDays((safeChartDays - 1).coerceAtLeast(0).toLong())
    val byDate = statuses.groupBy { (intake, _) -> intake.scheduledAt.toLocalDate() }
    val lastSevenDays = (0 until safeChartDays).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        val dayEntries = byDate[date].orEmpty()
        DailyStatistics(
            date = date,
            taken = dayEntries.count { (_, status) -> status == StatisticsStatus.TAKEN },
            missed = dayEntries.count { (_, status) -> status == StatisticsStatus.MISSED },
            pending = dayEntries.count { (_, status) -> status == StatisticsStatus.PENDING }
        )
    }

    val history = sortedWith(
        compareByDescending<StatisticsIntake> { it.scheduledAt }
            .thenByDescending { it.intakeId }
    )

    return StatisticsReport(
        summary = summary,
        byMedication = byMedication,
        lastSevenDays = lastSevenDays,
        history = history
    )
}

/**
 * Calcula el porcentaje de adherencia redondeado.
 *
 * @return Porcentaje entre 0 y 100, o null si no hay tomas evaluadas.
 */
private fun adherencePercent(taken: Int, missed: Int): Int? {
    val evaluated = taken + missed
    if (evaluated <= 0) return null
    return (taken * 100.0 / evaluated).roundToInt()
}
