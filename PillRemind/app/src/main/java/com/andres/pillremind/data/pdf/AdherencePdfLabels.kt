package com.andres.pillremind.data.pdf

/**
 * Textos del informe de adherencia en PDF.
 *
 * Se reciben ya resueltos desde la capa de UI para que el generador no dependa
 * de recursos Android y pueda probarse con textos fijos.
 *
 * @property title Título del documento.
 * @property profileLabel Etiqueta del perfil.
 * @property periodLabel Etiqueta del periodo consultado.
 * @property generatedAtLabel Etiqueta de la fecha de generación.
 * @property adherenceLabel Etiqueta del porcentaje de adherencia.
 * @property evaluatedLabel Etiqueta de las tomas evaluadas.
 * @property takenLabel Etiqueta de las tomas registradas.
 * @property missedLabel Etiqueta de las tomas sin registrar.
 * @property pendingLabel Etiqueta de las tomas pendientes.
 * @property noDataLabel Texto para un porcentaje sin datos.
 * @property archivedLabel Marca de medicamento archivado.
 * @property byMedicationTitle Título del desglose por medicamento.
 * @property medicationColumn Cabecera de la columna del medicamento.
 * @property missedTitle Título de la lista de tomas sin registrar.
 * @property missedEmpty Texto cuando no hay tomas sin registrar.
 * @property disclaimer Aviso sobre el origen y el alcance de los datos.
 * @property footer Texto del pie de página.
 * @property pageFormat Formato del número de página (`Página %1$d de %2$d`).
 * @property unknownProfile Nombre usado si el perfil no tiene nombre.
 * @property percentPattern Formato del porcentaje (`%1$d%%`).
 * @property datePattern Patrón de fecha (`dd/MM/yyyy`).
 */
data class AdherencePdfLabels(
    val title: String,
    val profileLabel: String,
    val periodLabel: String,
    val generatedAtLabel: String,
    val adherenceLabel: String,
    val evaluatedLabel: String,
    val takenLabel: String,
    val missedLabel: String,
    val pendingLabel: String,
    val noDataLabel: String,
    val archivedLabel: String,
    val byMedicationTitle: String,
    val medicationColumn: String,
    val missedTitle: String,
    val missedEmpty: String,
    val disclaimer: String,
    val footer: String,
    val pageFormat: String,
    val unknownProfile: String,
    val percentPattern: String,
    val datePattern: String
)
