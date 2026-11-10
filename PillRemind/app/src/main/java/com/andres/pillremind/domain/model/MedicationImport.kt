package com.andres.pillremind.domain.model

import java.time.LocalTime

/**
 * Estado de un medicamento dentro de la pantalla de revisión de importación.
 *
 * [NEW] se puede guardar, [EXISTING] ya está en el perfil o repetido en el PDF
 * y [INCOMPLETE] necesita que el usuario complete la pauta.
 */
enum class MedicationImportStatus {
    NEW,
    EXISTING,
    INCOMPLETE
}

/**
 * Avisos que puede dejar el parser o la revisión de la importación.
 */
enum class MedicationImportWarning {
    NO_DOSE,
    NO_SCHEDULE,
    UNKNOWN_UNIT,
    INTERVAL_NOT_SUPPORTED,
    DUPLICATE_IN_PDF
}

/**
 * Medicamento efímero extraído de una hoja de medicación en PDF.
 *
 * No se persiste tal cual: la pantalla de revisión lo convierte en
 * [Medication] y [Schedule] cuando el usuario confirma.
 *
 * @property name Nombre comercial con la concentración.
 * @property doseText Cantidad de una toma, escrita como en el formulario.
 * @property unit Unidad de la toma.
 * @property everyNDays Periodicidad en días de la regla del parser.
 * @property scheduleRules Reglas completas de la pauta; por defecto, la
 * periodicidad de [everyNDays].
 * @property times Horas de toma del día.
 * @property isChronic Si la hoja lo incluye en tratamientos de larga duración.
 * @property strengthText Concentración detectada, para mostrarla como detalle.
 * @property warnings Avisos detectados al interpretar el medicamento.
 */
data class ImportedMedication(
    val name: String,
    val doseText: String,
    val unit: String,
    val everyNDays: Int = 1,
    val scheduleRules: List<ScheduleRule> = listOf(ScheduleRule.EveryNDays(everyNDays)),
    val times: List<LocalTime> = listOf(DEFAULT_TIME),
    val isChronic: Boolean = false,
    val strengthText: String? = null,
    val warnings: List<MedicationImportWarning> = emptyList()
) {
    companion object {
        /** Hora por defecto cuando el PDF no indica una hora concreta. */
        val DEFAULT_TIME: LocalTime = LocalTime.of(9, 0)
    }
}

/**
 * Medicamento y pauta listos para insertarse en una sola transacción.
 */
data class MedicationWithSchedules(
    val medication: Medication,
    val schedules: List<Schedule>
)
