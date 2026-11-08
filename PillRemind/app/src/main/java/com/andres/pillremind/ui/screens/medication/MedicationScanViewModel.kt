package com.andres.pillremind.ui.screens.medication

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.R
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.ocr.OcrEngine
import com.andres.pillremind.domain.model.CnLookupResult
import com.andres.pillremind.domain.model.MedicationDraft
import com.andres.pillremind.domain.parsing.BoxTextParser
import com.andres.pillremind.domain.parsing.CnParser
import com.andres.pillremind.domain.parsing.MedicationDraftMapper
import com.andres.pillremind.domain.repository.MedicationLookupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pasos del escaneo.
 */
enum class ScanStage {
    CN_INPUT,
    BOX_INPUT,
    REVIEW
}

/**
 * Estado del flujo de escaneo de CN y caja.
 */
data class ScanUiState(
    val stage: ScanStage = ScanStage.CN_INPUT,
    val cnInput: String = "",
    val cnPhotoUri: Uri? = null,
    val boxPhotoUri: Uri? = null,
    val loading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val notFoundCn: String? = null,
    val draft: MedicationDraft? = null,
    val consentRequired: Boolean = false
)

/**
 * Orquesta el escaneo del CN, la consulta a CIMA y el OCR de la caja.
 *
 * El CN no se persiste: solo se usa para la consulta y se descarta al cerrar
 * el flujo.
 */
@HiltViewModel
class MedicationScanViewModel @Inject constructor(
    private val ocrEngine: OcrEngine,
    private val lookupRepository: MedicationLookupRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    private var pendingSearch = false

    fun onCnChange(value: String) {
        _state.update {
            it.copy(cnInput = value.filter(Char::isDigit).take(7), errorRes = null)
        }
    }

    fun onCnPhoto(uri: Uri) {
        _state.update { it.copy(cnPhotoUri = uri, errorRes = null) }
    }

    fun onBoxPhoto(uri: Uri) {
        _state.update { it.copy(boxPhotoUri = uri, errorRes = null) }
    }

    fun searchCn() {
        viewModelScope.launch {
            if (!sessionManager.cimaConsent.first()) {
                pendingSearch = true
                _state.update { it.copy(consentRequired = true, errorRes = null) }
                return@launch
            }
            doSearchCn()
        }
    }

    private suspend fun doSearchCn() {
        val current = _state.value
        val cn = CnParser.normalize(current.cnInput)
        if (cn != null) {
            lookup(cn)
            return
        }

        val photo = current.cnPhotoUri
        if (photo == null) {
            _state.update { it.copy(errorRes = R.string.scan_cn_error_empty) }
            return
        }

        _state.update { it.copy(loading = true, errorRes = null) }
        val text = ocrEngine.recognize(photo)
        if (text.isNullOrBlank()) {
            _state.update {
                it.copy(loading = false, errorRes = R.string.scan_cn_error_ocr)
            }
            return
        }

        val candidates = CnParser.extractCandidates(text)
        if (candidates.isEmpty()) {
            _state.update {
                it.copy(loading = false, errorRes = R.string.scan_cn_error_cn)
            }
            return
        }

        lookupCandidates(candidates)
    }

    private suspend fun lookup(cn: String) {
        _state.update { it.copy(loading = true, errorRes = null) }
        when (val result = lookupRepository.findByCn(cn)) {
            is CnLookupResult.Found -> showReview(result)
            is CnLookupResult.NotFound -> showNotFound(cn)
            is CnLookupResult.Error -> _state.update {
                it.copy(loading = false, errorRes = R.string.scan_cn_error_network)
            }
        }
    }

    private suspend fun lookupCandidates(candidates: List<String>) {
        for (cn in candidates) {
            when (val result = lookupRepository.findByCn(cn)) {
                is CnLookupResult.Found -> {
                    showReview(result)
                    return
                }

                is CnLookupResult.NotFound -> continue

                is CnLookupResult.Error -> {
                    _state.update {
                        it.copy(loading = false, errorRes = R.string.scan_cn_error_network)
                    }
                    return
                }
            }
        }
        showNotFound(candidates.first())
    }

    private fun showReview(result: CnLookupResult.Found) {
        _state.update {
            it.copy(
                loading = false,
                stage = ScanStage.REVIEW,
                draft = MedicationDraftMapper.fromCima(result.medication),
                notFoundCn = null,
                errorRes = null
            )
        }
    }

    private fun showNotFound(cn: String) {
        _state.update {
            it.copy(
                loading = false,
                stage = ScanStage.BOX_INPUT,
                notFoundCn = cn,
                errorRes = null
            )
        }
    }

    fun readBox() {
        val uri = _state.value.boxPhotoUri
        if (uri == null) {
            _state.update { it.copy(errorRes = R.string.scan_cn_box_empty) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, errorRes = null) }
            val text = ocrEngine.recognize(uri)
            if (text.isNullOrBlank()) {
                _state.update {
                    it.copy(loading = false, errorRes = R.string.scan_cn_error_ocr)
                }
                return@launch
            }

            val fields = BoxTextParser.parse(text)
            val draft = MedicationDraftMapper.fromOcr(fields, _state.value.notFoundCn)
            _state.update {
                it.copy(
                    loading = false,
                    stage = ScanStage.REVIEW,
                    draft = draft,
                    errorRes = null
                )
            }
        }
    }

    fun showBoxInput() {
        _state.update { it.copy(stage = ScanStage.BOX_INPUT, errorRes = null) }
    }

    fun backToCn() {
        _state.update { it.copy(stage = ScanStage.CN_INPUT, errorRes = null) }
    }

    fun reset() {
        pendingSearch = false
        _state.value = ScanUiState()
    }

    fun grantConsent() {
        viewModelScope.launch {
            sessionManager.setCimaConsent(true)
            _state.update { it.copy(consentRequired = false) }
            if (pendingSearch) {
                pendingSearch = false
                doSearchCn()
            }
        }
    }

    fun declineConsent() {
        pendingSearch = false
        _state.update {
            it.copy(
                consentRequired = false,
                stage = ScanStage.BOX_INPUT,
                errorRes = R.string.scan_cn_consent_disabled
            )
        }
    }
}
