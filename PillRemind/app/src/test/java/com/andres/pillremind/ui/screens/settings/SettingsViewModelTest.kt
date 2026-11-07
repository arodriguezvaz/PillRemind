package com.andres.pillremind.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.andres.pillremind.R
import com.andres.pillremind.data.backup.BackupFileStore
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.model.InstalledAppInfo
import com.andres.pillremind.domain.model.ThemeMode
import com.andres.pillremind.domain.model.UpdateInfo
import com.andres.pillremind.domain.repository.BackupRepository
import com.andres.pillremind.domain.repository.UpdateRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.ByteArrayOutputStream
import java.io.IOException
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
    private val updateRepository = mockk<UpdateRepository>(relaxed = true)
    private val savedStateHandle = SavedStateHandle()
    private val uri: Uri = Uri.parse("content://test/backup.pmbackup")

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sessionManager.themeMode } returns flowOf(ThemeMode.SYSTEM)
        every { updateRepository.installedAppInfo() } returns InstalledAppInfo(
            versionCode = 2,
            versionName = "1.1.0",
            isDebuggable = false,
        )
        every { backupFileStore.openOutput(uri) } returns ByteArrayOutputStream()
        coEvery { backupRepository.exportTo(any(), any()) } just Runs
        viewModel = SettingsViewModel(
            sessionManager = sessionManager,
            backupRepository = backupRepository,
            backupFileStore = backupFileStore,
            updateRepository = updateRepository,
            savedStateHandle = savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `guarda el tema elegido`() {
        viewModel.setThemeMode(ThemeMode.DARK)

        coVerify { sessionManager.setThemeMode(ThemeMode.DARK) }
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

    @Test
    fun `la comprobacion manual informa de que no hay actualizaciones`() {
        coEvery { updateRepository.refresh() } returns Result.success(updateInfo(versionCode = 2))

        viewModel.checkForUpdates()

        assertEquals(UpdateCheckUiState.UpToDate, viewModel.updateCheckState.value)
        coVerify(exactly = 0) { updateRepository.clearDismissal() }
        viewModel.consumeUpdateCheckState()
        assertEquals(UpdateCheckUiState.Idle, viewModel.updateCheckState.value)
    }

    @Test
    fun `la comprobacion manual detecta una version nueva`() {
        coEvery { updateRepository.refresh() } returns Result.success(updateInfo(versionCode = 3))

        viewModel.checkForUpdates()

        assertEquals(UpdateCheckUiState.Available, viewModel.updateCheckState.value)
        coVerify(exactly = 1) { updateRepository.clearDismissal() }
    }

    @Test
    fun `la comprobacion manual informa de un error de red`() {
        coEvery { updateRepository.refresh() } returns Result.failure(IOException("sin red"))

        viewModel.checkForUpdates()

        assertEquals(UpdateCheckUiState.Error, viewModel.updateCheckState.value)
    }

    private fun updateInfo(versionCode: Long) = UpdateInfo(
        versionCode = versionCode,
        versionName = "1.2.0",
        minimumVersionCode = 0,
        apkUrl = "https://github.com/arodriguezvaz/PillRemind/releases/download/v1.2.0/PillRemind-1.2.0.apk",
        releaseUrl = "https://github.com/arodriguezvaz/PillRemind/releases/tag/v1.2.0",
    )
}
