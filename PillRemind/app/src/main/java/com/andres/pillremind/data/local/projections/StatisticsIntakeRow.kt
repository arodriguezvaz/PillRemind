package com.andres.pillremind.data.local.projections

import java.time.LocalDateTime

/**
 * Proyección de Room con una toma y los datos necesarios para las estadísticas.
 *
 * Nombre, dosis y unidad proceden de los snapshots de la toma. La imagen y la
 * fecha de archivo se leen del medicamento actual. Esta clase pertenece a la
 * capa de datos; el repositorio la mapea a
 * [com.andres.pillremind.domain.model.StatisticsIntake].
 *
 * @property intakeId Identificador de la toma.
 * @property medicationId Identificador del medicamento.
 * @property scheduledAt Fecha y hora programadas.
 * @property takenAt Fecha y hora en las que se registró la toma.
 * @property medicationName Nombre del medicamento en el momento de la toma.
 * @property dose Dosis en el momento de la toma.
 * @property unit Unidad de la dosis en el momento de la toma.
 * @property imageUrl Imagen actual del medicamento, si existe.
 * @property deletedAt Fecha de archivo del medicamento, o null si está activo.
 */
data class StatisticsIntakeRow(
    val intakeId: Int,
    val medicationId: Int,
    val scheduledAt: LocalDateTime,
    val takenAt: LocalDateTime?,
    val medicationName: String,
    val dose: Double,
    val unit: String,
    val imageUrl: String?,
    val deletedAt: LocalDateTime?
)
