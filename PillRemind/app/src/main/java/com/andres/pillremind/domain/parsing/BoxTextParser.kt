package com.andres.pillremind.domain.parsing

import kotlin.math.abs

/**
 * Campos que se pueden extraer del OCR de la caja de un medicamento.
 *
 * No es un modelo persistente: solo alimenta la pantalla de revisión.
 */
data class BoxFields(
    val name: String? = null,
    val nameCandidates: List<String> = emptyList(),
    val nameConfident: Boolean = true,
    val strengthDoseText: String? = null,
    val strengthUnit: String? = null,
    val strengthText: String? = null,
    val presentationText: String? = null,
    val form: String? = null,
    val intakeDoseText: String? = null,
    val intakeUnit: String? = null
)

/**
 * Parser heurístico del texto de la cara frontal de una caja.
 *
 * No pretende ser perfecto: busca el nombre, la concentración y la forma
 * farmacéutica, y deja el resto para la revisión manual. Cuando recibe la
 * geometría que devuelve el OCR, elige el nombre por tamaño y posición en lugar
 * de por orden de aparición.
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
            "(mg|g|mcg|µg|μg|microgramos?|miligramos?|mililitros?|ml|ui|%)" +
            "(?![a-záéíóúüñ])"
    )

    private val intake = Regex(
        "(?i)(\\d+)\\s*(comprimidos?|cápsulas?|capsulas?|sobres?|parches?|" +
            "puffs?|pulverizaciones?|gotas?|inyecciones?|viales?|ampollas?)"
    )

    /**
     * Contexto que convierte un recuento en una pauta. Sin él, "20
     * comprimidos" se interpreta como tamaño del envase, no como toma.
     */
    private val intakeContext = Regex(
        "(?i)\\b(cada|tomar|tome|dosis|posolog|administr|al d[ií]a|" +
            "horas?|d[ií]as?|v[ií]a|antes|despu[eé]s)\\b"
    )

    private val countableFormUnits = setOf(
        "comprimidos", "cápsulas", "sobres", "parches", "supositorios",
        "inyecciones", "viales", "ampollas"
    )

    /**
     * Palabras que describen el producto o el envase, no su nombre comercial.
     * Se filtran solo cuando toda la línea es un descriptor; una mezcla como
     * "MABOdol CBD" se conserva.
     */
    private val genericNameWords = setOf(
        "CANNABIDIOL", "CBD", "THC", "SIN", "SABOR", "HIDRÓFILA",
        "HIDROFILA", "FÍSICA", "FISICA", "AROMA"
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
     * Analiza el texto completo de la caja sin geometría.
     *
     * Es el modo que usan los tests y las fuentes de texto que no conservan
     * posiciones. Mantiene el comportamiento anterior.
     */
    fun parse(text: String): BoxFields = parse(text, emptyList(), requireGeometry = false)

    /**
     * Analiza el resultado del OCR conservando la posición de cada línea.
     *
     * Si ML Kit no devuelve geometría, el nombre no se da por seguro: se
     * ofrecen los candidatos y el usuario decide.
     */
    fun parse(ocr: OcrResult): BoxFields =
        parse(ocr.text, ocr.lines, requireGeometry = true)

    internal fun parse(
        text: String,
        lines: List<OcrLine>,
        requireGeometry: Boolean
    ): BoxFields {
        val allLines = buildLines(text, lines)
        val useful = allLines.filterNot { isNoise(it.text) }
        val usefulText = useful.map { it.text }

        val strengthMatches = useful.flatMap { line ->
            strength.findAll(line.text).map { match -> StrengthMatch(match, line) }.toList()
        }
        val concentration = strengthMatches.firstOrNull {
            !isMillilitre(it.match.groupValues[2])
        }
        val volume = strengthMatches.firstOrNull {
            isMillilitre(it.match.groupValues[2])
        }
        val strengthMatch = concentration?.match
        val strengthLine = concentration?.line?.text
        val presentationText = volume
            ?.takeIf { !it.line.text.contains('/') }
            ?.let { formatPresentation(it.match) }

        val formLine = formWords.firstNotNullOfOrNull { word ->
            usefulText.firstOrNull { line -> line.contains(word, true) }
        }
        val form = formLine?.let(::canonicalForm)

        val intakeMatches = useful.mapNotNull { line ->
            intake.find(line.text)?.let { match -> match to line }
        }
        val explicitIntakeMatch = intakeMatches.firstOrNull { (match, line) ->
            val unit = mapIntakeUnit(match.groupValues[2])
            unit !in countableFormUnits || intakeContext.containsMatchIn(line.text)
        }
        val explicitIntake = explicitIntakeMatch?.first?.let {
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

        val nameGuess = pickName(
            lines = useful,
            strengthMatch = strengthMatch,
            strengthValues = strengthMatches.map { it.match.value }.toSet(),
            intakeMatch = explicitIntakeMatch?.first,
            requireGeometry = requireGeometry
        )

        return BoxFields(
            name = nameGuess.name,
            nameCandidates = nameGuess.candidates,
            nameConfident = nameGuess.confident,
            strengthDoseText = strengthMatch?.groupValues?.get(1),
            strengthUnit = strengthMatch?.groupValues?.get(2)?.let(::normalizeUnit),
            strengthText = when {
                strengthMatch == null -> null
                strengthLine?.contains('/') == true -> strengthLine
                else -> strengthMatch.value
            },
            presentationText = presentationText,
            form = form,
            intakeDoseText = intakePair?.first,
            intakeUnit = intakePair?.second
        )
    }

    /**
     * Combina las líneas con geometría con las que solo están en el texto.
     *
     * ML Kit puede descartar el `boundingBox` de una línea; en ese caso no se
     * debe perder el texto, solo la puntuación geométrica de esa línea.
     */
    private fun buildLines(text: String, lines: List<OcrLine>): List<CandidateLine> {
        val textLines = text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return textLines.map { CandidateLine(it, null) }
        }

        val result = mutableListOf<CandidateLine>()
        val seen = mutableSetOf<String>()
        lines.forEach { line ->
            val value = line.text.trim()
            if (value.isNotBlank() && seen.add(value)) {
                result.add(CandidateLine(value, line))
            }
        }
        textLines.forEach { line ->
            if (seen.add(line)) {
                result.add(CandidateLine(line, null))
            }
        }
        return result
    }

    private fun isNoise(line: String): Boolean {
        if (line.length > 80) return true
        if (noise.containsMatchIn(line)) return true
        if (line.count(Char::isDigit) > line.length / 2 &&
            !strength.containsMatchIn(line)
        ) {
            return true
        }
        if (line.startsWith("Cada ", ignoreCase = true)) return true
        if (line.startsWith("Fórmula", ignoreCase = true) ||
            line.startsWith("Formula", ignoreCase = true)
        ) {
            return true
        }
        return false
    }

    /**
     * Elige el nombre y calcula si hay suficiente confianza para rellenarlo.
     *
     * Con geometría, la altura de la línea es la señal principal: el nombre
     * comercial suele ser el texto más grande. Un texto pequeño en la franja
     * superior (marca, logo) se penaliza. Sin geometría se conserva el orden de
     * aparición.
     */
    private fun pickName(
        lines: List<CandidateLine>,
        strengthMatch: MatchResult?,
        strengthValues: Set<String>,
        intakeMatch: MatchResult?,
        requireGeometry: Boolean
    ): NameGuess {
        val candidates = lines.filter { line ->
            val text = line.text
            text.length in 3..40 &&
                text.any(Char::isLetter) &&
                text.firstOrNull()?.isLowerCase() != true &&
                !text.trimEnd().endsWith(".") &&
                text.count(Char::isWhitespace) <= 4 &&
                !isGenericNameLine(text) &&
                strengthValues.none { text.contains(it, true) } &&
                formWords.none { text.contains(it, true) } &&
                (intakeMatch == null || !text.contains(intakeMatch.value, true))
        }

        val hasGeometry = lines.any { it.box != null }

        if (candidates.isEmpty()) {
            val fallback = strengthMatch
                ?.let { strengthLinePrefix(lines.map { it.text }, it.value) }
                ?.takeIf { text ->
                    text.length in 3..40 &&
                        text.any(Char::isLetter) &&
                        formWords.none { text.contains(it, true) }
                }
                ?.let(::cleanName)
                ?.takeIf { it.isNotBlank() }
            val fallbackLine = strengthMatch?.let { match ->
                lines.firstOrNull { it.text.contains(match.value, true) }
            }
            val fallbackScore = fallbackLine?.let { nameScore(it) } ?: 0f
            val fallbackTopBand = fallbackLine?.box?.top
                ?.let { it < TOP_BAND } == true
            val confident = if (requireGeometry) {
                fallback != null &&
                    fallbackLine?.box != null &&
                    fallbackScore >= MIN_NAME_SCORE &&
                    !fallbackTopBand &&
                    !isGenericNameLine(fallback)
            } else {
                fallback != null
            }
            return NameGuess(
                name = if (confident) fallback else null,
                candidates = listOfNotNull(fallback),
                confident = confident
            )
        }

        if (!hasGeometry) {
            val chosen = candidates.firstOrNull { it.text.contains('®') }
                ?: candidates.first()
            val names = candidates
                .mapNotNull { cleanName(it.text).takeIf(String::isNotBlank) }
                .distinctBy { it.lowercase() }
                .take(MAX_NAME_CANDIDATES)
            val name = cleanName(chosen.text).takeIf { it.isNotBlank() }
            return NameGuess(
                name = if (requireGeometry) null else name,
                candidates = names,
                confident = !requireGeometry && name != null
            )
        }

        val scored = candidates
            .mapNotNull { line ->
                val name = cleanName(line.text)
                if (name.isBlank()) {
                    null
                } else {
                    ScoredName(
                        name = name,
                        score = nameScore(line),
                        inTopBand = line.box?.top?.let { it < TOP_BAND } == true
                    )
                }
            }
            .sortedByDescending { it.score }
            .distinctBy { it.name.lowercase() }

        if (scored.isEmpty()) {
            return NameGuess(null, emptyList(), confident = false)
        }

        val best = scored.first()
        val second = scored.getOrNull(1)
        val confident = best.score >= MIN_NAME_SCORE &&
            !best.inTopBand &&
            (second == null || best.score >= second.score * NAME_MARGIN)

        return NameGuess(
            name = if (confident) best.name else null,
            candidates = scored.take(MAX_NAME_CANDIDATES).map { it.name },
            confident = confident
        )
    }

    /**
     * Puntuación orientativa del nombre. La altura manda; el área y la
     * centralidad desempatan. Los valores son deliberadamente conservadores:
     * es preferible enseñar candidatos a rellenar un nombre incorrecto.
     */
    private fun nameScore(line: CandidateLine): Float {
        val box = line.box ?: return 0f
        val height = box.height
        val area = box.width * height
        val centrality = 1f - abs(box.centerY - 0.5f)
        val topPenalty = if (box.top < TOP_BAND && height < TOP_BAND_MAX_HEIGHT) {
            TOP_PENALTY
        } else {
            1f
        }
        val trademark = if (line.text.contains('®')) TRADEMARK_BONUS else 1f
        return (height * 2.5f + area * 0.5f + centrality * 0.2f) *
            topPenalty *
            trademark
    }

    private fun isGenericNameLine(text: String): Boolean {
        val words = text
            .split(Regex("\\s+"))
            .map { it.trim(' ', ',', '.', '-', '®').uppercase() }
            .filter { it.isNotBlank() }
        return words.isNotEmpty() && words.all { it in genericNameWords }
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

    private fun formatPresentation(match: MatchResult): String {
        val dose = match.groupValues[1]
        val unit = normalizeUnit(match.groupValues[2]) ?: match.groupValues[2]
        return "$dose $unit"
    }

    private fun isMillilitre(unit: String): Boolean =
        unit.lowercase() in setOf("ml", "mililitros", "mililitro")

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
        "%" -> "%"
        else -> null
    }

    private data class CandidateLine(
        val text: String,
        val box: OcrLine?
    )

    private data class StrengthMatch(
        val match: MatchResult,
        val line: CandidateLine
    )

    private data class ScoredName(
        val name: String,
        val score: Float,
        val inTopBand: Boolean
    )

    private data class NameGuess(
        val name: String?,
        val candidates: List<String>,
        val confident: Boolean
    )

    private const val MAX_NAME_CANDIDATES = 3
    private const val MIN_NAME_SCORE = 0.10f
    private const val NAME_MARGIN = 1.25f
    private const val TOP_BAND = 0.18f
    private const val TOP_BAND_MAX_HEIGHT = 0.09f
    private const val TOP_PENALTY = 0.4f
    private const val TRADEMARK_BONUS = 1.2f
}
