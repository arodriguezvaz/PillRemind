package com.andres.pillremind.ui.screens.main

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas de la selección del estado de la top bar según la ruta visible.
 */
class TopBarStateTest {

    private val fallback = TopBarState(title = "Inicio", showLogo = true)
    private val home = TopBarState(title = "Hoy, 4 de noviembre", showLogo = true)
    private val medications = TopBarState(title = "Medicamentos")

    @Test
    fun `usa el estado publicado por la ruta actual`() {
        val result = resolveTopBarState(
            currentRoute = "medications",
            statesByRoute = mapOf("home" to home, "medications" to medications),
            fallback = fallback,
        )

        assertEquals(medications, result)
    }

    @Test
    fun `ignora el estado de una ruta que ya no esta visible`() {
        val result = resolveTopBarState(
            currentRoute = "home",
            statesByRoute = mapOf("home" to home, "medications" to medications),
            fallback = fallback,
        )

        assertEquals(home, result)
    }

    @Test
    fun `usa el fallback mientras la ruta actual no publica estado`() {
        val result = resolveTopBarState(
            currentRoute = "profile",
            statesByRoute = mapOf("home" to home),
            fallback = fallback,
        )

        assertEquals(fallback, result)
    }

    @Test
    fun `usa el fallback si todavia no hay ruta`() {
        val result = resolveTopBarState(
            currentRoute = null,
            statesByRoute = mapOf("home" to home),
            fallback = fallback,
        )

        assertEquals(fallback, result)
    }
}
