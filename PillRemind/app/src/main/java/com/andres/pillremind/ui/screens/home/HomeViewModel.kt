package com.andres.pillremind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.notifications.ReminderNotifications
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ViewModel de la pantalla principal (`HomeScreen`).
 *
 * Expone el perfil activo y la lista de tomas del día. Mantiene un reloj propio
 * para que la pantalla cambie de día y los estados visuales caduquen aunque la
 * app lleve horas abierta.
 *
 * @param intakeRepository Repositorio de tomas.
 * @param sessionManager Gestor de sesión que expone el perfil activo.
 * @param reminderScheduler Coordina la generación de tomas y la programación de alarmas.
 * @param notifications Gestor de notificaciones de recordatorio.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val intakeRepository: IntakeRepository,
    private val sessionManager: SessionManager,
    private val reminderScheduler: ReminderScheduler,
    private val notifications: ReminderNotifications
) : ViewModel() {

    private val _now = MutableStateFlow(LocalDateTime.now())

    /** Hora actual observada por la pantalla. */
    val now: StateFlow<LocalDateTime> = _now.asStateFlow()

    private val today = MutableStateFlow(LocalDate.now())

    private val _remindersError = MutableStateFlow(false)

    /** Indica si la última regeneración de recordatorios falló. */
    val remindersError: StateFlow<Boolean> = _remindersError.asStateFlow()

    init {
        refreshReminders()
    }

    val profileId = sessionManager.activeProfileId

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayIntakes: StateFlow<List<IntakeWithMedication>> =
        combine(profileId.filterNotNull(), today) { id, date -> id to date }
            .flatMapLatest { (id, date) ->
                intakeRepository.getIntakesForToday(date, id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Actualiza la hora observada y cambia la fecha si se ha cruzado la
     * medianoche. La pantalla la llama periódicamente mientras está visible.
     */
    fun refreshClock() {
        val current = LocalDateTime.now()
        _now.value = current
        if (current.toLocalDate() != today.value) {
            today.value = current.toLocalDate()
        }
    }

    /**
     * Regenera la ventana de tomas y alarmas. Se reintenta desde la UI si falla.
     */
    fun refreshReminders() {
        viewModelScope.launch {
            _remindersError.value = !runCatching {
                reminderScheduler.generateWindow()
            }.isSuccess
        }
    }

    fun tomarIntake(intake: IntakeWithMedication) {
        viewModelScope.launch {
            val ahora = LocalDateTime.now()
            val nuevaTomadoHoy = if (intake.takenAt == null) ahora else null
            intakeRepository.markTaken(intake.intakeId, nuevaTomadoHoy)
            notifications.cancel(intake.intakeId)
        }
    }
}
