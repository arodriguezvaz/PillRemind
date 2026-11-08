package com.andres.pillremind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.model.StatisticsSummary
import com.andres.pillremind.domain.model.isPendingOrTakenLate
import com.andres.pillremind.domain.model.toStatisticsSummary
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.notifications.IntakeNotifications
import com.andres.pillremind.notifications.ReminderScheduler
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * ViewModel de la pantalla principal (`HomeScreen`).
 *
 * Expone el perfil activo y la lista de medicamentos del perfil usando flows
 * convertidos a `StateFlow` para su observación en la UI.
 *
 * @param profileRepository Repositorio para obtener datos de perfil.
 * @param intakeRepository Repositorio de tomas.
 * @param sessionManager Gestor de sesión que expone el perfil activo.
 * @param reminderScheduler Coordina la generación de tomas y la programación de alarmas.
 * @param intakeNotifications Cancela la notificación de las tomas marcadas desde Inicio.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val intakeRepository: IntakeRepository,
    private val sessionManager: SessionManager,
    private val reminderScheduler: ReminderScheduler,
    private val intakeNotifications: IntakeNotifications,
) : ViewModel() {
    init {
        viewModelScope.launch {
            reminderScheduler.generateWindow()
        }
    }

    val profileId = sessionManager.activeProfileId

    private val _now = MutableStateFlow(LocalDateTime.now())

    /** Momento de referencia para los estados visuales y el resumen semanal. */
    val now: StateFlow<LocalDateTime> = _now.asStateFlow()

    private val today = MutableStateFlow(_now.value.toLocalDate())

    /**
     * Actualiza el momento de referencia del resumen semanal.
     *
     * Se llama al volver a Inicio y desde el ticker de la pantalla para que las
     * tomas que han superado el margen de 15 minutos se cuenten como no
     * registradas y, si ha cambiado el día, se consulte el rango correcto.
     *
     * @param moment Nuevo momento de referencia.
     */
    fun refreshNow(moment: LocalDateTime = LocalDateTime.now()) {
        _now.value = moment
        if (today.value != moment.toLocalDate()) {
            today.value = moment.toLocalDate()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val profile: StateFlow<Profile?> =
        sessionManager.activeProfileId
            .filterNotNull()
            .flatMapLatest { id ->
                profileRepository.getProfileById(id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )


    @OptIn(ExperimentalCoroutinesApi::class)
    val todayIntakes: StateFlow<List<IntakeWithMedication>> =
        combine(profileId.filterNotNull(), today) { id, date -> id to date }
            .flatMapLatest { (id, date) ->
                intakeRepository.observeIntakesForDay(id, date)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Tomas de ayer que siguen pendientes o se registraron de madrugada.
     *
     * Se muestran al final de Inicio para poder marcarlas o deshacerlas aunque
     * el día natural ya haya cambiado.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val yesterdayIntakes: StateFlow<List<IntakeWithMedication>> =
        combine(profileId.filterNotNull(), today) { id, date ->
            id to date.minusDays(1)
        }
            .flatMapLatest { (id, date) ->
                intakeRepository.observeIntakesForDay(id, date)
            }
            .map { intakes -> intakes.filter { it.isPendingOrTakenLate() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Resumen de tomas de los últimos siete días para el banner de Inicio.
     *
     * Es null hasta que se recibe la primera consulta. La pantalla lo oculta
     * cuando no hay ninguna toma evaluada.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val weeklySummary: StateFlow<StatisticsSummary?> =
        combine(profileId.filterNotNull(), today) { id, date -> id to date }
            .flatMapLatest { (id, date) ->
                intakeRepository.observeStatisticsIntakes(
                    profileId = id,
                    from = date.minusDays(6).atStartOfDay(),
                    to = date.atTime(LocalTime.MAX)
                )
            }
            .combine(now) { intakes, moment ->
                intakes.toStatisticsSummary(moment)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    fun tomarIntake(intake: IntakeWithMedication) {
        viewModelScope.launch {
            val hoy = LocalDateTime.now()
            val nuevaTomadoHoy = if (intake.takenAt == null) hoy else null
            intakeRepository.markTaken(intake.intakeId, nuevaTomadoHoy)

            // Si la toma estaba pendiente, su recordatorio deja de ser válido.
            // Al deshacerla no hay nada que cancelar: la notificación ya se
            // eliminó cuando se marcó (desde aquí o desde la propia notificación).
            if (nuevaTomadoHoy != null) {
                intakeNotifications.cancel(intake.intakeId)
            }
        }
    }
}
