package com.andres.pillremind.data.remote

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas del mapeo de la respuesta de CIMA, sin red.
 */
class CimaDataSourceTest {

    private val dataSource = CimaDataSource()

    private val body = """
        {
          "nregistro": "82921",
          "nombre": "ESOMEPRAZOL CINFA 20 MG CAPSULAS DURAS GASTRORRESISTENTES EFG",
          "dosis": "20 mg",
          "formaFarmaceutica": {
            "id": 13,
            "codigo": "CAPSULA",
            "nombre": "CÁPSULA DURA GASTRORRESISTENTE"
          },
          "presentaciones": [
            {
              "cn": "720932",
              "nombre": "ESOMEPRAZOL CINFA 20 MG ..., 28 cápsulas (Blister)"
            },
            {
              "cn": "720933",
              "nombre": "ESOMEPRAZOL CINFA 20 MG ..., 56 cápsulas (Blister)"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `mapea el medicamento y la presentacion del CN`() {
        val result = dataSource.parse(body, "720932")

        assertNotNull(result)
        assertEquals("720932", result!!.cn)
        assertEquals(
            "ESOMEPRAZOL CINFA 20 MG CAPSULAS DURAS GASTRORRESISTENTES EFG",
            result.name
        )
        assertEquals("20 mg", result.dosis)
        assertEquals("CÁPSULA DURA GASTRORRESISTENTE", result.form)
        assertEquals(
            "ESOMEPRAZOL CINFA 20 MG ..., 28 cápsulas (Blister)",
            result.presentation
        )
        assertEquals(LocalDate.now(), result.retrievedAt)
    }

    @Test
    fun `si no hay presentacion coincidente usa el nombre`() {
        val result = dataSource.parse(body, "999999")

        assertEquals(
            "ESOMEPRAZOL CINFA 20 MG CAPSULAS DURAS GASTRORRESISTENTES EFG",
            result?.presentation
        )
    }

    @Test
    fun `un json invalido devuelve null`() {
        assertNull(dataSource.parse("no es json", "720932"))
    }

    @Test
    fun `un medicamento sin nombre devuelve null`() {
        assertNull(dataSource.parse("""{"dosis":"20 mg"}""", "720932"))
    }
}
