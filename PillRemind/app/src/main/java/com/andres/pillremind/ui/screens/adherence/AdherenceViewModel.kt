package com.andres.pillremind.ui.screens.adherence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.AdherencePeriod
import com.andres.pillremind.domain.model.AdherenceSummary
import com.andres.pillremind.domain.model.MedicationAdherence
import com.andres.pillremind.domain.model.toAdherenceSummary
import com.andres.pillremind.domain.repository.IntakeRepository
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * ViewModel de la pantalla de adherencia.
 *
 * Calcula, para el perfil activo, cuántas tomas se registraron, cuántas se
 * omitieron y cuántas siguen pendientes en el periodo elegido.
 *
 * @param intakeRepository Repositorio de tomas.
 * @param sessionManager Gestor de sesión con el perfil activo.
 */
@HiltViewModel
class AdherenceViewModel @Inject constructor(
    private val intakeRepository: IntakeRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val _period = MutableStateFlow(AdherencePeriod.THIRTY)

    /** Periodo seleccionado en la pantalla. */
    val period: StateFlow<AdherencePeriod> = _period.asStateFlow()

    private val now = MutableStateFlow(LocalDateTime.now())

    /** Actualiza el momento de referencia para recalcular tomas vencidas. */
    fun refreshNow() {
        now.value = LocalDateTime.now()
    }

    /**
     * Cambia el periodo y vuelve a calcular las estadísticas.
     */
    fun selectPeriod(selected: AdherencePeriod) {
        _period.value = selected
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val medications: StateFlow<List<MedicationAdherence>> =
        combine(
            sessionManager.activeProfileId.filterNotNull(),
            _period,
            now
        ) { profileId, period, currentMoment ->
            AdherenceRequest(profileId, period, currentMoment)
        }.flatMapLatest { request ->
            val to = request.now.toLocalDate().atTime(LocalTime.MAX)
            val from = request.now.toLocalDate()
                .minusDays(request.period.days - 1)
                .atStartOfDay()

            intakeRepository.getAdherence(
                profileId = request.profileId,
                from = from,
                to = to,
                // Se da el mismo margen de 15 minutos que usa Home antes de
                // considerar una toma como omitida.
                now = request.now.minusMinutes(15)
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Resumen agregado de todos los medicamentos del periodo. */
    val summary: StateFlow<AdherenceSummary> = medications
        .map { it.toAdherenceSummary() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AdherenceSummary(0, 0, 0, null)
        )

    private data class AdherenceRequest(
        val profileId: Int,
        val period: AdherencePeriod,
        val now: LocalDateTime
    )
}
