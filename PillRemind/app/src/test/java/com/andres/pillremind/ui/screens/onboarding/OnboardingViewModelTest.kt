package com.andres.pillremind.ui.screens.onboarding

import androidx.lifecycle.SavedStateHandle
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.notifications.ExactAlarmStatus
import com.andres.pillremind.notifications.NotificationAction
import com.andres.pillremind.notifications.NotificationStatus
import com.andres.pillremind.notifications.ReminderPermissions
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del estado y de las decisiones de la bienvenida.
 *
 * Los permisos se simulan con MockK para cubrir los casos denegados sin
 * necesitar un dispositivo ni instrumentación.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val reminderPermissions = mockk<ReminderPermissions>()
    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { sessionManager.completeOnboarding() } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        notificationStatus: NotificationStatus = NotificationStatus.GRANTED,
        exactAlarmStatus: ExactAlarmStatus = ExactAlarmStatus.GRANTED,
    ): OnboardingViewModel {
        every { reminderPermissions.notificationStatus() } returns notificationStatus
        every { reminderPermissions.exactAlarmStatus() } returns exactAlarmStatus
        return OnboardingViewModel(
            sessionManager = sessionManager,
            reminderPermissions = reminderPermissions,
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `sin permisos no se puede continuar`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.REQUESTABLE,
            exactAlarmStatus = ExactAlarmStatus.PENDING,
        )

        val state = viewModel.uiState.value

        assertFalse(state.canContinue)
        assertEquals(NotificationAction.REQUEST, state.notificationAction)
        assertFalse(state.showExactAlarmFallback)
    }

    @Test
    fun `conceder ambos permisos permite continuar`() {
        val viewModel = buildViewModel()

        assertTrue(viewModel.uiState.value.canContinue)
        assertEquals(NotificationAction.NONE, viewModel.uiState.value.notificationAction)
    }

    @Test
    fun `android antiguo no exige alarmas exactas`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.GRANTED,
            exactAlarmStatus = ExactAlarmStatus.NOT_REQUIRED,
        )

        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun `una denegacion definitiva de notificaciones abre ajustes`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.REQUESTABLE,
            exactAlarmStatus = ExactAlarmStatus.GRANTED,
        )

        viewModel.onNotificationPermissionResult(showNotificationRationale = false)

        assertEquals(
            NotificationAction.OPEN_SETTINGS,
            viewModel.uiState.value.notificationAction,
        )
    }

    @Test
    fun `mientras haya rationale se puede volver a pedir el permiso`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.REQUESTABLE,
            exactAlarmStatus = ExactAlarmStatus.GRANTED,
        )

        viewModel.onNotificationPermissionResult(showNotificationRationale = true)

        assertEquals(
            NotificationAction.REQUEST,
            viewModel.uiState.value.notificationAction,
        )
    }

    @Test
    fun `las notificaciones bloqueadas abren ajustes aunque no se hayan pedido`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.BLOCKED,
            exactAlarmStatus = ExactAlarmStatus.GRANTED,
        )

        assertEquals(
            NotificationAction.OPEN_SETTINGS,
            viewModel.uiState.value.notificationAction,
        )
    }

    @Test
    fun `tras intentar alarmas exactas aparece la salida de emergencia`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.GRANTED,
            exactAlarmStatus = ExactAlarmStatus.PENDING,
        )

        assertFalse(viewModel.uiState.value.showExactAlarmFallback)

        viewModel.onExactAlarmRequestAttempted()

        assertTrue(viewModel.uiState.value.showExactAlarmFallback)
    }

    @Test
    fun `un dispositivo sin pantalla de alarmas muestra la salida directamente`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.GRANTED,
            exactAlarmStatus = ExactAlarmStatus.UNAVAILABLE,
        )

        assertTrue(viewModel.uiState.value.showExactAlarmFallback)
        assertFalse(viewModel.uiState.value.canContinue)
    }

    @Test
    fun `la salida de emergencia exige las notificaciones concedidas`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.REQUESTABLE,
            exactAlarmStatus = ExactAlarmStatus.UNAVAILABLE,
        )

        assertFalse(viewModel.uiState.value.showExactAlarmFallback)

        viewModel.completeWithoutExactAlarms()

        coVerify(exactly = 0) { sessionManager.completeOnboarding() }
    }

    @Test
    fun `completar sin permisos no persiste la bienvenida`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.REQUESTABLE,
            exactAlarmStatus = ExactAlarmStatus.PENDING,
        )

        viewModel.complete()

        coVerify(exactly = 0) { sessionManager.completeOnboarding() }
    }

    @Test
    fun `completar con permisos persiste la bienvenida`() {
        val viewModel = buildViewModel()

        viewModel.complete()

        coVerify { sessionManager.completeOnboarding() }
    }

    @Test
    fun `la salida de emergencia persiste la bienvenida`() {
        val viewModel = buildViewModel(
            notificationStatus = NotificationStatus.GRANTED,
            exactAlarmStatus = ExactAlarmStatus.UNAVAILABLE,
        )

        viewModel.completeWithoutExactAlarms()

        coVerify { sessionManager.completeOnboarding() }
    }

    @Test
    fun `el intento de permiso sobrevive al guardado de estado`() {
        val first = buildViewModel(
            notificationStatus = NotificationStatus.GRANTED,
            exactAlarmStatus = ExactAlarmStatus.PENDING,
        )
        first.onExactAlarmRequestAttempted()

        val restored = OnboardingViewModel(
            sessionManager = sessionManager,
            reminderPermissions = reminderPermissions,
            savedStateHandle = savedStateHandle,
        )

        assertTrue(restored.uiState.value.showExactAlarmFallback)
    }
}
