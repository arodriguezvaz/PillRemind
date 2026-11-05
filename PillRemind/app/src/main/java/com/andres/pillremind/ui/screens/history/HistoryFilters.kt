package com.andres.pillremind.ui.screens.history

import com.andres.pillremind.domain.model.StatisticsStatus
import java.time.LocalDate

/**
 * Periodos rápidos disponibles en el historial.
 *
 * @property days Número de días incluidos, contando hoy, o `null` para todo
 * el historial.
 */
enum class HistoryPeriod(val days: Long?) {
    SEVEN(7),
    THIRTY(30),
    NINETY(90),
    ALL(null)
}

/**
 * Filtro por estado de una toma en el historial.
 */
enum class HistoryStatusFilter {
    ALL,
    TAKEN,
    MISSED,
    PENDING
}

/**
 * Rango de fechas aplicado al historial.
 *
 * Puede ser un periodo rápido o un intervalo personalizado con ambas fechas
 * incluidas.
 */
sealed interface HistoryDateFilter {
    /**
     * Periodo rápido seleccionado con los chips.
     *
     * @param period Periodo elegido.
     */
    data class Preset(val period: HistoryPeriod) : HistoryDateFilter

    /**
     * Intervalo personalizado.
     *
     * @param from Primer día incluido.
     * @param to Último día incluido.
     */
    data class Custom(val from: LocalDate, val to: LocalDate) : HistoryDateFilter
}

/**
 * Filtros aplicados a la pantalla de historial.
 *
 * @property query Texto de búsqueda por nombre de medicamento.
 * @property dateFilter Rango de fechas activo.
 * @property status Estado de la toma por el que se filtra.
 * @property medicationId Medicamento seleccionado, o `null` para todos.
 * @property includeArchived Indica si se incluyen las tomas de medicamentos
 * archivados.
 */
data class HistoryFilters(
    val query: String = "",
    val dateFilter: HistoryDateFilter = HistoryDateFilter.Preset(HistoryPeriod.THIRTY),
    val status: HistoryStatusFilter = HistoryStatusFilter.ALL,
    val medicationId: Int? = null,
    val includeArchived: Boolean = true,
) {
    /**
     * Indica si hay algún filtro que reduzca la lista más allá del periodo.
     *
     * Se usa para elegir entre el estado vacío de «sin tomas en el periodo» y
     * el de «sin coincidencias».
     */
    val hasNarrowingFilters: Boolean
        get() = query.isNotBlank() ||
            status != HistoryStatusFilter.ALL ||
            medicationId != null ||
            !includeArchived

    /**
     * Número de filtros activos que se muestran en la tarjeta.
     */
    val activeFilterCount: Int
        get() = listOf(
            query.isNotBlank(),
            status != HistoryStatusFilter.ALL,
            medicationId != null,
            dateFilter is HistoryDateFilter.Custom,
            !includeArchived,
        ).count { it }
}

/**
 * Traduce el filtro de estado al estado del dominio, o `null` para todos.
 */
internal fun HistoryStatusFilter.toStatisticsStatus(): StatisticsStatus? = when (this) {
    HistoryStatusFilter.ALL -> null
    HistoryStatusFilter.TAKEN -> StatisticsStatus.TAKEN
    HistoryStatusFilter.MISSED -> StatisticsStatus.MISSED
    HistoryStatusFilter.PENDING -> StatisticsStatus.PENDING
}
