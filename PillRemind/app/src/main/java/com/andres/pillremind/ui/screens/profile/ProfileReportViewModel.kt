package com.andres.pillremind.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.pdf.AdherencePdfFileStore
import com.andres.pillremind.data.pdf.AdherencePdfGenerator
import com.andres.pillremind.data.pdf.AdherencePdfLabels
import com.andres.pillremind.data.pdf.AdherencePdfReport
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.domain.model.dateRange
import com.andres.pillremind.domain.model.toStatisticsReport
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Estados de la exportación del informe de adherencia del perfil.
 */
sealed interface ProfileReportExportState {

    /** No hay ninguna exportación en curso ni avisos pendientes. */
    data object Idle : ProfileReportExportState

    /** Se está generando el PDF. */
    data object Working : ProfileReportExportState

    /** El informe se guardó correctamente. */
    data object Exported : ProfileReportExportState

    /** La generación o la escritura del archivo falló. */
    data object Error : ProfileReportExportState
}

/**
 * ViewModel de la exportación del informe de adherencia.
 *
 * Es independiente de [ProfileViewModel] para no mezclar el CRUD del perfil
 * con la generación de archivos. Calcula el informe con el mismo rango y los
 * mismos criterios que la pantalla de estadísticas.
 *
 * @param intakeRepository Repositorio de tomas.
 * @param profileRepository Repositorio de perfiles.
 * @param sessionManager Gestor de sesión con el perfil activo.
 * @param adherencePdfGenerator Generador del documento.
 * @param adherencePdfFileStore Almacén del archivo elegido con SAF.
 */
@HiltViewModel
class ProfileReportViewModel @Inject constructor(
    private val intakeRepository: IntakeRepository,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    private val adherencePdfGenerator: AdherencePdfGenerator,
    private val adherencePdfFileStore: AdherencePdfFileStore
) : ViewModel() {

    private val _period = MutableStateFlow(StatisticsPeriod.THIRTY)

    /**
     * Periodo seleccionado para el informe.
     *
     * El valor inicial es de treinta días porque es el rango habitual de un
     * informe para la consulta.
     */
    val period: StateFlow<StatisticsPeriod> = _period.asStateFlow()

    private val _exportState = MutableStateFlow<ProfileReportExportState>(
        ProfileReportExportState.Idle
    )

    /** Estado de la exportación. */
    val exportState: StateFlow<ProfileReportExportState> = _exportState.asStateFlow()

    /**
     * Cambia el periodo del informe.
     *
     * @param selected Periodo elegido en el diálogo.
     */
    fun selectPeriod(selected: StatisticsPeriod) {
        _period.value = selected
    }

    /**
     * Nombre sugerido para el archivo del informe.
     */
    fun suggestedExportFileName(): String =
        adherencePdfFileStore.suggestedFileName(LocalDate.now())

    /**
     * Genera el informe de adherencia y lo escribe en el documento elegido.
     *
     * La consulta, el cálculo y la escritura se hacen fuera del hilo principal.
     * Si algo falla, se intenta borrar el archivo parcial.
     *
     * @param uri Documento de destino elegido con Storage Access Framework.
     * @param labels Textos del documento resueltos desde la UI.
     * @param now Momento de referencia para el informe y el rango.
     */
    fun exportPdf(
        uri: Uri,
        labels: AdherencePdfLabels,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        if (_exportState.value is ProfileReportExportState.Working) return

        val selectedPeriod = _period.value
        _exportState.value = ProfileReportExportState.Working

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val profileId = sessionManager.activeProfileId.first()
                        ?: error("No hay un perfil activo")
                    val profileName = profileRepository.getProfileByIdOnce(profileId)?.name
                        ?: labels.unknownProfile
                    val range = selectedPeriod.dateRange(now.toLocalDate())
                    val intakes = intakeRepository
                        .observeStatisticsIntakes(
                            profileId = profileId,
                            from = range.from,
                            to = range.to
                        )
                        .first()
                    val report = intakes.toStatisticsReport(
                        now = now,
                        chartDays = selectedPeriod.days.toInt()
                    )

                    adherencePdfFileStore.openOutput(uri).use { output ->
                        adherencePdfGenerator.write(
                            output = output,
                            report = AdherencePdfReport(
                                profileName = profileName,
                                period = selectedPeriod,
                                reference = now,
                                report = report
                            ),
                            labels = labels
                        )
                    }
                }
                _exportState.value = ProfileReportExportState.Exported
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                adherencePdfFileStore.delete(uri)
                _exportState.value = ProfileReportExportState.Error
            }
        }
    }

    /**
     * Marca como consumido el estado de la exportación para no repetir avisos.
     */
    fun consumeExportState() {
        if (_exportState.value !is ProfileReportExportState.Working) {
            _exportState.value = ProfileReportExportState.Idle
        }
    }
}
