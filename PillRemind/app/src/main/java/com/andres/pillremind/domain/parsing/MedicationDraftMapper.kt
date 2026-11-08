package com.andres.pillremind.domain.parsing

import com.andres.pillremind.domain.model.CimaMedication
import com.andres.pillremind.domain.model.DraftSource
import com.andres.pillremind.domain.model.MedicationDraft

/**
 * Convierte los datos de CIMA o del OCR de la caja en un borrador editable
 * para el formulario.
 *
 * La regla de dosis acordada:
 * - formas sólidas: una toma (`1` + comprimidos/cápsulas...),
 * - líquidos e inhaladores: la concentración o la dosis que indique la pauta.
 *
 * El borrador guarda las dos opciones cuando existen para que la pantalla de
 * revisión permita cambiar de una a otra.
 */
object MedicationDraftMapper {

    private val doseRegex = Regex(
        "(\\d{1,4}(?:[.,]\\d+)?)\\s*" +
            "(mg|g|mcg|µg|μg|microgramos?|miligramos?|mililitros?|ml|ui|%)",
        RegexOption.IGNORE_CASE
    )

    private val longFormWords = listOf(
        "COMPRIMID", "CÁPSUL", "CAPSUL", "SUSPENSI", "SOLUCI", "JARABE",
        "PULVERIZ", "PULSACION", "POLVO", "GRANULADO", "SOBRE", "GOTAS",
        "PARCH", "SUPOSITOR", "INYECT", "VIAL", "AMPOLLA", "CREMA", "GEL",
        "POMADA", "COLIRIO"
    )

    /**
     * Crea el borrador a partir de la ficha oficial de CIMA.
     */
    fun fromCima(medication: CimaMedication): MedicationDraft {
        val strength = parseDose(medication.dosis)
        val intake = formToIntake(medication.form)

        return MedicationDraft(
            name = shortName(medication.name),
            doseText = intake?.first ?: strength?.first.orEmpty(),
            unit = intake?.second ?: strength?.second.orEmpty(),
            form = medication.form,
            strengthText = medication.dosis,
            presentation = medication.presentation,
            cn = medication.cn,
            source = DraftSource.CIMA,
            retrievedAt = medication.retrievedAt,
            intakeDoseText = intake?.first,
            intakeUnit = intake?.second,
            strengthDoseText = strength?.first,
            strengthUnit = strength?.second
        )
    }

    /**
     * Crea el borrador a partir del OCR de la caja.
     */
    fun fromOcr(fields: BoxFields, cn: String?): MedicationDraft {
        val intake = fields.intakeDoseText?.let { dose ->
            fields.intakeUnit?.let { unit -> dose to unit }
        }
        val strength = fields.strengthDoseText?.let { dose ->
            fields.strengthUnit?.let { unit -> dose to unit }
        }

        return MedicationDraft(
            name = fields.name.orEmpty(),
            doseText = intake?.first ?: strength?.first.orEmpty(),
            unit = intake?.second ?: strength?.second.orEmpty(),
            form = fields.form,
            strengthText = fields.strengthText,
            cn = cn,
            source = DraftSource.OCR,
            notCatalogued = true,
            intakeDoseText = intake?.first,
            intakeUnit = intake?.second,
            strengthDoseText = strength?.first,
            strengthUnit = strength?.second
        )
    }

    /**
     * Acorta el nombre oficial para que quepa en la ficha.
     *
     * Corta antes de la forma farmacéutica y conserva la marca y la
     * concentración. Si no encuentra una forma reconocible, devuelve el nombre
     * completo.
     */
    fun shortName(name: String): String {
        val upper = name.uppercase()
        val index = longFormWords
            .map { upper.indexOf(it) }
            .filter { it > 0 }
            .minOrNull()
        val short = if (index != null) {
            name.substring(0, index).trim().trimEnd(',', '-', ' ', '/')
        } else {
            name.trim()
        }
        return short.ifBlank { name.trim() }
    }

    /**
     * Convierte una dosis de CIMA en número y unidad.
     *
     * En las combinaciones (`25 µg/600 µg`) usa el primer valor; el texto
     * completo se conserva en [MedicationDraft.strengthText].
     */
    internal fun parseDose(dosis: String?): Pair<String, String>? {
        if (dosis.isNullOrBlank()) return null
        val match = doseRegex.find(dosis) ?: return null
        val unit = normalizeUnit(match.groupValues[2]) ?: return null
        return match.groupValues[1] to unit
    }

    /**
     * Unidad de toma sugerida para una forma farmacéutica.
     */
    internal fun formToIntake(form: String?): Pair<String, String>? {
        if (form.isNullOrBlank()) return null
        val upper = form.uppercase()
        val unit = when {
            upper.contains("COMPRIMID") -> "comprimidos"
            upper.contains("CÁPSUL") || upper.contains("CAPSUL") -> "cápsulas"
            upper.contains("SOBRE") -> "sobres"
            upper.contains("PARCHE") -> "parches"
            upper.contains("PULVERIZ") -> "puffs"
            upper.contains("SUPOSITOR") -> "supositorios"
            upper.contains("INYECT") -> "inyecciones"
            upper.contains("GOTAS") -> "gotas"
            upper.contains("VIAL") -> "viales"
            upper.contains("AMPOLLA") -> "ampollas"
            else -> return null
        }
        return "1" to unit
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
