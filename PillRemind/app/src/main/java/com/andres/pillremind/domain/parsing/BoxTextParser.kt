package com.andres.pillremind.domain.parsing

/**
 * Campos que se pueden extraer del OCR de la caja de un medicamento.
 *
 * No es un modelo persistente: solo alimenta la pantalla de revisión.
 */
data class BoxFields(
    val name: String? = null,
    val strengthDoseText: String? = null,
    val strengthUnit: String? = null,
    val strengthText: String? = null,
    val form: String? = null,
    val intakeDoseText: String? = null,
    val intakeUnit: String? = null
)

/**
 * Parser heurístico del texto de la cara frontal de una caja.
 *
 * No pretende ser perfecto: busca el nombre, la concentración y la forma
 * farmacéutica, y deja el resto para la revisión manual.
 */
object BoxTextParser {

    private val noise = Regex(
        "(?i)\\b(lote|lot|exp|cad|cn|pc|sn|prospecto|vía|via|fabric|registro|" +
            "certificado|dirección|direccion|tel|farmacéutico|farmaceutico|" +
            "laboratorio|S\\.?A\\.?|EFG|ORGANON|SIDUS|SANDOZ|NOVARTIS|STADA|" +
            "ASTRAZENECA|CINFA|NORMON|conservar|agitar|leer|medicamento)\\b"
    )

    private val strength = Regex(
        "(?i)(\\d{1,4}(?:[.,]\\d+)?)\\s*" +
            "(mg|g|mcg|µg|μg|microgramos?|miligramos?|mililitros?|ml|ui|%)"
    )

    private val intake = Regex(
        "(?i)(\\d+)\\s*(comprimidos?|cápsulas?|capsulas?|sobres?|parches?|" +
            "puffs?|pulverizaciones?|gotas?|inyecciones?|viales?|ampollas?)"
    )

    /**
     * Orden de prioridad: las formas más específicas primero. Así
     * "suspensión para pulverización nasal" se queda con pulverización y no
     * con suspensión.
     */
    private val formWords = listOf(
        "pulveriz", "comprim", "cápsul", "capsul", "sobre", "parche",
        "supositor", "inyect", "gota", "vial", "ampolla", "polvo",
        "granulado", "suspens", "soluc", "jarabe"
    )

    /**
     * Analiza el texto completo de la caja.
     */
    fun parse(text: String): BoxFields {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val useful = lines.filterNot(::isNoise)

        val strengthMatch = useful.firstNotNullOfOrNull { strength.find(it) }
        val strengthLine = strengthMatch?.let { match ->
            useful.firstOrNull { it.contains(match.value, ignoreCase = true) }
        }
        val formLine = formWords.firstNotNullOfOrNull { word ->
            useful.firstOrNull { line -> line.contains(word, true) }
        }
        val form = formLine?.let(::canonicalForm)

        val intakeMatch = useful.firstNotNullOfOrNull { intake.find(it) }
        val explicitIntake = intakeMatch?.let {
            it.groupValues[1] to mapIntakeUnit(it.groupValues[2])
        }
        val impliedIntake = when {
            form == "pulverización" -> "1" to "puffs"
            form == "comprimidos" -> "1" to "comprimidos"
            form == "cápsulas" -> "1" to "cápsulas"
            form == "sobres" -> "1" to "sobres"
            form == "parches" -> "1" to "parches"
            form == "supositorios" -> "1" to "supositorios"
            form == "inyecciones" -> "1" to "inyecciones"
            else -> null
        }
        val intakePair = explicitIntake ?: impliedIntake

        val name = pickName(useful, strengthMatch, formLine, intakeMatch)

        return BoxFields(
            name = name,
            strengthDoseText = strengthMatch?.groupValues?.get(1),
            strengthUnit = strengthMatch?.groupValues?.get(2)?.let(::normalizeUnit),
            strengthText = when {
                strengthMatch == null -> null
                strengthLine?.contains('/') == true -> strengthLine
                else -> strengthMatch.value
            },
            form = form,
            intakeDoseText = intakePair?.first,
            intakeUnit = intakePair?.second
        )
    }

    private fun isNoise(line: String): Boolean {
        if (line.length > 80) return true
        if (noise.containsMatchIn(line)) return true
        if (line.count(Char::isDigit) > line.length / 2) return true
        if (line.startsWith("Cada ", ignoreCase = true)) return true
        if (line.startsWith("Fórmula", ignoreCase = true) ||
            line.startsWith("Formula", ignoreCase = true)
        ) return true
        return false
    }

    private fun pickName(
        lines: List<String>,
        strengthMatch: MatchResult?,
        formLine: String?,
        intakeMatch: MatchResult?
    ): String? {
        val candidates = lines.filter { line ->
            line.length in 3..40 &&
                line.any(Char::isLetter) &&
                line.firstOrNull()?.isLowerCase() != true &&
                !line.trimEnd().endsWith(".") &&
                line.count(Char::isWhitespace) <= 4 &&
                (strengthMatch == null || !line.contains(strengthMatch.value, true)) &&
                (formLine == null || line != formLine) &&
                (intakeMatch == null || !line.contains(intakeMatch.value, true))
        }

        val chosen = candidates.firstOrNull { it.contains('®') }
            ?: candidates.firstOrNull()

        if (chosen != null) return cleanName(chosen).takeIf { it.isNotBlank() }

        // Si toda la información está en la misma línea, usa el texto anterior
        // a la concentración: "IBUPROFENO 600 MG COMPRIMIDOS".
        val fallback = strengthMatch?.let { match ->
            strengthLinePrefix(lines, match.value)
        }?.takeIf { text ->
            text.length in 3..40 &&
                text.any(Char::isLetter) &&
                formWords.none { text.contains(it, true) }
        }

        return fallback?.let(::cleanName)?.takeIf { it.isNotBlank() }
    }

    private fun strengthLinePrefix(lines: List<String>, strengthValue: String): String? {
        val line = lines.firstOrNull { it.contains(strengthValue, true) } ?: return null
        return line.substringBefore(strengthValue, "").trim().takeIf { it.isNotBlank() }
    }

    private fun cleanName(line: String): String =
        line
            .replace("®", "")
            .replace(Regex("[\\s\\d().,-]+$"), "")
            .trim()

    private fun canonicalForm(line: String): String = when {
        line.contains("comprim", true) -> "comprimidos"
        line.contains("cápsul", true) || line.contains("capsul", true) -> "cápsulas"
        line.contains("sobre", true) -> "sobres"
        line.contains("parche", true) -> "parches"
        line.contains("pulveriz", true) -> "pulverización"
        line.contains("supositor", true) -> "supositorios"
        line.contains("inyect", true) -> "inyecciones"
        line.contains("gota", true) -> "gotas"
        line.contains("vial", true) -> "viales"
        line.contains("ampolla", true) -> "ampollas"
        line.contains("polvo", true) -> "polvo"
        line.contains("granulado", true) -> "granulado"
        line.contains("suspens", true) -> "suspensión"
        line.contains("soluc", true) -> "solución"
        line.contains("jarabe", true) -> "jarabe"
        else -> line.lowercase()
    }

    private fun mapIntakeUnit(raw: String): String = when {
        raw.startsWith("comprim", true) -> "comprimidos"
        raw.startsWith("cápsul", true) || raw.startsWith("capsul", true) -> "cápsulas"
        raw.startsWith("sobre", true) -> "sobres"
        raw.startsWith("parche", true) -> "parches"
        raw.startsWith("puff", true) -> "puffs"
        raw.startsWith("pulveriz", true) -> "puffs"
        raw.startsWith("gota", true) -> "gotas"
        raw.startsWith("inyecc", true) -> "inyecciones"
        raw.startsWith("vial", true) -> "viales"
        raw.startsWith("ampolla", true) -> "ampollas"
        else -> "dosis"
    }

    private fun normalizeUnit(raw: String): String? = when (raw.lowercase()) {
        "mg", "miligramos", "miligramo" -> "mg"
        "g", "gramos", "gramo" -> "g"
        "mcg", "µg", "μg", "microgramos", "microgramo" -> "mcg"
        "ml", "mililitros", "mililitro" -> "ml"
        "ui" -> "UI"
        else -> null
    }
}
