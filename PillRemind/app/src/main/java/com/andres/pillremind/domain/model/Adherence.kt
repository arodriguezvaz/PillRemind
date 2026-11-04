package com.andres.pillremind.domain.model

import kotlin.math.roundToInt

/**
 * Periodo disponible para consultar la adherencia al tratamiento.
 *
 * @property days Número de días que se incluyen, contando hoy.
 */
enum class AdherencePeriod(val days: Long) {
    SEVEN(7),
    THIRTY(30),
    NINETY(90)
}

/**
 * Resumen de tomas de un medicamento dentro de un periodo.
 *
 * Una toma se considera omitida cuando su hora programada ya pasó y no se
 * registró. Las tomas futuras quedan como pendientes y no penalizan el
 * porcentaje de adherencia.
 *
 * @property medicationId Identificador del medicamento.
 * @property medicationName Nombre actual del medicamento.
 * @property dose Dosis actual.
 * @property unit Unidad de la dosis.
 * @property imageUrl Imagen actual del medicamento, si existe.
 * @property isArchived Indica si el medicamento está archivado.
 * @property total Número total de tomas del periodo.
 * @property taken Tomas registradas.
 * @property missed Tomas pasadas sin registrar.
 */
data class MedicationAdherence(
    val medicationId: Int,
    val medicationName: String,
    val dose: Double,
    val unit: String,
    val imageUrl: String?,
    val isArchived: Boolean,
    val total: Int,
    val taken: Int,
    val missed: Int
) {
    /** Tomas del periodo que todavía no han vencido. */
    val pending: Int get() = (total - taken - missed).coerceAtLeast(0)

    /**
     * Porcentaje de adherencia sobre las tomas que ya deberían haberse
     * registrado. Es `null` si todavía no hay ninguna toma vencida.
     */
    val adherencePercent: Int?
        get() {
            val evaluated = taken + missed
            if (evaluated <= 0) return null
            return (taken * 100.0 / evaluated).roundToInt()
        }
}

/**
 * Resumen global de adherencia de todos los medicamentos de un perfil.
 */
data class AdherenceSummary(
    val taken: Int,
    val missed: Int,
    val pending: Int,
    val adherencePercent: Int?
)

/**
 * Agrega las estadísticas de una lista de medicamentos.
 */
fun List<MedicationAdherence>.toAdherenceSummary(): AdherenceSummary {
    val taken = sumOf { it.taken }
    val missed = sumOf { it.missed }
    val pending = sumOf { it.pending }
    val evaluated = taken + missed

    return AdherenceSummary(
        taken = taken,
        missed = missed,
        pending = pending,
        adherencePercent = if (evaluated == 0) null else (taken * 100.0 / evaluated).roundToInt()
    )
}
