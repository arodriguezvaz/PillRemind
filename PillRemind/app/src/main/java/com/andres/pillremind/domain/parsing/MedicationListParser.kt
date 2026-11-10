package com.andres.pillremind.domain.parsing

import com.andres.pillremind.domain.model.ImportedMedication
import com.andres.pillremind.domain.model.MedicationImportWarning
import java.time.LocalTime
import java.util.Locale

/**
 * Interpreta el texto de una hoja de medicación activa.
 *
 * Reconoce primero la plantilla del SERGAS, que agrupa los tratamientos en
 * «longa duración» y «outros tratamentos», y después aplica el mismo patrón de
 * nombre + pauta a otras hojas con una estructura parecida.
 *
 * Es un parser puro: no conoce Android ni PdfBox. El texto se obtiene en la
 * capa de datos y se le pasa como cadena.
 */
object MedicationListParser {

    private val longTermHeader = Regex(
        "(?iu)\\b(TRATAMENTOS?|TRATAMIENTOS?)\\s+DE\\s+" +
            "(?:LONGA|LARGA)\\s+DURACI[ÓO]N\\b"
    )

    private val otherHeader = Regex(
        "(?iu)\\b(OUTROS|OTROS)\\s+TRATAMENTOS?\\b"
    )

    private val posology = Regex(
        "(?iu)\\b(\\d+\\s*/\\s*\\d+|\\d+(?:[.,]\\d+)?)\\s+" +
            "([A-Za-zÁÉÍÓÚÜÑáéíóúüñ]{1,}(?:\\s+" +
            "(?!(?:CADA|AO|AL)\\b)[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]{2,})?)\\s+" +
            "(?:CADA\\s+(\\d+)\\s+H(?:ORAS?)?|(AO|AL)\\s+D[ÍI]A|" +
            "CADA\\s+(\\d+)\\s+D[ÍI]AS?)"
    )

    private val posologyHint = Regex(
        "(?iu)\\b(CADA|AO|AL)\\b.*\\b(HORAS?|D[ÍI]A|D[ÍI]AS?)\\b"
    )

    private val presentation = Regex(
        "(?iu)\\b\\d+\\s+(FRASCO|DOSIS|COMPRIMIDOS?|C[ÁA]PSULAS?|CAPSULAS?|" +
            "SOBRES?|AMPOLLAS?|VIALES?|PARCHES?|PULVERIZACIONES?|ML)\\b"
    )

    private val form = Regex(
        "(?iu)\\b(SUSP|SUSPENSI[ÓO]N|POLVO|COMPRIMID|C[ÁA]PSUL|CAPSUL|" +
            "SOLUCI[ÓO]N|JARABE|PULVERIZ|INHALACI[ÓO]N|RECUBIERT|EFG|" +
            "GRANULADO|CREMA|GEL|POMADA|COLIRIO)\\b"
    )

    private val strength = Regex(
        "(?iu)\\d+(?:[.,]\\d+)?(?:\\s*/\\s*\\d+(?:[.,]\\d+)?)*\\s*" +
            "(?:MG|MCG|G|ML|UI|%)(?:/[A-ZÁÉÍÓÚÜÑ]+)?"
    )

    private val noise = Regex(
        "(?iu)\\b(PACIENTE|CENTRO|M[ÉE]DICO|MEDICO|TEL[ÉE]FONO|TELEFONO|" +
            "URXENCIAS|CUSTO|COSTE|IMPRESO|P[ÁA]XINA|PAGINA|VOSTEDE|USTED|" +
            "RETIRE|INFORMACI[ÓO]N|TARXETA|TARJETA|RECEITA|RECETA|SALVO|" +
            "INDICACI[ÓO]N|F[OÓ]LLA|HOJA|TRATAMENTOS?|TRATAMIENTOS?|" +
            "OUTROS|OTROS|DATA|NACEMENTO|NACIMIENTO|NASCIMENTO|DNI|NIE|" +
            "HISTORIA|ENDEREZO|DIRECCI[ÓO]N|N[ÚU]MERO)\\b"
    )

    private val dateLike = Regex("\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b")

    private val idLike = Regex("(?iu)\\b(?:\\d{8}|[XYZ]\\d{7})[A-Z]\\b")

    /**
     * Convierte el texto extraído de un PDF en medicamentos editables.
     *
     * Los datos de paciente, centro, médico, costes, pies legales y números de
     * página se descartan. Si no se reconoce ninguna pauta, no se inventa un
     * medicamento: se omite la línea.
     */
    fun parse(text: String): List<ImportedMedication> {
        val lines = text
            .replace('\uFEFF', ' ')
            .replace('\u00A0', ' ')
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val medications = mutableListOf<ImportedMedication>()
        val pending = mutableListOf<String>()
        var sectionIsChronic: Boolean? = null

        lines.forEach { line ->
            when {
                longTermHeader.containsMatchIn(line) -> {
                    sectionIsChronic = true
                    pending.clear()
                }

                otherHeader.containsMatchIn(line) -> {
                    sectionIsChronic = false
                    pending.clear()
                }

                else -> {
                    val parsedPosology = parsePosology(line)
                    if (parsedPosology != null) {
                        val nameLine = pending.lastOrNull { strength.containsMatchIn(it) }
                            ?: pending.firstOrNull { looksLikeName(it) }

                        if (nameLine != null) {
                            buildMedication(
                                nameLine = nameLine,
                                posology = parsedPosology,
                                isChronic = sectionIsChronic == true
                            )?.let(medications::add)
                        }
                        pending.clear()
                    } else if (!isNoise(line) && looksLikeName(line)) {
                        pending.add(line)
                        if (pending.size > MAX_PENDING_LINES) {
                            pending.removeAt(0)
                        }
                    }
                }
            }
        }

        return medications
    }

    /**
     * Convierte la parte de dosis y pauta de una línea.
     *
     * Devuelve `null` cuando la línea no contiene una pauta reconocible. En ese
     * caso no se crea un borrador a medias: se descarta la línea y el usuario
     * puede añadir el medicamento a mano.
     */
    private fun parsePosology(line: String): ParsedPosology? {
        val match = posology.find(line) ?: return null

        val doseText = normalizeDose(match.groupValues[1])
        val rawUnit = match.groupValues[2]
        val hours = match.groupValues[3].toIntOrNull()
        val days = match.groupValues[5].toIntOrNull()

        val warnings = mutableListOf<MedicationImportWarning>()
        val unit = unitFor(rawUnit) ?: run {
            warnings += MedicationImportWarning.UNKNOWN_UNIT
            "dosis"
        }

        val times: List<LocalTime>
        val everyNDays: Int
        when {
            days != null -> {
                times = listOf(ImportedMedication.DEFAULT_TIME)
                everyNDays = days.coerceAtLeast(1)
            }

            hours == null -> {
                // «AO DÍA» / «AL DÍA» sin intervalo explícito.
                times = listOf(ImportedMedication.DEFAULT_TIME)
                everyNDays = 1
            }

            hours in setOf(6, 8, 12, 24) -> {
                times = timesForHours(hours)
                everyNDays = 1
            }

            hours > 24 && hours % 24 == 0 -> {
                times = listOf(ImportedMedication.DEFAULT_TIME)
                everyNDays = hours / 24
            }

            else -> {
                warnings += MedicationImportWarning.INTERVAL_NOT_SUPPORTED
                times = listOf(ImportedMedication.DEFAULT_TIME)
                everyNDays = 1
            }
        }

        if (doseText.isBlank()) warnings += MedicationImportWarning.NO_DOSE
        if (times.isEmpty()) warnings += MedicationImportWarning.NO_SCHEDULE

        return ParsedPosology(
            doseText = doseText,
            unit = unit,
            times = times,
            everyNDays = everyNDays,
            warnings = warnings.distinct()
        )
    }

    /**
     * Convierte una dosis con fracción (`1/2`) o coma decimal en un número.
     */
    private fun normalizeDose(raw: String): String {
        val compact = raw.replace(" ", "")
        if ('/' in compact) {
            val parts = compact.split('/')
            val numerator = parts.getOrNull(0)?.replace(',', '.')?.toDoubleOrNull()
            val denominator = parts.getOrNull(1)?.replace(',', '.')?.toDoubleOrNull()
            if (numerator != null && denominator != null && denominator != 0.0) {
                return (numerator / denominator).toString()
            }
        }
        return raw.replace(',', '.').trim()
    }

    /**
     * Horas de toma para los intervalos más habituales.
     */
    private fun timesForHours(hours: Int): List<LocalTime> = when (hours) {
        6 -> listOf(
            LocalTime.of(6, 0),
            LocalTime.of(12, 0),
            LocalTime.of(18, 0),
            LocalTime.of(0, 0)
        )

        8 -> listOf(
            LocalTime.of(8, 0),
            LocalTime.of(16, 0),
            LocalTime.of(0, 0)
        )

        12 -> listOf(
            LocalTime.of(9, 0),
            LocalTime.of(21, 0)
        )

        else -> listOf(ImportedMedication.DEFAULT_TIME)
    }

    /**
     * Unidad de toma en el formato del desplegable de la app.
     */
    private fun unitFor(raw: String): String? {
        val value = raw.uppercase(Locale.ROOT)
        return when {
            value.startsWith("PULSACION") -> "pulsaciones"
            value.startsWith("DOSIS") -> "dosis"
            value.startsWith("COMPRIMID") -> "comprimidos"
            value.startsWith("CÁPSUL") || value.startsWith("CAPSUL") -> "cápsulas"
            value.startsWith("SOBRE") -> "sobres"
            value.startsWith("PARCHE") -> "parches"
            value.startsWith("GOTA") -> "gotas"
            value.startsWith("INYECC") -> "inyecciones"
            value.startsWith("VIAL") -> "viales"
            value.startsWith("AMPOLLA") -> "ampollas"
            value.startsWith("SUPOSITOR") -> "supositorios"
            value.startsWith("APLICACI") -> "aplicaciones"
            value.startsWith("SPRAY") -> "sprays"
            value.startsWith("PUFF") -> "puffs"
            value.startsWith("UNIDAD") -> "dosis"
            value == "ML" -> "ml"
            else -> null
        }
    }

    /**
     * El nombre comercial, sin presentación ni forma farmacéutica.
     */
    private fun shortName(line: String): String {
        val cleaned = line.replace(Regex("\\s+"), " ").trim()
        val presentationStart = presentation.find(cleaned)?.range?.first
        val formStart = form.find(cleaned)?.range?.first
        val cut = listOfNotNull(presentationStart, formStart).minOrNull()
            ?: cleaned.length

        return cleaned.substring(0, cut)
            .trim()
            .trimEnd(',', '-', '/', ' ')
            .ifBlank { cleaned }
    }

    private fun buildMedication(
        nameLine: String,
        posology: ParsedPosology,
        isChronic: Boolean
    ): ImportedMedication? {
        val name = shortName(nameLine)
        if (name.isBlank()) return null

        return ImportedMedication(
            name = name,
            doseText = posology.doseText,
            unit = posology.unit,
            everyNDays = posology.everyNDays,
            times = posology.times,
            isChronic = isChronic,
            strengthText = strength.find(nameLine)?.value,
            warnings = posology.warnings
        )
    }

    /**
     * Descarta cabeceras, datos identificativos, costes y pies legales.
     */
    private fun isNoise(line: String): Boolean {
        if (line.length > 180) return true
        if (noise.containsMatchIn(line)) return true
        if (dateLike.containsMatchIn(line)) return true
        if (idLike.containsMatchIn(line)) return true
        if (line.count(Char::isDigit) > line.length / 2 &&
            !strength.containsMatchIn(line)
        ) {
            return true
        }
        return line.startsWith("·") || line.startsWith("...")
    }

    /**
     * Una línea puede ser el nombre de un medicamento si tiene letras y no es
     * una pauta ni una cabecera. La concentración no es obligatoria: hay
     * medicamentos que solo se identifican por su marca.
     */
    private fun looksLikeName(line: String): Boolean {
        if (line.length !in 3..120) return false
        if (!line.any(Char::isLetter)) return false
        if (posology.find(line) != null) return false
        if (posologyHint.containsMatchIn(line)) return false
        if (dateLike.containsMatchIn(line)) return false
        if (idLike.containsMatchIn(line)) return false
        return true
    }

    private data class ParsedPosology(
        val doseText: String,
        val unit: String,
        val times: List<LocalTime>,
        val everyNDays: Int,
        val warnings: List<MedicationImportWarning>
    )

    private const val MAX_PENDING_LINES = 4
}
