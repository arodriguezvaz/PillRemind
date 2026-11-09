package com.andres.pillremind.data.repository

import com.andres.pillremind.data.remote.CimaDataSource
import com.andres.pillremind.domain.model.CnLookupResult
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del contrato de errores de la consulta a CIMA.
 */
class MedicationLookupRepositoryImplTest {

    @Test
    fun `la cancelacion se propaga y no se convierte en error`() {
        val dataSource = mockk<CimaDataSource>()
        coEvery { dataSource.findByCn(any()) } throws CancellationException()
        val repository = MedicationLookupRepositoryImpl(dataSource)

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.findByCn("663526") }
        }
    }

    @Test
    fun `un fallo de red se mapea a Error`() = runBlocking {
        val dataSource = mockk<CimaDataSource>()
        coEvery { dataSource.findByCn(any()) } throws IOException("sin red")
        val repository = MedicationLookupRepositoryImpl(dataSource)

        val result = repository.findByCn("663526")

        assertTrue(result is CnLookupResult.Error)
    }
}
