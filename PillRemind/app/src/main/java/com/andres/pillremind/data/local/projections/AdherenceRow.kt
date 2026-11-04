package com.andres.pillremind.data.local.projections

import java.time.LocalDateTime

/**
 * Fila agregada con las tomas de un medicamento dentro de un periodo.
 *
 * @property medicationId Identificador del medicamento.
 * @property medicationName Nombre actual del medicamento.
 * @property dose Dosis actual.
 * @property unit Unidad de la dosis.
 * @property imageUrl Imagen actual, si existe.
 * @property deletedAt Fecha de archivo, o `null` si sigue activo.
 * @property total Tomas incluidas en el periodo.
 * @property taken Tomas registradas.
 * @property missed Tomas pasadas sin registrar.
 */
data class AdherenceRow(
    val medicationId: Int,
    val medicationName: String,
    val dose: Double,
    val unit: String,
    val imageUrl: String?,
    val deletedAt: LocalDateTime?,
    val total: Int,
    val taken: Int,
    val missed: Int
)
