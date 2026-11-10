package com.andres.pillremind.ui.screens.home

import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.notifications.IntakeNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Reproduce la carrera de medianoche entre las dos listas de Inicio.
 *
 * Las listas de hoy y ayer se derivan de dos consultas Room independientes
 * sobre el mismo flujo `today`. Al cambiar de día, cada
 * `flatMapLatest` cambia de fecha por separado y cada `stateIn` conserva su
 * último valor hasta que su consulta emite. Si la consulta de "ayer" (la fecha
 * que hasta ahora era "hoy") termina antes que la del nuevo "hoy", la misma
 * toma está simultáneamente en las dos listas.
 *
 * En la interfaz ambas listas comparten el mismo `LazyColumn` y usan
 * `intakeId` como clave, así que la clave se repite. Compose documenta que las
 * claves deben ser únicas; como mínimo la toma se pinta dos veces (una bajo
 * "Hoy" con datos obsoletos y otra bajo "Ayer").
 *
 * El ViewModel debe emitir hoy y ayer de forma atómica y excluir de una lista
 * los IDs presentes en la otra mientras dura el cambio de día.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelMidnightRaceTest {

    private val now = LocalDateTime.of(2026, 11, 5, 23, 59)
    private val profileRepository = mockk<ProfileRepository>(relaxed = true)
    private val intakeRepository = mockk<IntakeRepository>()
    private val medicationRepository = mockk<MedicationRepository>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val intakeNotifications = mockk<IntakeNotifications>(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { reminderScheduler.generateWindow(any(), any()) } returns emptyList()
        every { sessionManager.activeProfileId } returns flowOf(1)
        every { intakeRepository.observeStatisticsIntakes(1, any(), any()) } returns
            flowOf(emptyList())
        viewModel = HomeViewModel(
            profileRepository = profileRepository,
            intakeRepository = intakeRepository,
            medicationRepository = medicationRepository,
            sessionManager = sessionManager,
            reminderScheduler = reminderScheduler,
            intakeNotifications = intakeNotifications
        )
        viewModel.refreshNow(now)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `la misma toma no puede estar a la vez en hoy y en ayer al cambiar de dia`() = runTest {
        val day = now.toLocalDate()
        val intake = intake(id = 1, scheduledAt = day.atTime(23, 0))
        // Las tomas del día D son las mismas antes y después del cambio: la
        // consulta de "ayer" tras medianoche devuelve lo que era "hoy".
        every { intakeRepository.observeIntakesForDay(1, day) } returns
            MutableStateFlow(listOf(intake))
        every { intakeRepository.observeIntakesForDay(1, day.minusDays(1)) } returns
            flowOf(emptyList())
        // La consulta del nuevo día aún no ha emitido: es el caso en el que la
        // consulta de "ayer" gana la carrera.
        every { intakeRepository.observeIntakesForDay(1, day.plusDays(1)) } returns
            MutableSharedFlow()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.intakes.collect()
        }
        viewModel.intakes.first { state -> state.today.any { it.intakeId == 1 } }

        viewModel.refreshNow(now.plusDays(1))
        viewModel.intakes.first { state -> state.yesterday.any { it.intakeId == 1 } }

        val todayIds = viewModel.intakes.value.today.map { it.intakeId }
        val yesterdayIds = viewModel.intakes.value.yesterday.map { it.intakeId }
        val duplicated = todayIds.intersect(yesterdayIds.toSet())

        assertTrue(
            "La toma $duplicated está a la vez en hoy=$todayIds y ayer=$yesterdayIds; " +
                "esa clave se repetiría en el LazyColumn de Inicio",
            duplicated.isEmpty()
        )
    }

    private fun intake(id: Int, scheduledAt: LocalDateTime): IntakeWithMedication =
        IntakeWithMedication(
            intakeId = id,
            medicationId = 1,
            scheduledAt = scheduledAt,
            scheduleId = 1,
            takenAt = null,
            medicationName = "Ibuprofeno",
            dose = 600.0,
            unit = "mg",
            imageUrl = null,
            profileName = "Andrés"
        )
}
