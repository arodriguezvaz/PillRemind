package com.andres.pillremind.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.andres.pillremind.R
import com.andres.pillremind.data.backup.BackupFileStore
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.repository.BackupRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.ByteArrayOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas del flujo de preparación y ejecución de la exportación.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val backupRepository = mockk<BackupRepository>(relaxed = true)
    private val backupFileStore = mockk<BackupFileStore>(relaxed = true)
    private val savedStateHandle = SavedStateHandle()
    private val uri: Uri = Uri.parse("content://test/backup.pmbackup")

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.isDarkMode } returns flowOf(false)
        every { backupFileStore.openOutput(uri) } returns ByteArrayOutputStream()
        coEvery { backupRepository.exportTo(any(), any()) } just Runs
        viewModel = SettingsViewModel(
            sessionManager = sessionManager,
            backupRepository = backupRepository,
            backupFileStore = backupFileStore,
            savedStateHandle = savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exporta con la contrasena preparada`() {
        var receivedPassword: String? = null
        val answerLatch = CountDownLatch(1)
        coEvery { backupRepository.exportTo(any(), any()) } coAnswers {
            receivedPassword = secondArg<CharArray?>()?.concatToString()
            answerLatch.countDown()
        }
        assertTrue(viewModel.prepareExport("clave-segura".toCharArray()))

        viewModel.exportBackup(uri)

        coVerify(timeout = 2_000) { backupRepository.exportTo(any(), any()) }
        assertTrue(answerLatch.await(2, TimeUnit.SECONDS))
        assertEquals("clave-segura", receivedPassword)
    }

    @Test
    fun `exporta sin contrasena cuando no se protege`() {
        assertTrue(viewModel.prepareExport(null))

        viewModel.exportBackup(uri)

        coVerify(timeout = 2_000) { backupRepository.exportTo(any(), null) }
    }

    @Test
    fun `cancela si se pierde la contrasena de una copia cifrada`() {
        savedStateHandle[SettingsViewModel.KEY_PENDING_EXPORT] = true
        savedStateHandle[SettingsViewModel.KEY_PENDING_EXPORT_ENCRYPTED] = true

        viewModel.exportBackup(uri)

        verify(exactly = 1) { backupFileStore.delete(uri) }
        coVerify(exactly = 0) { backupRepository.exportTo(any(), any()) }
        val state = viewModel.backupState.value
        assertTrue(state is BackupUiState.Error)
        assertEquals(
            R.string.backup_error_export_restarted,
            (state as BackupUiState.Error).messageRes
        )
    }

    @Test
    fun `cancelar la exportacion pendiente evita exportar`() {
        assertTrue(viewModel.prepareExport("clave-segura".toCharArray()))
        viewModel.cancelPendingExport()

        viewModel.exportBackup(uri)

        verify(exactly = 1) { backupFileStore.delete(uri) }
        coVerify(exactly = 0) { backupRepository.exportTo(any(), any()) }
        assertTrue(viewModel.backupState.value is BackupUiState.Error)
    }
}
