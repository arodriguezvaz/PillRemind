package com.andres.pillremind.ui.screens.medication

import java.time.LocalDate

/**
 * Convierte la dosis escrita en el formulario a un número.
 *
 * Acepta coma o punto como separador decimal, recorta los espacios y exige un
 * valor finito mayor que cero. Así no se guardan dosis vacías, negativas o
 * imposibles por un error de formato.
 *
 * @param input Texto introducido por el usuario.
 * @return La dosis válida, o `null` si el texto no representa una dosis admitida.
 */
internal fun parseDoseInput(input: String): Double? =
    input.trim()
        .replace(',', '.')
        .toDoubleOrNull()
        ?.takeIf { it.isFinite() && it > 0.0 }

/**
 * Comprueba que la fecha de fin no sea anterior a la de inicio.
 *
 * Los valores nulos no invalidan el rango: la obligatoriedad de cada fecha se
 * valida por separado en el formulario.
 */
internal fun isDateRangeValid(startDate: LocalDate?, endDate: LocalDate?): Boolean =
    startDate == null || endDate == null || !endDate.isBefore(startDate)
