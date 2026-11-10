package com.andres.pillremind.ui.screens.medication

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.R
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.ocr.OcrEngine
import com.andres.pillremind.data.scan.BarcodeEngine
import com.andres.pillremind.domain.model.CnLookupResult
import com.andres.pillremind.domain.model.MedicationDraft
import com.andres.pillremind.domain.parsing.BoxFields
import com.andres.pillremind.domain.parsing.BoxTextParser
import com.andres.pillremind.domain.parsing.CnParser
import com.andres.pillremind.domain.parsing.MedicationDraftMapper
import com.andres.pillremind.domain.repository.MedicationLookupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pantalla del flujo de escaneo.
 */
enum class ScanStage {
    PHOTOS,
    ANALYZING,
    RESULT
}

/**
 * Estado de cada paso del análisis.
 */
enum class StepState {
    PENDING,
    RUNNING,
    DONE,
    FAILED,
    SKIPPED
}

/**
 * Resultado que se muestra como tarjeta breve bajo el formulario.
 */
enum class ScanNotice {
    NONE,
    CIMA_FOUND,
    CIMA_NOT_FOUND,
    CIMA_NOT_FOUND_EMPTY,
    CIMA_ERROR,
    CIMA_NOT_CONSULTED,
    CN_NOT_DETECTED,
    CODE_UNREADABLE,
    NOTHING
}

/**
 * Estado del escaneo de dos fotos.
 *
 * El CN y la respuesta de CIMA no se persisten: viven en este estado y se
 * descartan al cerrar el flujo. Los valores que el usuario acepta sí pasan al
 * formulario del medicamento y se guardan como campos normales.
 */
data class ScanUiState(
    val stage: ScanStage = ScanStage.PHOTOS,
    val boxPhotoUri: Uri? = null,
    val codePhotoUri: Uri? = null,
    val manualCn: String = "",
    val analyzing: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val draft: MedicationDraft? = null,
    val notice: ScanNotice = ScanNotice.NONE,
    val codeStep: StepState = StepState.PENDING,
    val boxStep: StepState = StepState.PENDING,
    val cimaStep: StepState = StepState.PENDING,
    val consentRequired: Boolean = false
) {
    /** CN que se pueden obtener del texto escrito a mano. */
    val manualCnCandidates: List<String>
        get() = buildList {
            if (manualCn.isBlank()) return@buildList
            CnParser.normalize(manualCn)?.let(::add)
            CnParser.fromAnyCode(manualCn)?.let(::add)
        }.distinct()

    /** El campo manual tiene texto pero no contiene un CN válido. */
    val manualCnInvalid: Boolean
        get() = manualCn.isNotBlank() && manualCnCandidates.isEmpty()

    /** Hay al menos una fuente válida que analizar. */
    val canAnalyze: Boolean
        get() = !manualCnInvalid &&
            (boxPhotoUri != null || codePhotoUri != null || manualCnCandidates.isNotEmpty())
}

/**
 * Orquesta el flujo: fotos de caja/código, OCR, códigos de barras y CIMA.
 */
@HiltViewModel
class MedicationScanViewModel @Inject constructor(
    private val ocrEngine: OcrEngine,
    private val barcodeEngine: BarcodeEngine,
    private val lookupRepository: MedicationLookupRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    private var analysisJob: Job? = null
    private var pendingCns: List<String> = emptyList()
    private var pendingBox: BoxFields? = null
    private var pendingBoxHasData = false

    fun onBoxPhoto(uri: Uri) {
        _state.update { it.copy(boxPhotoUri = uri, errorRes = null) }
    }

    fun onCodePhoto(uri: Uri) {
        _state.update { it.copy(codePhotoUri = uri, errorRes = null) }
    }

    fun onManualCnChange(value: String) {
        _state.update {
            it.copy(
                manualCn = value.filter(Char::isDigit).take(MAX_MANUAL_CN_INPUT),
                errorRes = null
            )
        }
    }

    /**
     * Analiza las fotos y, si hay algún CN, consulta CIMA.
     */
    fun analyze() {
        val current = _state.value
        if (!current.canAnalyze) {
            _state.update {
                it.copy(
                    errorRes = if (current.manualCnInvalid) {
                        R.string.scan_error_invalid_cn
                    } else {
                        R.string.scan_error_need_photo
                    }
                )
            }
            return
        }

        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    stage = ScanStage.ANALYZING,
                    analyzing = true,
                    errorRes = null,
                    draft = null,
                    notice = ScanNotice.NONE,
                    codeStep = StepState.PENDING,
                    boxStep = StepState.PENDING,
                    cimaStep = StepState.PENDING
                )
            }

            try {
                val candidates = LinkedHashSet<String>()
                candidates.addAll(current.manualCnCandidates)

                var boxFields: BoxFields? = null
                var boxHasData = false
                var codeAttempted = false
                var codeFound = false

                val codePhoto = current.codePhotoUri
                if (codePhoto != null) {
                    _state.update { it.copy(codeStep = StepState.RUNNING) }
                    codeAttempted = true
                    val codes = barcodeEngine.scan(codePhoto)
                    currentCoroutineContext().ensureActive()

                    val codeCns = codes
                        .mapNotNull { CnParser.fromAnyCode(it.raw) }
                        .toMutableList()
                    if (codeCns.isEmpty()) {
                        val codeText = ocrEngine.recognize(codePhoto)
                        currentCoroutineContext().ensureActive()
                        codeCns.addAll(codeText?.let(CnParser::extractCandidates).orEmpty())
                    }
                    codeCns.take(MAX_CNS_PER_SOURCE).forEach { candidates.add(it) }

                    codeFound = codeCns.isNotEmpty()
                    _state.update {
                        it.copy(
                            codeStep = if (codeFound) StepState.DONE else StepState.FAILED
                        )
                    }
                } else {
                    _state.update { it.copy(codeStep = StepState.SKIPPED) }
                }

                val boxPhoto = current.boxPhotoUri
                if (boxPhoto != null) {
                    _state.update { it.copy(boxStep = StepState.RUNNING) }
                    val boxText = ocrEngine.recognize(boxPhoto)
                    currentCoroutineContext().ensureActive()
                    boxFields = boxText?.let(BoxTextParser::parse)
                    boxHasData = boxFields?.let { fields ->
                        fields.name != null ||
                            fields.strengthDoseText != null ||
                            fields.intakeDoseText != null
                    } == true

                    val boxCns = boxText
                        ?.let(CnParser::extractCandidates)
                        .orEmpty()
                        .take(MAX_CNS_PER_SOURCE)
                    candidates.addAll(boxCns)
                    val boxRead = boxHasData || boxCns.isNotEmpty()
                    _state.update {
                        it.copy(boxStep = if (boxRead) StepState.DONE else StepState.FAILED)
                    }
                } else {
                    _state.update { it.copy(boxStep = StepState.SKIPPED) }
                }

                val finalCns = candidates.take(MAX_TOTAL_CNS).toList()
                if (finalCns.isEmpty()) {
                    finishWithBox(
                        boxFields = boxFields,
                        boxHasData = boxHasData,
                        notice = when {
                            !boxHasData -> ScanNotice.NOTHING
                            codeAttempted || current.manualCn.isNotBlank() ->
                                ScanNotice.CODE_UNREADABLE

                            else -> ScanNotice.CN_NOT_DETECTED
                        },
                        cimaStep = StepState.SKIPPED
                    )
                    return@launch
                }

                if (!sessionManager.cimaConsent.first()) {
                    currentCoroutineContext().ensureActive()
                    pendingCns = finalCns
                    pendingBox = boxFields
                    pendingBoxHasData = boxHasData
                    _state.update {
                        it.copy(
                            analyzing = false,
                            cimaStep = StepState.PENDING,
                            consentRequired = true
                        )
                    }
                    return@launch
                }

                currentCoroutineContext().ensureActive()
                lookupAndFinish(
                    cns = finalCns,
                    boxFields = boxFields,
                    boxHasData = boxHasData
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        stage = ScanStage.PHOTOS,
                        analyzing = false,
                        errorRes = R.string.scan_error_generic,
                        codeStep = StepState.PENDING,
                        boxStep = StepState.PENDING,
                        cimaStep = StepState.PENDING
                    )
                }
            }
        }
    }

    /**
     * Prueba los CN en orden y se queda con el primero que exista en CIMA.
     */
    private suspend fun lookupAndFinish(
        cns: List<String>,
        boxFields: BoxFields?,
        boxHasData: Boolean
    ) {
        _state.update { it.copy(cimaStep = StepState.RUNNING) }

        for (cn in cns) {
            currentCoroutineContext().ensureActive()
            when (val result = lookupRepository.findByCn(cn)) {
                is CnLookupResult.Found -> {
                    currentCoroutineContext().ensureActive()
                    _state.update {
                        it.copy(
                            analyzing = false,
                            stage = ScanStage.RESULT,
                            cimaStep = StepState.DONE,
                            draft = MedicationDraftMapper.fromCima(result.medication),
                            notice = ScanNotice.CIMA_FOUND
                        )
                    }
                    return
                }

                is CnLookupResult.NotFound -> Unit

                is CnLookupResult.Error -> {
                    currentCoroutineContext().ensureActive()
                    finishWithBox(
                        boxFields = boxFields,
                        boxHasData = boxHasData,
                        notice = ScanNotice.CIMA_ERROR,
                        cimaStep = StepState.FAILED
                    )
                    return
                }
            }
        }

        currentCoroutineContext().ensureActive()
        finishWithBox(
            boxFields = boxFields,
            boxHasData = boxHasData,
            notice = if (boxHasData) {
                ScanNotice.CIMA_NOT_FOUND
            } else {
                ScanNotice.CIMA_NOT_FOUND_EMPTY
            },
            cimaStep = StepState.FAILED
        )
    }

    private fun finishWithBox(
        boxFields: BoxFields?,
        boxHasData: Boolean,
        notice: ScanNotice,
        cimaStep: StepState
    ) {
        val draft = if (boxHasData && boxFields != null) {
            MedicationDraftMapper.fromOcr(boxFields, cn = null)
        } else {
            null
        }
        _state.update {
            it.copy(
                analyzing = false,
                stage = ScanStage.RESULT,
                draft = draft,
                notice = notice,
                cimaStep = cimaStep
            )
        }
    }

    /**
     * Vuelve a las fotos conservando las imágenes y el CN escrito.
     */
    fun backToPhotos() {
        analysisJob?.cancel()
        analysisJob = null
        clearPending()
        _state.update {
            it.copy(
                stage = ScanStage.PHOTOS,
                analyzing = false,
                errorRes = null,
                draft = null,
                notice = ScanNotice.NONE,
                codeStep = StepState.PENDING,
                boxStep = StepState.PENDING,
                cimaStep = StepState.PENDING,
                consentRequired = false
            )
        }
    }

    fun reset() {
        analysisJob?.cancel()
        analysisJob = null
        clearPending()
        _state.value = ScanUiState()
    }

    /**
     * Acepta la consulta a CIMA y continúa con los CN pendientes.
     *
     * El diálogo se oculta de forma síncrona para que una segunda pulsación no
     * lance otra consulta.
     */
    fun grantConsent() {
        if (pendingCns.isEmpty()) return

        val cns = pendingCns
        val box = pendingBox
        val hasBox = pendingBoxHasData
        clearPending()
        _state.update {
            it.copy(
                consentRequired = false,
                analyzing = true,
                errorRes = null
            )
        }

        analysisJob = viewModelScope.launch {
            runCatching { sessionManager.setCimaConsent(true) }
            currentCoroutineContext().ensureActive()
            lookupAndFinish(cns, box, hasBox)
        }
    }

    fun declineConsent() {
        val box = pendingBox
        val hasBox = pendingBoxHasData
        clearPending()
        _state.update { it.copy(consentRequired = false, analyzing = false) }

        finishWithBox(
            boxFields = box,
            boxHasData = hasBox,
            notice = ScanNotice.CIMA_NOT_CONSULTED,
            cimaStep = StepState.SKIPPED
        )
    }

    private fun clearPending() {
        pendingCns = emptyList()
        pendingBox = null
        pendingBoxHasData = false
    }

    private companion object {
        const val MAX_MANUAL_CN_INPUT = 20
        const val MAX_CNS_PER_SOURCE = 3
        const val MAX_TOTAL_CNS = 5
    }
}
