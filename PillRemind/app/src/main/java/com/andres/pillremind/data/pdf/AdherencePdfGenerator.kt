package com.andres.pillremind.data.pdf

import com.andres.pillremind.domain.model.MedicationStatistics
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.domain.model.StatisticsReport
import com.andres.pillremind.domain.model.StatisticsStatus
import com.andres.pillremind.domain.model.status
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDFont
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Datos que se imprimen en el informe de adherencia.
 *
 * @property profileName Nombre del perfil activo.
 * @property period Periodo seleccionado en la pantalla de estadísticas.
 * @property reference Momento usado para calcular el rango y los estados.
 * @property report Informe ya calculado por el dominio.
 */
data class AdherencePdfReport(
    val profileName: String,
    val period: StatisticsPeriod,
    val reference: LocalDateTime,
    val report: StatisticsReport
)

/**
 * Genera un informe de adherencia en PDF con PdfBox.
 *
 * El documento incluye la cabecera del perfil y del periodo, el resumen, el
 * desglose por medicamento y las tomas sin registrar. La lista completa se
 * pagina y cada página lleva un pie con el número de página.
 *
 * No usa recursos Android: los textos llegan en [AdherencePdfLabels] y el
 * generador puede probarse con un JVM normal.
 */
@Singleton
class AdherencePdfGenerator @Inject constructor() {

    /**
     * Escribe el informe en el flujo indicado.
     *
     * El flujo no se cierra; lo gestiona quien llama.
     *
     * @param output Destino del documento.
     * @param report Datos y periodo que se van a imprimir.
     * @param labels Textos ya resueltos desde la UI.
     * @param locale Configuración regional para los números y las fechas.
     */
    fun write(
        output: OutputStream,
        report: AdherencePdfReport,
        labels: AdherencePdfLabels,
        locale: Locale = Locale.getDefault()
    ) {
        val endDate = report.reference.toLocalDate()
        val startDate = endDate.minusDays((report.period.days - 1).coerceAtLeast(0))
        val dateFormatter = DateTimeFormatter.ofPattern(labels.datePattern, locale)
        val dateTimeFormatter = DateTimeFormatter.ofPattern(
            "${labels.datePattern} HH:mm",
            locale
        )
        val missedIntakes = report.report.history
            .filter { it.status(report.reference) == StatisticsStatus.MISSED }
            .sortedByDescending { it.scheduledAt }

        PDDocument().use { document ->
            val writer = PdfWriter(document)
            writer.newPage()

            drawHeader(
                writer = writer,
                profileName = report.profileName,
                startDate = startDate,
                endDate = endDate,
                generatedAt = dateTimeFormatter.format(report.reference),
                labels = labels,
                dateFormatter = dateFormatter
            )
            drawSummary(writer, report.report, labels, locale)
            drawDisclaimer(writer, labels)
            drawMedicationTable(writer, report.report.byMedication, labels, locale)
            drawMissedIntakes(writer, missedIntakes, labels, dateTimeFormatter, locale)

            writer.finish()
            drawFooters(document, labels, locale)
            document.save(output)
        }
    }

    private fun drawHeader(
        writer: PdfWriter,
        profileName: String,
        startDate: LocalDate,
        endDate: LocalDate,
        generatedAt: String,
        labels: AdherencePdfLabels,
        dateFormatter: DateTimeFormatter
    ) {
        writer.writeLine(labels.title, font = PDType1Font.HELVETICA_BOLD, size = 20f, spacingAfter = 8f)
        writer.writeLine(
            text = "${labels.profileLabel}: ${profileName.ifBlank { labels.unknownProfile }}",
            size = 11f,
            spacingAfter = 2f
        )
        writer.writeLine(
            text = "${labels.periodLabel}: ${dateFormatter.format(startDate)} – ${dateFormatter.format(endDate)}",
            size = 10f,
            color = MUTED,
            spacingAfter = 2f
        )
        writer.writeLine(
            text = "${labels.generatedAtLabel}: $generatedAt",
            size = 10f,
            color = MUTED,
            spacingAfter = 8f
        )
        writer.line(MARGIN, writer.y, CONTENT_RIGHT, writer.y, BORDER)
        writer.advance(12f)
    }

    private fun drawSummary(
        writer: PdfWriter,
        report: StatisticsReport,
        labels: AdherencePdfLabels,
        locale: Locale
    ) {
        val summary = report.summary
        writer.ensureSpace(SUMMARY_HEIGHT + 10f)
        val top = writer.y
        writer.fillRect(MARGIN, top, CONTENT_WIDTH, SUMMARY_HEIGHT, SUMMARY_BACKGROUND)

        val adherence = summary.adherencePercent?.let {
            String.format(locale, labels.percentPattern, it)
        } ?: labels.noDataLabel
        val adherenceSize = if (summary.adherencePercent == null) 15f else 23f
        writer.drawText(
            text = adherence,
            x = MARGIN + 14f,
            baseline = top - 30f,
            font = PDType1Font.HELVETICA_BOLD,
            size = adherenceSize,
            color = ACCENT
        )
        writer.drawText(
            text = labels.adherenceLabel,
            x = MARGIN + 14f,
            baseline = top - 46f,
            font = PDType1Font.HELVETICA,
            size = 9f,
            color = MUTED
        )

        val counts = listOf(
            "${labels.takenLabel}: ${summary.taken}",
            "${labels.missedLabel}: ${summary.missed}",
            "${labels.pendingLabel}: ${summary.pending}",
            "${labels.evaluatedLabel}: ${summary.evaluated}"
        )
        counts.forEachIndexed { index, text ->
            writer.drawText(
                text = text,
                x = MARGIN + 210f,
                baseline = top - 20f - index * 12f,
                font = PDType1Font.HELVETICA,
                size = 9.5f,
                color = TEXT
            )
        }

        writer.advance(SUMMARY_HEIGHT + 10f)
    }

    private fun drawDisclaimer(writer: PdfWriter, labels: AdherencePdfLabels) {
        val size = 8.5f
        val lineHeight = 11f
        val lines = wrap(labels.disclaimer, PDType1Font.HELVETICA_OBLIQUE, size, CONTENT_WIDTH)
        val height = lines.size * lineHeight + 4f
        writer.ensureSpace(height)
        lines.forEachIndexed { index, line ->
            writer.drawText(
                text = line,
                x = MARGIN,
                baseline = writer.y - size - index * lineHeight,
                font = PDType1Font.HELVETICA_OBLIQUE,
                size = size,
                color = MUTED
            )
        }
        writer.advance(height + 6f)
    }

    private fun drawMedicationTable(
        writer: PdfWriter,
        statistics: List<MedicationStatistics>,
        labels: AdherencePdfLabels,
        locale: Locale
    ) {
        writer.ensureSpace(80f)
        writer.writeSectionTitle(labels.byMedicationTitle)

        if (statistics.isEmpty()) {
            writer.writeParagraph(labels.noDataLabel)
            return
        }

        drawTableHeader(writer, labels)
        for (row in statistics) {
            val nameLines = wrap(
                text = row.medicationName,
                font = PDType1Font.HELVETICA,
                size = 10f,
                maxWidth = MEDICATION_NAME_WIDTH
            ).take(MAX_MEDICATION_NAME_LINES)
            val dose = formatDose(row.dose, row.unit, locale)
            val doseLine = if (row.isArchived) "$dose · ${labels.archivedLabel}" else dose
            val rowHeight = max(
                MIN_TABLE_ROW_HEIGHT,
                6f + nameLines.size * 12f + 12f
            )

            if (!writer.hasSpace(rowHeight)) {
                writer.newPage()
                drawTableHeader(writer, labels)
            }

            val top = writer.y
            val nameTop = top - 8f
            nameLines.forEachIndexed { index, line ->
                writer.drawText(
                    text = line,
                    x = MARGIN + 8f,
                    baseline = nameTop - index * 12f,
                    font = PDType1Font.HELVETICA,
                    size = 10f,
                    color = TEXT
                )
            }
            writer.drawText(
                text = doseLine,
                x = MARGIN + 8f,
                baseline = nameTop - nameLines.size * 12f - 2f,
                font = PDType1Font.HELVETICA,
                size = 8.5f,
                color = MUTED
            )

            val numberBaseline = top - rowHeight / 2f - 3f
            writer.drawRightText(
                text = row.taken.toString(),
                right = TAKEN_COLUMN_RIGHT,
                baseline = numberBaseline,
                size = 10f
            )
            writer.drawRightText(
                text = row.missed.toString(),
                right = MISSED_COLUMN_RIGHT,
                baseline = numberBaseline,
                size = 10f
            )
            writer.drawRightText(
                text = row.pending.toString(),
                right = PENDING_COLUMN_RIGHT,
                baseline = numberBaseline,
                size = 10f
            )
            val adherence = row.adherencePercent?.let {
                String.format(locale, labels.percentPattern, it)
            } ?: labels.noDataLabel
            writer.drawRightText(
                text = adherence,
                right = ADHERENCE_COLUMN_RIGHT,
                baseline = numberBaseline,
                font = PDType1Font.HELVETICA_BOLD,
                size = 10f
            )

            writer.line(MARGIN, top - rowHeight, CONTENT_RIGHT, top - rowHeight, BORDER)
            writer.advance(rowHeight)
        }
        writer.advance(10f)
    }

    private fun drawTableHeader(writer: PdfWriter, labels: AdherencePdfLabels) {
        val height = TABLE_HEADER_HEIGHT
        writer.ensureSpace(height + 8f)
        val top = writer.y
        writer.fillRect(MARGIN, top, CONTENT_WIDTH, height, TABLE_HEADER_BACKGROUND)
        val baseline = top - 14f
        writer.drawText(
            text = labels.medicationColumn,
            x = MARGIN + 8f,
            baseline = baseline,
            font = PDType1Font.HELVETICA_BOLD,
            size = 9f,
            color = TEXT
        )
        writer.drawRightText(labels.takenLabel, TAKEN_COLUMN_RIGHT, baseline, size = 9f, bold = true)
        writer.drawRightText(labels.missedLabel, MISSED_COLUMN_RIGHT, baseline, size = 9f, bold = true)
        writer.drawRightText(labels.pendingLabel, PENDING_COLUMN_RIGHT, baseline, size = 9f, bold = true)
        writer.drawRightText(labels.adherenceLabel, ADHERENCE_COLUMN_RIGHT, baseline, size = 9f, bold = true)
        writer.advance(height)
    }

    private fun drawMissedIntakes(
        writer: PdfWriter,
        intakes: List<StatisticsIntake>,
        labels: AdherencePdfLabels,
        dateTimeFormatter: DateTimeFormatter,
        locale: Locale
    ) {
        writer.ensureSpace(60f)
        writer.writeSectionTitle("${labels.missedTitle} (${intakes.size})")

        if (intakes.isEmpty()) {
            writer.writeParagraph(labels.missedEmpty)
            return
        }

        for (intake in intakes) {
            val detail = "${intake.medicationName} · ${formatDose(intake.dose, intake.unit, locale)}"
            val detailLines = wrap(
                text = detail,
                font = PDType1Font.HELVETICA,
                size = 9.5f,
                maxWidth = MISSED_DETAIL_WIDTH
            )
            val rowHeight = max(16f, detailLines.size * 11f + 5f)

            if (!writer.hasSpace(rowHeight)) {
                writer.newPage()
            }

            val top = writer.y
            writer.drawText(
                text = dateTimeFormatter.format(intake.scheduledAt),
                x = MARGIN,
                baseline = top - 10f,
                font = PDType1Font.HELVETICA,
                size = 9.5f,
                color = MUTED
            )
            detailLines.forEachIndexed { index, line ->
                writer.drawText(
                    text = line,
                    x = MARGIN + MISSED_DETAIL_X,
                    baseline = top - 10f - index * 11f,
                    font = PDType1Font.HELVETICA,
                    size = 9.5f,
                    color = TEXT
                )
            }
            writer.line(MARGIN, top - rowHeight, CONTENT_RIGHT, top - rowHeight, BORDER)
            writer.advance(rowHeight)
        }
        writer.advance(8f)
    }

    private fun drawFooters(
        document: PDDocument,
        labels: AdherencePdfLabels,
        locale: Locale
    ) {
        val totalPages = document.numberOfPages
        val footerSize = 8f
        val footerBaseline = 30f

        document.pages.forEachIndexed { index, page ->
            PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true
            ).use { stream ->
                stream.beginText()
                stream.setFont(PDType1Font.HELVETICA, footerSize)
                stream.setNonStrokingColor(MUTED.red, MUTED.green, MUTED.blue)
                stream.newLineAtOffset(MARGIN, footerBaseline)
                stream.showText(labels.footer.toPdfText())
                stream.endText()

                val pageText = String.format(locale, labels.pageFormat, index + 1, totalPages)
                val pageWidth = textWidth(pageText, PDType1Font.HELVETICA, footerSize)
                stream.beginText()
                stream.setFont(PDType1Font.HELVETICA, footerSize)
                stream.setNonStrokingColor(MUTED.red, MUTED.green, MUTED.blue)
                stream.newLineAtOffset(CONTENT_RIGHT - pageWidth, footerBaseline)
                stream.showText(pageText.toPdfText())
                stream.endText()
            }
        }
    }

    /**
     * Escritor que añade páginas y lleva el cursor vertical del documento.
     */
    private class PdfWriter(private val document: PDDocument) {

        private var stream: PDPageContentStream? = null

        /** Coordenada superior del contenido pendiente. */
        var y: Float = 0f
            private set

        /** Abre una página nueva y reinicia el cursor. */
        fun newPage() {
            closeStream()
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)
            stream = PDPageContentStream(document, page)
            y = PDRectangle.A4.height - MARGIN
        }

        /** Indica si queda espacio suficiente en la página actual. */
        fun hasSpace(height: Float): Boolean = y - height >= BOTTOM_MARGIN

        /** Salta de página si el contenido no cabe. */
        fun ensureSpace(height: Float) {
            if (!hasSpace(height)) newPage()
        }

        /** Baja el cursor sin dibujar. */
        fun advance(height: Float) {
            y -= height
        }

        /** Escribe una línea completa desde el margen izquierdo. */
        fun writeLine(
            text: String,
            size: Float = 10f,
            font: PDFont = PDType1Font.HELVETICA,
            color: PdfColor = TEXT,
            spacingAfter: Float = 2f
        ) {
            ensureSpace(size + spacingAfter)
            drawText(text, MARGIN, y - size, font, size, color)
            y -= size + spacingAfter
        }

        /** Escribe un título de sección. */
        fun writeSectionTitle(text: String) {
            ensureSpace(30f)
            drawText(
                text = text,
                x = MARGIN,
                baseline = y - 13f,
                font = PDType1Font.HELVETICA_BOLD,
                size = 13f,
                color = ACCENT
            )
            y -= 21f
        }

        /** Escribe un párrafo corto con el texto de relleno de una sección. */
        fun writeParagraph(text: String) {
            val lines = wrap(text, PDType1Font.HELVETICA, 10f, CONTENT_WIDTH)
            val height = lines.size * 12f + 4f
            ensureSpace(height)
            lines.forEachIndexed { index, line ->
                drawText(
                    text = line,
                    x = MARGIN,
                    baseline = y - 10f - index * 12f,
                    font = PDType1Font.HELVETICA,
                    size = 10f,
                    color = TEXT
                )
            }
            y -= height
        }

        /** Dibuja texto en una posición absoluta. */
        fun drawText(
            text: String,
            x: Float,
            baseline: Float,
            font: PDFont,
            size: Float,
            color: PdfColor
        ) {
            val content = requireNotNull(stream)
            content.beginText()
            content.setFont(font, size)
            content.setNonStrokingColor(color.red, color.green, color.blue)
            content.newLineAtOffset(x, baseline)
            content.showText(text.toPdfText())
            content.endText()
        }

        /** Dibuja texto alineado a la derecha. */
        fun drawRightText(
            text: String,
            right: Float,
            baseline: Float,
            font: PDFont = PDType1Font.HELVETICA,
            size: Float,
            bold: Boolean = false
        ) {
            val effectiveFont = if (bold) PDType1Font.HELVETICA_BOLD else font
            val x = right - textWidth(text, effectiveFont, size)
            drawText(text, x, baseline, effectiveFont, size, TEXT)
        }

        /** Dibuja una línea horizontal o vertical. */
        fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: PdfColor) {
            val content = requireNotNull(stream)
            content.setStrokingColor(color.red, color.green, color.blue)
            content.setLineWidth(0.6f)
            content.moveTo(x1, y1)
            content.lineTo(x2, y2)
            content.stroke()
        }

        /** Rellena un rectángulo indicando su borde superior. */
        fun fillRect(x: Float, top: Float, width: Float, height: Float, color: PdfColor) {
            val content = requireNotNull(stream)
            content.setNonStrokingColor(color.red, color.green, color.blue)
            content.addRect(x, top - height, width, height)
            content.fill()
        }

        /** Cierra el flujo de la página actual. */
        fun finish() {
            closeStream()
        }

        private fun closeStream() {
            stream?.close()
            stream = null
        }
    }

    /**
     * Color RGB independiente de Android y de los tokens de Compose.
     */
    private data class PdfColor(val red: Float, val green: Float, val blue: Float)

    private companion object {
        const val MARGIN = 48f
        const val CONTENT_WIDTH = 499.28f
        const val CONTENT_RIGHT = MARGIN + CONTENT_WIDTH
        const val BOTTOM_MARGIN = 60f
        const val SUMMARY_HEIGHT = 58f
        const val TABLE_HEADER_HEIGHT = 22f
        const val MIN_TABLE_ROW_HEIGHT = 30f
        const val MAX_MEDICATION_NAME_LINES = 2
        const val MEDICATION_NAME_WIDTH = 215f
        const val TAKEN_COLUMN_RIGHT = 328f
        const val MISSED_COLUMN_RIGHT = 408f
        const val PENDING_COLUMN_RIGHT = 468f
        const val ADHERENCE_COLUMN_RIGHT = CONTENT_RIGHT
        const val MISSED_DETAIL_X = 115f
        const val MISSED_DETAIL_WIDTH = CONTENT_WIDTH - MISSED_DETAIL_X

        val TEXT = PdfColor(0.12f, 0.16f, 0.20f)
        val MUTED = PdfColor(0.38f, 0.42f, 0.46f)
        val BORDER = PdfColor(0.78f, 0.82f, 0.85f)
        val ACCENT = PdfColor(0.12f, 0.31f, 0.47f)
        val SUMMARY_BACKGROUND = PdfColor(0.92f, 0.95f, 0.98f)
        val TABLE_HEADER_BACKGROUND = PdfColor(0.89f, 0.93f, 0.95f)
    }
}

/** Caracteres admitidos por las fuentes estándar de PDF. */
private val PdfCharset: Charset = Charset.forName("windows-1252")

/**
 * Convierte un texto en algo que se pueda escribir con las fuentes estándar
 * de PDF. WinAnsi cubre el español y el gallego, pero no emojis ni alfabetos
 * no latinos: esos caracteres se sustituyen por `?` para no romper el informe.
 */
private fun String.toPdfText(): String {
    val normalized = replace("\r\n", " ")
        .replace('\n', ' ')
        .replace('\r', ' ')
        .replace('\t', ' ')
    val encoder = PdfCharset.newEncoder()
    val result = StringBuilder(normalized.length)
    var index = 0
    while (index < normalized.length) {
        val codePoint = normalized.codePointAt(index)
        val text = String(Character.toChars(codePoint))
        val encodable = runCatching { encoder.canEncode(text) }.getOrDefault(false)
        result.append(if (encodable) text else "?")
        index += Character.charCount(codePoint)
    }
    return result.toString()
}

/**
 * Mide un texto con una fuente y un tamaño.
 */
private fun textWidth(text: String, font: PDFont, size: Float): Float =
    font.getStringWidth(text.toPdfText()) / 1000f * size

/**
 * Parte un texto en líneas que caben en el ancho indicado.
 */
private fun wrap(text: String, font: PDFont, size: Float, maxWidth: Float): List<String> {
    val normalized = text.toPdfText().replace(Regex("\\s+"), " ").trim()
    if (normalized.isEmpty()) return listOf("")

    val lines = mutableListOf<String>()
    var current = ""
    for (word in normalized.split(' ')) {
        val candidate = if (current.isEmpty()) word else "$current $word"
        if (textWidth(candidate, font, size) <= maxWidth) {
            current = candidate
            continue
        }

        if (current.isNotEmpty()) {
            lines += current
        }

        var rest = word
        while (textWidth(rest, font, size) > maxWidth && rest.length > 1) {
            var cut = rest.length - 1
            while (cut > 1 && textWidth(rest.take(cut), font, size) > maxWidth) {
                cut--
            }
            lines += rest.take(cut)
            rest = rest.drop(cut)
        }
        current = rest
    }
    if (current.isNotEmpty()) {
        lines += current
    }
    return lines.ifEmpty { listOf("") }
}

/**
 * Formatea una dosis sin decimales innecesarios y sin separadores de miles.
 */
private fun formatDose(dose: Double, unit: String, locale: Locale): String {
    val numberFormat = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = 3
        minimumFractionDigits = 0
        isGroupingUsed = false
    }
    val number = numberFormat.format(dose)
    return if (unit.isBlank()) number else "$number $unit"
}
