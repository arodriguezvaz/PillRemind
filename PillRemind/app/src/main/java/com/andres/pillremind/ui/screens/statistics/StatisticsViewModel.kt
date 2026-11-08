package com.andres.pillremind.ui.screens.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.StatisticsPeriod
import com.andres.pillremind.domain.model.StatisticsReport
import com.andres.pillremind.domain.model.toStatisticsReport
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
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
    sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicationId: Int? = savedStateHandle
        .get<Int>("medicationId")
        ?.takeIf { it > 0 }

    private val _period = MutableStateFlow(StatisticsPeriod.SEVEN)

    /** Periodo seleccionado en la pantalla. */
    val period: StateFlow<StatisticsPeriod> = _period.asStateFlow()

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
