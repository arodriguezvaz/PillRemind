package com.andres.pillremind.ui.screens.statistics

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.data.pdf.AdherencePdfFileStore
import com.andres.pillremind.data.pdf.AdherencePdfGenerator
import com.andres.pillremind.data.pdf.AdherencePdfLabels
import com.andres.pillremind.data.pdf.AdherencePdfReport
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.domain.model.StatisticsReport
import com.andres.pillremind.domain.model.toStatisticsReport
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Estado de la pantalla de estadísticas.
 *
 * @property isLoading Indica que todavía no se ha recibido la primera consulta.
 * @property hasMedications Indica si el perfil tiene medicamentos, aunque estén archivados.
 * @property medicationId Medicamento por el que se filtra el informe, o `null`
 * cuando se muestran las estadísticas de todo el perfil.
 * @property report Informe calculado con las tomas del periodo seleccionado.
 * @property now Momento usado para calcular los estados de las tomas.
 */
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val hasMedications: Boolean = false,
    val medicationId: Int? = null,
    val report: StatisticsReport = StatisticsReport(),
    val now: LocalDateTime = LocalDateTime.now()
)

/**
 * Estados de la exportación del informe de adherencia.
 */
sealed interface StatisticsExportState {

    /** No hay ninguna exportación en curso ni avisos pendientes. */
    data object Idle : StatisticsExportState

    /** Se está generando el PDF. */
    data object Working : StatisticsExportState

    /** El informe se guardó correctamente. */
    data object Exported : StatisticsExportState

    /** La generación o la escritura del archivo falló. */
    data object Error : StatisticsExportState
}

/**
 * ViewModel de la pantalla de estadísticas.
 *
 * Observa el perfil activo, el periodo seleccionado y las tomas del rango para
 * calcular el resumen, el gráfico y el historial. El cálculo se delega en el
 * dominio para poder probarlo sin Android.
 *
 * @param intakeRepository Repositorio de tomas.
 * @param medicationRepository Repositorio de medicamentos, usado para saber si
 * el perfil tiene algún medicamento aunque no haya tomas en el periodo.
 * @param sessionManager Gestor de sesión con el perfil activo.
 * @param savedStateHandle Estado de navegación con el medicamento a filtrar, si
 * se ha abierto la pantalla desde su ficha.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val intakeRepository: IntakeRepository,
    private val medicationRepository: MedicationRepository,
    private val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository,
    private val adherencePdfGenerator: AdherencePdfGenerator,
    private val adherencePdfFileStore: AdherencePdfFileStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicationId: Int? = savedStateHandle
        .get<Int>("medicationId")
        ?.takeIf { it > 0 }

    private val _period = MutableStateFlow(StatisticsPeriod.SEVEN)

    /** Periodo seleccionado en la pantalla. */
    val period: StateFlow<StatisticsPeriod> = _period.asStateFlow()

    private val _exportState = MutableStateFlow<StatisticsExportState>(
        StatisticsExportState.Idle
    )

    /** Estado de la exportación del informe de adherencia. */
    val exportState: StateFlow<StatisticsExportState> = _exportState.asStateFlow()

    private val now = MutableStateFlow(LocalDateTime.now())
    private val today = MutableStateFlow(LocalDate.now())

    /**
     * Actualiza el momento de referencia para recalcular las tomas vencidas.
     *
     * Si cambia el día, también se actualiza la fecha del rango para no seguir
     * consultando el periodo anterior.
     *
     * @param moment Nuevo momento de referencia.
     */
    fun refreshNow(moment: LocalDateTime = LocalDateTime.now()) {
        now.value = moment
        if (today.value != moment.toLocalDate()) {
            today.value = moment.toLocalDate()
        }
    }

    /**
     * Cambia el periodo y vuelve a consultar las tomas.
     *
     * @param selected Periodo elegido por el usuario.
     */
    fun selectPeriod(selected: StatisticsPeriod) {
        _period.value = selected
    }

    /**
     * Nombre sugerido para el archivo del informe.
     */
    fun suggestedExportFileName(): String =
        adherencePdfFileStore.suggestedFileName(uiState.value.now.toLocalDate())

    /**
     * Genera el informe de adherencia y lo escribe en el documento elegido.
     *
     * La escritura se hace fuera del hilo principal y, si falla, se intenta
     * borrar el archivo parcial.
     *
     * @param uri Documento de destino elegido con Storage Access Framework.
     * @param labels Textos del documento resueltos desde la UI.
     */
    fun exportPdf(uri: Uri, labels: AdherencePdfLabels) {
        if (_exportState.value is StatisticsExportState.Working) return

        val state = uiState.value
        val period = _period.value
        _exportState.value = StatisticsExportState.Working

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val profileId = sessionManager.activeProfileId.first()
                        ?: error("No hay un perfil activo")
                    val profileName = profileRepository.getProfileByIdOnce(profileId)?.name
                        ?: labels.unknownProfile

                    adherencePdfFileStore.openOutput(uri).use { output ->
                        adherencePdfGenerator.write(
                            output = output,
                            report = AdherencePdfReport(
                                profileName = profileName,
                                period = period,
                                reference = state.now,
                                report = state.report
                            ),
                            labels = labels
                        )
                    }
                }
                _exportState.value = StatisticsExportState.Exported
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                adherencePdfFileStore.delete(uri)
                _exportState.value = StatisticsExportState.Error
            }
        }
    }

    /**
     * Marca como consumido el estado de la exportación para no repetir avisos.
     */
    fun consumeExportState() {
        if (_exportState.value !is StatisticsExportState.Working) {
            _exportState.value = StatisticsExportState.Idle
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticsUiState> = combine(
        sessionManager.activeProfileId.filterNotNull(),
        _period,
        today
    ) { profileId, selectedPeriod, currentDate ->
        val to = currentDate.atTime(LocalTime.MAX)
        val from = currentDate.minusDays(selectedPeriod.days - 1).atStartOfDay()
        StatisticsRequest(profileId, selectedPeriod, from, to)
    }.flatMapLatest { request ->
        combine(
            intakeRepository.observeStatisticsIntakes(
                profileId = request.profileId,
                from = request.from,
                to = request.to
            ),
            medicationRepository.hasMedications(request.profileId)
        ) { intakes, hasMedications ->
            Triple(intakes, hasMedications, request.period)
        }
    }.combine(now) { (intakes, hasMedications, selectedPeriod), moment ->
        val reportIntakes = medicationId?.let { id ->
            intakes.filter { it.medicationId == id }
        } ?: intakes

        StatisticsUiState(
            isLoading = false,
            hasMedications = medicationId != null || hasMedications,
            medicationId = medicationId,
            report = reportIntakes.toStatisticsReport(
                now = moment,
                chartDays = selectedPeriod.days.toInt()
            ),
            now = moment
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState()
    )

    /**
     * Datos de una consulta concreta.
     */
    private data class StatisticsRequest(
        val profileId: Int,
        val period: StatisticsPeriod,
        val from: LocalDateTime,
        val to: LocalDateTime
    )
}
