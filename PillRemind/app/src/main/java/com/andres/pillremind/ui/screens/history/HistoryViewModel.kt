package com.andres.pillremind.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.StatisticsIntake
import com.andres.pillremind.domain.model.StatisticsStatus
import com.andres.pillremind.domain.model.status
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Medicamento disponible en el filtro del historial.
 *
 * @property id Identificador del medicamento.
 * @property name Nombre actual del medicamento.
 * @property isArchived Indica si el medicamento está archivado.
 */
data class HistoryMedicationOption(
    val id: Int,
    val name: String,
    val isArchived: Boolean,
)

/**
 * Toma del historial con su estado calculado.
 *
 * @property intake Datos de la toma.
 * @property status Estado en el momento de referencia del ViewModel.
 */
data class HistoryEntry(
    val intake: StatisticsIntake,
    val status: StatisticsStatus,
)

/**
 * Estado de la pantalla de historial.
 *
 * @property isLoading Indica que todavía no se ha recibido la primera consulta.
 * @property hasMedications Indica si el perfil tiene algún medicamento, aunque
 * esté archivado.
 * @property entries Tomas que cumplen los filtros, de más reciente a más antigua.
 * @property medications Medicamentos del perfil para el desplegable de filtros.
 * @property totalInRange Número de tomas del rango antes de aplicar los filtros.
 * @property now Momento usado para calcular los estados de las tomas.
 */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val hasMedications: Boolean = false,
    val entries: List<HistoryEntry> = emptyList(),
    val medications: List<HistoryMedicationOption> = emptyList(),
    val totalInRange: Int = 0,
    val now: LocalDateTime = LocalDateTime.now(),
)

/**
 * ViewModel de la pantalla de historial.
 *
 * Observa el perfil activo, el rango de fechas y los filtros para construir la
 * lista de tomas. El rango se consulta en Room y el resto de filtros se aplican
 * en memoria, porque el estado de cada toma depende del momento actual.
 *
 * @param intakeRepository Repositorio de tomas.
 * @param medicationRepository Repositorio de medicamentos, usado para el
 * desplegable de filtros y para distinguir un perfil sin medicamentos.
 * @param sessionManager Gestor de sesión con el perfil activo.
 * @param savedStateHandle Estado de navegación con el medicamento preseleccionado,
 * si se ha abierto la pantalla desde su ficha.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val intakeRepository: IntakeRepository,
    private val medicationRepository: MedicationRepository,
    sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialMedicationId: Int? = savedStateHandle
        .get<Int>("medicationId")
        ?.takeIf { it > 0 }

    private val _filters = MutableStateFlow(
        HistoryFilters(medicationId = initialMedicationId)
    )

    /** Filtros activos en la pantalla. */
    val filters: StateFlow<HistoryFilters> = _filters.asStateFlow()

    private val now = MutableStateFlow(LocalDateTime.now())
    private val today = MutableStateFlow(LocalDate.now())

    /**
     * Actualiza el momento de referencia para recalcular los estados.
     *
     * Si cambia el día, también se actualiza el rango por defecto.
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
     * Actualiza el texto de búsqueda.
     *
     * @param query Texto escrito por el usuario.
     */
    fun onSearchQueryChange(query: String) {
        _filters.update { it.copy(query = query) }
    }

    /**
     * Aplica un rango de fechas personalizado.
     *
     * Si la fecha inicial es posterior a la final, no se aplica.
     *
     * @param from Primer día incluido.
     * @param to Último día incluido.
     */
    fun applyCustomRange(from: LocalDate, to: LocalDate) {
        if (from.isAfter(to)) return
        _filters.update { it.copy(dateFilter = HistoryDateFilter.Custom(from, to)) }
    }

    /**
     * Selecciona el estado por el que filtrar.
     *
     * @param status Estado elegido.
     */
    fun selectStatus(status: HistoryStatusFilter) {
        _filters.update { it.copy(status = status) }
    }

    /**
     * Selecciona un medicamento para el filtro.
     *
     * Si el medicamento está archivado, activa también la inclusión de
     * archivados para que el filtro no deje la lista vacía.
     *
     * @param option Medicamento elegido, o `null` para todos.
     */
    fun selectMedication(option: HistoryMedicationOption?) {
        _filters.update { current ->
            current.copy(
                medicationId = option?.id,
                includeArchived = current.includeArchived || option?.isArchived == true,
            )
        }
    }

    /**
     * Activa o desactiva la inclusión de medicamentos archivados.
     *
     * Si se desactiva y había un medicamento archivado seleccionado, se limpia
     * esa selección.
     *
     * @param include Indica si se incluyen los archivados.
     */
    fun setIncludeArchived(include: Boolean) {
        _filters.update { current ->
            val selectedIsArchived = current.medicationId?.let { id ->
                uiState.value.medications.firstOrNull { it.id == id }?.isArchived == true
            } == true
            current.copy(
                includeArchived = include,
                medicationId = if (!include && selectedIsArchived) null else current.medicationId,
            )
        }
    }

    /**
     * Restablece todos los filtros al estado inicial.
     */
    fun clearFilters() {
        _filters.value = HistoryFilters()
    }

    private val profileId = sessionManager.activeProfileId.filterNotNull()

    private val medications = profileId.flatMapLatest { id ->
        medicationRepository.getAllMedications(id)
    }

    private val intakes = combine(
        profileId,
        _filters.map { it.dateFilter }.distinctUntilChanged(),
        today,
    ) { id, dateFilter, currentDate ->
        HistoryRequest(
            profileId = id,
            from = dateFilter.rangeStart(currentDate),
            to = dateFilter.rangeEnd(currentDate),
        )
    }.flatMapLatest { request ->
        intakeRepository.observeHistoryIntakes(
            profileId = request.profileId,
            from = request.from,
            to = request.to,
        )
    }

    /** Estado que consume la pantalla. */
    val uiState: StateFlow<HistoryUiState> = combine(
        intakes,
        medications,
        _filters,
        now,
    ) { intakes, medications, filters, moment ->
        val query = filters.query.trim()
        val currentMedicationNames = medications.associate { it.id to it.name }
        val entries = intakes
            .asSequence()
            .filter { filters.includeArchived || !it.isArchived }
            .filter { filters.medicationId == null || it.medicationId == filters.medicationId }
            .filter { intake ->
                query.isBlank() ||
                    intake.medicationName.contains(query, ignoreCase = true) ||
                    currentMedicationNames[intake.medicationId]
                        ?.contains(query, ignoreCase = true) == true
            }
            .map { intake -> HistoryEntry(intake, intake.status(moment)) }
            .filter { entry ->
                filters.status == HistoryStatusFilter.ALL ||
                    entry.status == filters.status.toStatisticsStatus()
            }
            .sortedByDescending { it.intake.scheduledAt }
            .toList()

        HistoryUiState(
            isLoading = false,
            hasMedications = medications.isNotEmpty(),
            entries = entries,
            medications = medications
                .map { HistoryMedicationOption(it.id, it.name, it.deletedAt != null) }
                .sortedBy { it.name.lowercase() },
            totalInRange = intakes.size,
            now = moment,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    /**
     * Datos de una consulta concreta.
     */
    private data class HistoryRequest(
        val profileId: Int,
        val from: LocalDateTime,
        val to: LocalDateTime,
    )

    /**
     * Inicio del rango de fechas.
     */
    private fun HistoryDateFilter.rangeStart(today: LocalDate): LocalDateTime = when (this) {
        HistoryDateFilter.Default ->
            today.minusDays(DEFAULT_HISTORY_DAYS - 1).atStartOfDay()

        is HistoryDateFilter.Custom -> from.atStartOfDay()
    }

    /**
     * Fin del rango de fechas.
     */
    private fun HistoryDateFilter.rangeEnd(today: LocalDate): LocalDateTime = when (this) {
        HistoryDateFilter.Default -> today.atTime(LocalTime.MAX)
        is HistoryDateFilter.Custom -> to.atTime(LocalTime.MAX)
    }
}
