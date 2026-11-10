package com.andres.pillremind.ui.screens.medication

import android.net.Uri
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.ocr.OcrEngine
import com.andres.pillremind.data.scan.BarcodeEngine
import com.andres.pillremind.domain.parsing.OcrLine
import com.andres.pillremind.domain.parsing.OcrResult
import com.andres.pillremind.domain.repository.MedicationLookupRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de integración entre el OCR de la caja y la pantalla de revisión.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MedicationScanViewModelTest {

    private val ocrEngine = mockk<OcrEngine>()
    private val barcodeEngine = mockk<BarcodeEngine>()
    private val lookupRepository = mockk<MedicationLookupRepository>()
    private val sessionManager = mockk<SessionManager>()
    private val boxUri = mockk<Uri>()

    private lateinit var viewModel: MedicationScanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.cimaConsent } returns flowOf(false)
        viewModel = MedicationScanViewModel(
            ocrEngine = ocrEngine,
            barcodeEngine = barcodeEngine,
            lookupRepository = lookupRepository,
            sessionManager = sessionManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * El parser devuelve candidatos y presentación pero no un nombre seguro. El
     * ViewModel debe conservar el borrador para que la revisión pueda enseñar
     * los candidatos y la presentación.
     */
    @Test
    fun `los candidatos de nombre y la presentacion llegan a la revision`() = runTest {
        coEvery { ocrEngine.recognize(boxUri) } returns OcrResult(
            text = "ALFA\nBETA\n120 ml",
            lines = listOf(
                OcrLine("ALFA", 0.20f, 0.30f, 0.50f, 0.42f),
                OcrLine("BETA", 0.20f, 0.50f, 0.50f, 0.62f),
                OcrLine("120 ml", 0.04f, 0.70f, 0.20f, 0.74f)
            )
        )

        viewModel.onBoxPhoto(boxUri)
        viewModel.analyze()

        val state = viewModel.state.value
        val draft = state.draft
        assertEquals(ScanStage.RESULT, state.stage)
        assertNotNull(
            "la revisión debe conservar los candidatos y la presentación " +
                "(notice=${state.notice}, draft=$draft)",
            draft
        )
        if (draft == null) return@runTest
        assertFalse(draft.nameConfident)
        assertEquals(setOf("ALFA", "BETA"), draft.nameCandidates.toSet())
        assertEquals("120 ml", draft.presentation)
    }
}
