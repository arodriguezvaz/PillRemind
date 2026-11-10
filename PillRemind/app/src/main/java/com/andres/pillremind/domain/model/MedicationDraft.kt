package com.andres.pillremind.domain.model

import java.time.LocalDate

/**
 * Origen de los datos de un borrador de medicamento.
 */
enum class DraftSource {
    CIMA,
    OCR,
    MANUAL
}

/**
 * Datos detectados para rellenar el formulario de medicamento.
 *
 * Es un objeto efímero: se usa en la pantalla de revisión y se descarta al
 * aplicar los valores al formulario. No se guarda en Room ni en el backup.
 *
 * @property nameCandidates Nombres alternativos detectados en la caja, de mayor
 * a menor confianza. La UI los ofrece cuando [nameConfident] es `false`.
 * @property nameConfident Indica si el OCR ha identificado el nombre con
 * suficiente seguridad. Si es `false`, [name] está vacío y el usuario debe
 * elegir un candidato o escribirlo.
 * @property doseText Dosis seleccionada inicialmente, tal y como se escribe en
 * el formulario (admite coma decimal).
 * @property unit Unidad seleccionada inicialmente.
 * @property intakeDoseText Dosis expresada como toma (por ejemplo, 1 cápsula).
 * @property intakeUnit Unidad de la toma.
 * @property strengthDoseText Dosis expresada como concentración (por ejemplo,
 * 20 mg).
 * @property strengthUnit Unidad de la concentración.
 */
data class MedicationDraft(
    val name: String = "",
    val nameCandidates: List<String> = emptyList(),
    val nameConfident: Boolean = true,
    val doseText: String = "",
    val unit: String = "",
    val form: String? = null,
    val strengthText: String? = null,
    val presentation: String? = null,
    val cn: String? = null,
    val source: DraftSource = DraftSource.MANUAL,
    val notCatalogued: Boolean = false,
    val retrievedAt: LocalDate? = null,
    val intakeDoseText: String? = null,
    val intakeUnit: String? = null,
    val strengthDoseText: String? = null,
    val strengthUnit: String? = null
)
