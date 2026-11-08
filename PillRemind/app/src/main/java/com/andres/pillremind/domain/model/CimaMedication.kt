package com.andres.pillremind.domain.model

import java.time.LocalDate

/**
 * Datos oficiales de un medicamento obtenidos de la API de CIMA.
 *
 * No se persisten: solo viven durante el flujo de escaneo. La app no guarda
 * el CN ni el resultado de la consulta.
 *
 * @property cn Código Nacional de 6 dígitos usado en la consulta.
 * @property name Nombre oficial del medicamento.
 * @property dosis Concentración oficial, tal y como la devuelve CIMA.
 * @property form Forma farmacéutica.
 * @property presentation Presentación que coincide con el CN.
 * @property retrievedAt Fecha de la consulta, para mostrar la atribución.
 */
data class CimaMedication(
    val cn: String,
    val name: String,
    val dosis: String?,
    val form: String?,
    val presentation: String?,
    val retrievedAt: LocalDate
)

/**
 * Resultado de buscar un CN en CIMA.
 */
sealed interface CnLookupResult {
    data class Found(val medication: CimaMedication) : CnLookupResult
    data class NotFound(val cn: String) : CnLookupResult
    data class Error(val cn: String, val cause: Throwable? = null) : CnLookupResult
}
