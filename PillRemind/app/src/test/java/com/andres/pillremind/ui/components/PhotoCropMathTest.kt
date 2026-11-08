package com.andres.pillremind.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del cálculo puro del recorte 1:1.
 */
class PhotoCropMathTest {

    private val imageWidth = 4000
    private val imageHeight = 3000
    private val viewport = 1000f

    @Test
    fun `baseScale cubre el cuadrado usando el lado corto`() {
        val scale = PhotoCropMath.baseScale(imageWidth, imageHeight, viewport)

        assertEquals(viewport / imageHeight, scale, 0.0001f)
    }

    @Test
    fun `rotatedWidth y rotatedHeight intercambian dimensiones con giros impares`() {
        assertEquals(3000, PhotoCropMath.rotatedWidth(imageWidth, imageHeight, 1))
        assertEquals(4000, PhotoCropMath.rotatedHeight(imageWidth, imageHeight, 1))
        assertEquals(4000, PhotoCropMath.rotatedWidth(imageWidth, imageHeight, 2))
        assertEquals(3000, PhotoCropMath.rotatedHeight(imageWidth, imageHeight, 2))
        assertEquals(3000, PhotoCropMath.rotatedWidth(imageWidth, imageHeight, -1))
        assertEquals(4000, PhotoCropMath.rotatedWidth(imageWidth, imageHeight, 4))
        assertEquals(3000, PhotoCropMath.rotatedWidth(imageWidth, imageHeight, 5))
    }

    @Test
    fun `cropRect centra el cuadrado sin zoom ni desplazamiento`() {
        val rect = PhotoCropMath.cropRect(
            zoom = 1f,
            offset = CropOffset.Zero,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 0,
            viewportSize = viewport,
        )

        assertEquals(3000f, rect.size, 0.01f)
        assertEquals(rect.width, rect.height, 0.001f)
        assertEquals(500f, rect.left, 0.01f)
        assertEquals(0f, rect.top, 0.01f)
    }

    @Test
    fun `cropRect reduce el area al ampliar`() {
        val rect = PhotoCropMath.cropRect(
            zoom = 2f,
            offset = CropOffset.Zero,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 0,
            viewportSize = viewport,
        )

        assertEquals(1500f, rect.size, 0.01f)
        assertEquals(1250f, rect.left, 0.01f)
        assertEquals(750f, rect.top, 0.01f)
    }

    @Test
    fun `cropRect mueve la ventana al arrastrar en horizontal`() {
        val rect = PhotoCropMath.cropRect(
            zoom = 1f,
            offset = CropOffset(x = 100f, y = 0f),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 0,
            viewportSize = viewport,
        )

        assertEquals(200f, rect.left, 0.01f)
        assertEquals(0f, rect.top, 0.01f)
    }

    @Test
    fun `cropRect mueve la ventana al arrastrar en vertical`() {
        val rect = PhotoCropMath.cropRect(
            zoom = 1f,
            offset = CropOffset(x = 0f, y = 100f),
            imageWidth = 3000,
            imageHeight = 4000,
            quarterTurns = 0,
            viewportSize = viewport,
        )

        assertEquals(0f, rect.left, 0.01f)
        assertEquals(200f, rect.top, 0.01f)
    }

    @Test
    fun `clampOffset no deja huecos en los bordes`() {
        val clamped = PhotoCropMath.clampOffset(
            offset = CropOffset(x = 1000f, y = 1000f),
            zoom = 1f,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 0,
            viewportSize = viewport,
        )

        assertEquals((viewport / 3f) / 2f, clamped.x, 0.01f)
        assertEquals(0f, clamped.y, 0.01f)

        val negative = PhotoCropMath.clampOffset(
            offset = CropOffset(x = -1000f, y = -1000f),
            zoom = 1f,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 0,
            viewportSize = viewport,
        )
        assertEquals(-(viewport / 3f) / 2f, negative.x, 0.01f)
        assertEquals(0f, negative.y, 0.01f)
    }

    @Test
    fun `clampOffset permite mas desplazamiento al ampliar`() {
        val clamped = PhotoCropMath.clampOffset(
            offset = CropOffset(x = 10000f, y = 10000f),
            zoom = 2f,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 0,
            viewportSize = viewport,
        )

        assertEquals((4000f * 2f / 3f - viewport) / 2f, clamped.x, 0.01f)
        assertEquals((3000f * 2f / 3f - viewport) / 2f, clamped.y, 0.01f)
    }

    @Test
    fun `cropRect tiene en cuenta la rotacion de 90 grados`() {
        val rect = PhotoCropMath.cropRect(
            zoom = 1f,
            offset = CropOffset.Zero,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 1,
            viewportSize = viewport,
        )

        assertEquals(3000f, rect.size, 0.01f)
        assertEquals(0f, rect.left, 0.01f)
        assertEquals(500f, rect.top, 0.01f)
    }

    @Test
    fun `cropRect normaliza los giros negativos`() {
        val negative = PhotoCropMath.cropRect(
            zoom = 1.5f,
            offset = CropOffset(75f, -40f),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = -1,
            viewportSize = viewport,
        )
        val equivalent = PhotoCropMath.cropRect(
            zoom = 1.5f,
            offset = CropOffset(75f, -40f),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 3,
            viewportSize = viewport,
        )

        assertEquals(equivalent.left, negative.left, 0.001f)
        assertEquals(equivalent.top, negative.top, 0.001f)
        assertEquals(equivalent.size, negative.size, 0.001f)
    }

    @Test
    fun `cropRect queda dentro de la imagen con desplazamientos extremos`() {
        val turns = 3
        val rect = PhotoCropMath.cropRect(
            zoom = 3f,
            offset = CropOffset(x = 99999f, y = -99999f),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = turns,
            viewportSize = viewport,
        )
        val rotatedWidth = PhotoCropMath.rotatedWidth(imageWidth, imageHeight, turns)
        val rotatedHeight = PhotoCropMath.rotatedHeight(imageWidth, imageHeight, turns)

        assertTrue(rect.left >= 0f)
        assertTrue(rect.top >= 0f)
        assertTrue(rect.left + rect.width <= rotatedWidth + 0.01f)
        assertTrue(rect.top + rect.height <= rotatedHeight + 0.01f)
    }

    @Test
    fun `cropRect limita el zoom maximo`() {
        val maxZoom = PhotoCropMath.cropRect(
            zoom = PhotoCropMath.MAX_ZOOM,
            offset = CropOffset.Zero,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 0,
            viewportSize = viewport,
        )
        val beyondMax = PhotoCropMath.cropRect(
            zoom = 100f,
            offset = CropOffset.Zero,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            quarterTurns = 0,
            viewportSize = viewport,
        )

        assertEquals(maxZoom.size, beyondMax.size, 0.001f)
        assertEquals(500f, maxZoom.size, 0.01f)
    }
}
