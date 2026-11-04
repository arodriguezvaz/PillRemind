package com.andres.pillremind.domain.model

/**
 * Convierte el texto de dosis escrito por el usuario en un número válido.
 *
 * Se acepta la coma decimal habitual en español (`1,5`) además del punto.
 * Se rechazan valores vacíos, no numéricos, no finitos (`NaN`, `Infinity`) y
 * dosis menores o iguales que cero.
 *
 * @param input Texto introducido en el formulario.
 * @return Dosis válida, o `null` si el texto no representa una dosis aceptable.
 */
fun parseDose(input: String): Double? {
    val normalized = input.trim().replace(',', '.')
    if (normalized.isEmpty()) return null

    val value = normalized.toDoubleOrNull() ?: return null
    return value.takeIf { it.isFinite() && it > 0.0 }
}

/**
 * Comprueba que un medicamento se puede guardar sin dejar datos incoherentes.
 *
 * La validación se aplica en la capa de datos además de en el formulario para
 * que una copia de seguridad manipulada o una llamada interna no pueda saltarse
 * las reglas.
 *
 * @throws IllegalArgumentException Si algún campo obligatorio no es válido.
 */
fun Medication.requireValidForSaving() {
    require(name.isNotBlank()) { "El medicamento debe tener nombre" }
    require(unit.isNotBlank()) { "El medicamento debe tener unidad" }
    require(dose.isFinite() && dose > 0.0) { "La dosis debe ser un número positivo" }
    require(endDate == null || !endDate.isBefore(startDate)) {
        "La fecha de fin no puede ser anterior a la fecha de inicio"
    }
}
