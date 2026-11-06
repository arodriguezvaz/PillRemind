package com.andres.pillremind.ui.components

/**
 * Desplazamiento de la imagen dentro del encuadre cuadrado, en píxeles de
 * pantalla. Un valor positivo mueve la imagen hacia la derecha o hacia abajo.
 */
data class CropOffset(val x: Float, val y: Float) {
    companion object {
        /** Imagen centrada en el encuadre. */
        val Zero = CropOffset(0f, 0f)
    }
}

/**
 * Rectángulo que se recortará de la imagen ya rotada, en píxeles de esa imagen.
 * El recorte siempre es cuadrado, por eso [width] y [height] coinciden.
 */
data class CropRect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
) {
    /** Lado del cuadrado que se recorta. */
    val size: Float get() = width
}

/**
 * Cálculos puros del recorte de fotos.
 *
 * La pantalla de recorte mantiene un cuadrado fijo y mueve la imagen por
 * debajo. Este objeto traduce ese estado (zoom, desplazamiento y rotación) al
 * rectángulo de la imagen original que hay que recortar. No depende de Android
 * para poder probarse con tests de JVM.
 */
object PhotoCropMath {

    /** Zoom máximo permitido sobre el tamaño mínimo que cubre el encuadre. */
    const val MAX_ZOOM = 6f

    private const val QUARTER_TURNS = 4

    /**
     * Normaliza los giros de 90° a un valor entre 0 y 3.
     */
    fun normalizeQuarterTurns(quarterTurns: Int): Int =
        ((quarterTurns % QUARTER_TURNS) + QUARTER_TURNS) % QUARTER_TURNS

    /**
     * Ancho de la imagen una vez aplicados los giros de 90°.
     */
    fun rotatedWidth(width: Int, height: Int, quarterTurns: Int): Int =
        if (normalizeQuarterTurns(quarterTurns) % 2 == 0) width else height

    /**
     * Alto de la imagen una vez aplicados los giros de 90°.
     */
    fun rotatedHeight(width: Int, height: Int, quarterTurns: Int): Int =
        if (normalizeQuarterTurns(quarterTurns) % 2 == 0) height else width

    /**
     * Escala mínima para que la imagen cubra por completo el encuadre cuadrado.
     *
     * Al ser un cuadrado, el resultado es el mismo con cualquier giro de 90°.
     */
    fun baseScale(imageWidth: Int, imageHeight: Int, viewportSize: Float): Float {
        if (imageWidth <= 0 || imageHeight <= 0 || viewportSize <= 0f) return 1f
        return maxOf(viewportSize / imageWidth, viewportSize / imageHeight)
    }

    /**
     * Limita el desplazamiento para que la imagen no deje huecos dentro del
     * encuadre.
     */
    fun clampOffset(
        offset: CropOffset,
        zoom: Float,
        imageWidth: Int,
        imageHeight: Int,
        quarterTurns: Int,
        viewportSize: Float,
    ): CropOffset {
        if (imageWidth <= 0 || imageHeight <= 0 || viewportSize <= 0f) {
            return CropOffset.Zero
        }
        val totalScale = baseScale(imageWidth, imageHeight, viewportSize) *
            zoom.coerceIn(1f, MAX_ZOOM)
        val maxX = (
            (rotatedWidth(imageWidth, imageHeight, quarterTurns) * totalScale - viewportSize) / 2f
            ).coerceAtLeast(0f)
        val maxY = (
            (rotatedHeight(imageWidth, imageHeight, quarterTurns) * totalScale - viewportSize) / 2f
            ).coerceAtLeast(0f)
        return CropOffset(
            x = offset.x.coerceIn(-maxX, maxX),
            y = offset.y.coerceIn(-maxY, maxY),
        )
    }

    /**
     * Rectángulo de la imagen ya rotada que corresponde al cuadrado visible.
     *
     * @param zoom Factor sobre la escala mínima de cobertura; 1 equivale a
     *   encajar la imagen sin ampliarla más de lo necesario.
     * @param offset Desplazamiento actual de la imagen, en píxeles de pantalla.
     * @param imageWidth Ancho de la imagen original (sin girar).
     * @param imageHeight Alto de la imagen original (sin girar).
     * @param quarterTurns Giros de 90° en sentido horario, de 0 a 3.
     * @param viewportSize Lado del cuadrado de recorte, en píxeles.
     */
    fun cropRect(
        zoom: Float,
        offset: CropOffset,
        imageWidth: Int,
        imageHeight: Int,
        quarterTurns: Int,
        viewportSize: Float,
    ): CropRect {
        val effectiveWidth = rotatedWidth(imageWidth, imageHeight, quarterTurns).toFloat()
        val effectiveHeight = rotatedHeight(imageWidth, imageHeight, quarterTurns).toFloat()
        if (effectiveWidth <= 0f || effectiveHeight <= 0f || viewportSize <= 0f) {
            return CropRect(0f, 0f, 0f, 0f)
        }

        val totalScale = baseScale(imageWidth, imageHeight, viewportSize) *
            zoom.coerceIn(1f, MAX_ZOOM)
        val size = (viewportSize / totalScale)
            .coerceIn(0f, minOf(effectiveWidth, effectiveHeight))
        val maxLeft = (effectiveWidth - size).coerceAtLeast(0f)
        val maxTop = (effectiveHeight - size).coerceAtLeast(0f)

        return CropRect(
            left = ((effectiveWidth - size) / 2f - offset.x / totalScale)
                .coerceIn(0f, maxLeft),
            top = ((effectiveHeight - size) / 2f - offset.y / totalScale)
                .coerceIn(0f, maxTop),
            width = size,
            height = size,
        )
    }
}
