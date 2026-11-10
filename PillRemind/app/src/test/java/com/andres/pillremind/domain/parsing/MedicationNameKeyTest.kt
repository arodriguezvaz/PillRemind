package com.andres.pillremind.domain.parsing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la clave de comparación de nombres de medicamentos.
 */
class MedicationNameKeyTest {

    @Test
    fun `ignora mayusculas acentos y microgramos`() {
        assertTrue(
            MedicationNameKey.matches(
                "RYALTRIS 25/600MCG/PULSACION",
                "Ryaltrís 25/600 µg/pulsación"
            )
        )
    }

    @Test
    fun `no confunde dos concentraciones del mismo producto`() {
        assertFalse(
            MedicationNameKey.matches(
                "RELVAR ELLIPTA 92/22MCG",
                "RELVAR ELLIPTA 184/22MCG"
            )
        )
    }

    @Test
    fun `compara solo la marca cuando el existente no tiene concentracion`() {
        assertTrue(
            MedicationNameKey.matches(
                "RELVAR ELLIPTA",
                "RELVAR ELLIPTA 92/22MCG"
            )
        )
    }

    @Test
    fun `no mezcla marcas distintas`() {
        assertFalse(
            MedicationNameKey.matches(
                "ENALAPRIL 5MG",
                "OMEPRAZOL 5MG"
            )
        )
    }

    @Test
    fun `separa la marca de la concentracion`() {
        val (brand, strength) = MedicationNameKey.brandAndStrength(
            "RELVAR ELLIPTA 92/22MCG"
        )

        assertEquals("RELVAR ELLIPTA", brand)
        assertEquals("92/22MCG", strength)
    }

    @Test
    fun `normaliza espacios entre numero y unidad`() {
        assertTrue(
            MedicationNameKey.matches(
                "ENALAPRIL 5 MG",
                "ENALAPRIL 5MG"
            )
        )
    }

    @Test
    fun `equipara la pulverizacion del pdf con la pulsion del prospecto`() {
        assertTrue(
            MedicationNameKey.matches(
                "RYALTRIS 25/600MCG/PULSACION",
                "Ryaltrís 25/600 µg/pulsión"
            )
        )
    }

    @Test
    fun `ignora el sufijo efg del nombre`() {
        assertTrue(
            MedicationNameKey.matches(
                "ATORVASTATINA 10 MG EFG",
                "Atorvastatina 10 mg"
            )
        )
    }

    @Test
    fun `normaliza guiones y puntuacion del nombre`() {
        assertTrue(
            MedicationNameKey.matches(
                "ENALAPRIL-5-MG",
                "enalapril 5 mg"
            )
        )
    }

    @Test
    fun `no confunde miligramos con microgramos`() {
        assertFalse(
            MedicationNameKey.matches(
                "ENALAPRIL 5MG",
                "ENALAPRIL 5MCG"
            )
        )
    }
}
