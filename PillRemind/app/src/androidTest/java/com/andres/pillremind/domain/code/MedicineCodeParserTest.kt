package com.andres.pillremind.domain.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas del parser de códigos de envases.
 *
 * Se ejecutan en el dispositivo para no depender de la JVM de los tests
 * unitarios (Robolectric no es necesario aquí).
 */
@RunWith(AndroidJUnit4::class)
class MedicineCodeParserTest {

    @Test
    fun eanOssDevuelveElCn() {
        assertEquals("720932", MedicineCodeParser.fromEan13("8470007209324"))
    }

    @Test
    fun eanNoOssNoDevuelveCn() {
        assertNull(MedicineCodeParser.fromEan13("8412345678905"))
    }

    @Test
    fun eanConDigitoDeControlInvalidoSeRechaza() {
        assertFalse(MedicineCodeParser.isValidEan13("8470007209325"))
        assertNull(MedicineCodeParser.fromEan13("8470007209325"))
    }

    @Test
    fun eanConLetrasSeRechaza() {
        assertNull(MedicineCodeParser.fromEan13("84700072093AB"))
    }

    @Test
    fun gs1NtinDevuelveElCn() {
        assertEquals("720932", MedicineCodeParser.fromGs1("0108470007209324"))
    }

    @Test
    fun gs1NtinConNhrnDevuelveElCn() {
        assertEquals(
            "720932",
            MedicineCodeParser.fromGs1("01084700072093247127209324")
        )
    }

    @Test
    fun gs1GtinNormalConNhrnDevuelveElCn() {
        assertEquals(
            "720932",
            MedicineCodeParser.fromGs1("01084123456789057127209324")
        )
    }

    @Test
    fun gs1GtinNormalSinNhrnNoDevuelveCn() {
        assertNull(MedicineCodeParser.fromGs1("0108412345678905"))
    }

    @Test
    fun gs1SoloNhrnDevuelveElCn() {
        assertEquals("720932", MedicineCodeParser.fromGs1("7127209324"))
    }

    @Test
    fun gs1ConSeparadorFnc1DevuelveElCn() {
        assertEquals(
            "720932",
            MedicineCodeParser.fromGs1("0108470007209324\u001d17261231\u001d10LOTE123")
        )
    }

    @Test
    fun gs1NtinSinAi01DevuelveElCn() {
        assertEquals("720932", MedicineCodeParser.fromGs1("08470007209324"))
    }

    @Test
    fun gs1ConLoteQueContiene712NoConfunde() {
        assertNull(MedicineCodeParser.fromGs1("010841234567890510ABC7123456"))
    }

    @Test
    fun eanValidoTieneDigitoDeControlCorrecto() {
        assertTrue(MedicineCodeParser.isValidEan13("8470007209324"))
    }
}
