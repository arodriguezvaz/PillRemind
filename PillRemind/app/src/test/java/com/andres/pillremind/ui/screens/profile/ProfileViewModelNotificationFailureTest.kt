package com.andres.pillremind.ui.screens.profile

import android.content.Context
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.notifications.IntakeNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Comprueba que un fallo al cancelar notificaciones no impide terminar el
 * borrado de un perfil ni limpiar la sesión activa.
 *
 * El ViewModel registra el fallo con `android.util.Log`, de ahí Robolectric.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProfileViewModelNotificationFailureTest {

    private val profileRepository = mockk<ProfileRepository>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val intakeRepository = mockk<IntakeRepository>()
    private val intakeNotifications = mockk<IntakeNotifications>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private val profileId = 10
    private val userId = 1

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.activeProfileId } returns flowOf(profileId)
        viewModel = ProfileViewModel(
            profileRepository = profileRepository,
            sessionManager = sessionManager,
            reminderScheduler = reminderScheduler,
            intakeRepository = intakeRepository,
            intakeNotifications = intakeNotifications,
            context = context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `si falla la cancelacion de notificaciones el perfil se borra y la sesion se limpia`() =
        runTest {
            val profile = Profile(id = profileId, usuarioId = userId, name = "Ana")
            coEvery { intakeRepository.getIntakeIdsByProfileId(profileId) } returns listOf(11)
            coEvery { reminderScheduler.cancelFutureForProfile(profileId) } returns Unit
            coEvery { profileRepository.deleteProfile(profile) } just Runs
            coEvery {
                intakeNotifications.cancelAll(any<Collection<Int>>())
            } throws IllegalStateException("fallo del gestor de notificaciones")
            var deleted = false

            viewModel.deleteProfile(profile) { deleted = true }

            coVerify { profileRepository.deleteProfile(profile) }
            coVerify { sessionManager.clearActiveProfile() }
            assertTrue(deleted)
        }
}
