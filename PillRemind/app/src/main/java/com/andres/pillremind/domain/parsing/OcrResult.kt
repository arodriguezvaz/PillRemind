package com.andres.pillremind.domain.parsing

/**
 * Línea reconocida por el OCR con su caja normalizada (0..1).
 *
 * Las coordenadas se guardan en proporción al ancho y alto de la imagen para
 * que el parser pueda comparar líneas sin depender de la resolución.
 */
data class OcrLine(
    val text: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

/**
 * Resultado del OCR de una imagen.
 *
 * [text] es el texto completo que usa el detector de CN; [lines] conserva la
 * geometría que necesita el parser de la caja para elegir el nombre.
 */
data class OcrResult(
    val text: String,
    val lines: List<OcrLine> = emptyList()
)
