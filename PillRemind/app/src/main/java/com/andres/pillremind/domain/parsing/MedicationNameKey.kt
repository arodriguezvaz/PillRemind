package com.andres.pillremind.domain.parsing

import java.text.Normalizer
import java.util.Locale

/**
 * Clave de comparación de nombres de medicamentos para detectar duplicados.
 *
 * El PDF puede escribir `RYALTRIS 25/600MCG/PULSACION` y el perfil tener
 * `Ryaltrís 25/600 mcg/pulsión`. La comparación normaliza mayúsculas, acentos,
 * microgramos y espacios, y separa la marca de la concentración para no
 * confundir dos dosis distintas del mismo producto.
 */
object MedicationNameKey {

    private val strength = Regex(
        "\\d+(?:[.,]\\d+)?(?:\\s*/\\s*\\d+(?:[.,]\\d+)?)*\\s*" +
            "(?:MG|MCG|G|ML|UI|%)",
        RegexOption.IGNORE_CASE
    )

    private val spaces = Regex("\\s+")
    private val numericUnitSpace = Regex("(\\d)\\s+(MG|MCG|G|ML|UI|%)")
    private val slashSpaces = Regex("\\s*/\\s*")
    private val obsoleteSuffix = Regex("\\b(EFG|GENERICO|GENÉRICO)\\b")

    /**
     * Normaliza un nombre para compararlo.
     */
    fun normalize(name: String): String {
        val withoutAccents = Normalizer.normalize(
            name.replace("µ", "mc").replace("μ", "mc"),
            Normalizer.Form.NFD
        ).replace(Regex("\\p{M}+"), "")

        return withoutAccents
            .uppercase(Locale.ROOT)
            .replace(obsoleteSuffix, " ")
            .replace(Regex("[^A-Z0-9/%\\s]"), " ")
            .replace(slashSpaces, "/")
            .replace(numericUnitSpace, "$1$2")
            .replace(spaces, " ")
            .trim()
    }

    /**
     * Separa la marca de la concentración.
     *
     * @return Marca normalizada y concentración normalizada, o `null` si el
     * nombre no contiene una concentración reconocible.
     */
    fun brandAndStrength(name: String): Pair<String, String?> {
        val normalized = normalize(name)
        val match = strength.find(normalized) ?: return normalized to null
        val brand = normalized.substring(0, match.range.first).trim()
        return (brand.ifBlank { normalized }) to match.value.trim()
    }

    /**
     * Indica si dos nombres corresponden al mismo medicamento.
     *
     * Si uno de los dos no tiene concentración, se compara solo la marca para
     * no arriesgarse a crear una ficha duplicada.
     */
    fun matches(first: String, second: String): Boolean {
        val (firstBrand, firstStrength) = brandAndStrength(first)
        val (secondBrand, secondStrength) = brandAndStrength(second)

        if (firstBrand != secondBrand) return false
        if (firstStrength == null || secondStrength == null) return true
        return firstStrength == secondStrength
    }
}
