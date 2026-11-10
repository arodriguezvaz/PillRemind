package com.andres.pillremind.domain.parsing

import com.andres.pillremind.domain.code.MedicineCodeParser

/**
 * Extrae el Código Nacional de un texto de OCR.
 *
 * Los envases españoles suelen imprimir el CN como `C.N. 215936.5` o
 * `CN 663526.1`: seis dígitos seguidos del dígito de control. CIMA usa solo
 * los seis primeros.
 */
object CnParser {

    private val labeled = Regex(
        "(?i)\\bC\\.?\\s*N\\.?\\s*[:.]?\\s*(\\d{6})(?:[.\\s-]?(\\d))?"
    )
    private val dotted = Regex("(?i)\\b(\\d{6})[.\\s-](\\d)\\b")
    private val sevenDigits = Regex("\\b(\\d{7})\\b")
    private val sixDigits = Regex("\\b(\\d{6})\\b")

    /**
     * Devuelve los CN candidatos en orden de aparición.
     *
     * Se priorizan las formas con etiqueta o con dígito de control separado.
     * El último recurso son bloques de seis dígitos; la consulta a CIMA
     * descarta los que no correspondan a un medicamento.
     */
    fun extractCandidates(text: String): List<String> {
        val normalized = text.replace('\u00a0', ' ').replace('\n', ' ')
        val found = LinkedHashSet<String>()

        labeled.findAll(normalized).forEach { found.add(it.groupValues[1]) }
        dotted.findAll(normalized).forEach { found.add(it.groupValues[1]) }

        if (found.isEmpty()) {
            sevenDigits.findAll(normalized).forEach { found.add(it.groupValues[1].take(6)) }
        }
        if (found.isEmpty()) {
            sixDigits.findAll(normalized).forEach { found.add(it.groupValues[1]) }
        }

        return found.filter { it.length == 6 }.toList()
    }

    /**
     * Normaliza un CN escrito a mano: acepta seis o siete dígitos y descarta
     * el de control.
     */
    fun normalize(input: String): String? {
        val digits = input.filter(Char::isDigit)
        return when {
            digits.length == 6 -> digits
            digits.length == 7 -> digits.take(6)
            else -> null
        }
    }

    /**
     * Devuelve el CN de un EAN-13 o DataMatrix ya decodificado.
     */
    fun fromAnyCode(raw: String): String? =
        MedicineCodeParser.fromEan13(raw) ?: MedicineCodeParser.fromGs1(raw)
}
