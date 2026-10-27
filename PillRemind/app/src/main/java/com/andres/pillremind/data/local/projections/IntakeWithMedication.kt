package com.andres.pillremind.data.local.projections

import java.time.LocalDateTime

/**
 * Proyección utilizada para mostrar una toma junto con la información
 * del medicamento y del perfil al que pertenece.
 *
 * Los campos de nombre, dosis y unidad se leen de los snapshots guardados
 * en la toma para que el historial no cambie al editar el medicamento.
 *
 * @property intakeId Identificador local de la toma.
 * @property medicationId Identificador del medicamento propietario.
 * @property scheduledAt Fecha y hora programadas para la toma.
 * @property scheduleId Identificador del horario asociado, si todavía existe.
 * @property takenAt Fecha y hora en la que se registró la toma. Es null si aún no se ha tomado.
 * @property medicationName Nombre del medicamento en el momento de crear la toma.
 * @property dose Cantidad que debe administrarse en cada toma.
 * @property unit Unidad de medida de la dosis (mg, ml, comprimidos, etc.).
 * @property imageUrl URL de la imagen del medicamento. Puede ser null.
 * @property profileName Nombre del perfil al que pertenece la toma.
 */
data class IntakeWithMedication(
    val intakeId: Int,
    val medicationId: Int,
    val scheduledAt: LocalDateTime,
    val scheduleId: Int?,
    val takenAt: LocalDateTime?,
    val medicationName: String,
    val dose: Double,
    val unit: String,
    val imageUrl: String?,
    val profileName: String
)

/**
 * Obtiene el estado visual de una toma en función de si ha sido registrada
 * y del tiempo transcurrido desde la hora programada.
 *
 * Estados posibles:
 * - TAKEN: la toma ha sido registrada.
 * - DELAYED: han transcurrido más de 15 minutos desde la hora programada.
 * - PENDING: la toma aún está pendiente y se encuentra dentro del margen permitido.
 *
 * @return Estado visual de la toma.
 */
fun IntakeWithMedication.visualState(): IntakeVisualState {
    if (takenAt != null) return IntakeVisualState.TAKEN
    return if (LocalDateTime.now() > scheduledAt.plusMinutes(15))
        IntakeVisualState.DELAYED
    else
        IntakeVisualState.PENDING
}


/**
 * Estados visuales posibles para una toma de medicamento.
 */
enum class IntakeVisualState {
    /** La toma ha sido registrada. */
    TAKEN,

    /** La toma sigue pendiente y ha superado el margen de 15 minutos. */
    DELAYED,

    /** La toma sigue pendiente y aún se encuentra dentro del horario previsto. */
    PENDING
}
