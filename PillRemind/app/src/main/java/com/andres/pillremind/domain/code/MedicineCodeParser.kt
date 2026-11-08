package com.andres.pillremind.domain.code

/**
 * Extrae el Código Nacional (CN) de los códigos que llevan los envases españoles.
 *
 * Los medicamentos financiados por el SNS usan un EAN-13 con el prefijo
 * `847000`, seguido del CN de 6 dígitos y el dígito de control. Los envases
 * serializados pueden llevar un DataMatrix GS1 con el CN en el campo `(712)` o
 * un NTIN que empieza por `0847000`.
 *
 * No hace ninguna llamada de red: solo interpreta el texto que devuelve el
 * lector de códigos.
 */
object MedicineCodeParser {

    /** Prefijo de los EAN-13 que incluyen el CN (GTIN-13/SF). */
    private const val OSS_EAN_PREFIX = "847000"

    /** Prefijo de los NTIN que incluyen el CN. */
    private const val NTIN_PREFIX = "0847000"

    private const val CN_LENGTH = 6

    /** Separador GS1 (FNC1) que puede aparecer en el DataMatrix. */
    private const val FNC1 = '\u001d'

    /**
     * Devuelve el CN de un EAN-13 español, o `null` si el código no es válido
     * o no sigue el formato `847000 + CN + control`.
     */
    fun fromEan13(raw: String): String? {
        val code = raw.trim()
        if (!isValidEan13(code)) return null
        if (!code.startsWith(OSS_EAN_PREFIX)) return null
        return code.substring(6, 12)
    }

    /**
     * Devuelve el CN de un DataMatrix GS1, o `null` si no lo contiene.
     *
     * Acepta tanto el NTIN `0847000...` como el campo `(712)` con el CN de
     * 7 dígitos. También tolera que el lector elimine el separador FNC1.
     */
    fun fromGs1(raw: String): String? {
        val value = raw.trim()
        if (value.isEmpty()) return null

        // GTIN sin el AI 01: algunos lectores devuelven solo los dígitos.
        if (value.length >= 14 && value.startsWith(NTIN_PREFIX)) {
            return value.substring(7, 13)
        }

        for (segment in value.split(FNC1)) {
            parseSegment(segment)?.let { return it }
        }
        return null
    }

    /**
     * Comprueba el dígito de control de un EAN-13.
     */
    fun isValidEan13(code: String): Boolean {
        if (code.length != 13 || !code.all(Char::isDigit)) return false
        val sum = code.take(12)
            .mapIndexed { index, char ->
                char.digitToInt() * if (index % 2 == 0) 1 else 3
            }
            .sum()
        val check = (10 - sum % 10) % 10
        return check == code[12].digitToInt()
    }

    /**
     * Recorre los AI de un segmento GS1.
     *
     * Solo se interpretan los campos de longitud fija que pueden aparecer antes
     * del CN. Los campos variables (`10`, `21`...) cortan el recorrido cuando
     * no hay separador, porque sin FNC1 no se sabe dónde terminan.
     */
    private fun parseSegment(segment: String): String? {
        var index = 0
        while (index < segment.length) {
            when {
                segment.startsWith("01", index) && index + 16 <= segment.length -> {
                    val gtin = segment.substring(index + 2, index + 16)
                    if (gtin.all(Char::isDigit) && gtin.startsWith(NTIN_PREFIX)) {
                        return gtin.substring(7, 13)
                    }
                    index += 16
                }

                segment.startsWith("712", index) && index + 10 <= segment.length -> {
                    val nhrn = segment.substring(index + 3, index + 10)
                    if (nhrn.all(Char::isDigit)) return nhrn.take(CN_LENGTH)
                    index += 3
                }

                segment.startsWith("17", index) && index + 8 <= segment.length -> index += 8
                segment.startsWith("11", index) && index + 8 <= segment.length -> index += 8
                segment.startsWith("10", index) || segment.startsWith("21", index) -> return null
                else -> index++
            }
        }
        return null
    }
}
