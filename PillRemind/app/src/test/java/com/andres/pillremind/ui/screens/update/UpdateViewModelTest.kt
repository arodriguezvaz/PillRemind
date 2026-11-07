package com.andres.pillremind.ui.screens.update

import com.andres.pillremind.domain.model.InstalledAppInfo
import com.andres.pillremind.domain.model.UpdateInfo
import com.andres.pillremind.domain.model.UpdateStatus
import com.andres.pillremind.domain.repository.UpdateRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de la compuerta de actualizaciones.
 *
 * Cubren la política de versiones, la caché offline, el intervalo diario y la
 * exención de las compilaciones de depuración.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UpdateViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `una version por debajo del minimo bloquea`() {
        val repository = repository(
            cached = updateInfo(versionCode = 3, minimumVersionCode = 3),
            lastCheckAt = System.currentTimeMillis(),
        )

        val viewModel = UpdateViewModel(repository)

        assertTrue(viewModel.uiState.value.status is UpdateStatus.Required)
        assertEquals(0, repository.refreshCalls)
    }

    @Test
    fun `una version nueva sin minimo es opcional`() {
        val repository = repository(
            cached = updateInfo(versionCode = 3),
            lastCheckAt = System.currentTimeMillis(),
        )

        val viewModel = UpdateViewModel(repository)

        assertTrue(viewModel.uiState.value.status is UpdateStatus.Optional)
    }

    @Test
    fun `una version descartada no vuelve a mostrarse`() {
        val info = updateInfo(versionCode = 3)
        val repository = repository(
            cached = info,
            dismissed = info.versionCode,
            lastCheckAt = System.currentTimeMillis(),
        )

        val viewModel = UpdateViewModel(repository)

        assertEquals(UpdateStatus.UpToDate, viewModel.uiState.value.status)
    }

    @Test
    fun `un fallo de red conserva el bloqueo cacheado`() {
        val repository = repository(
            cached = updateInfo(versionCode = 3, minimumVersionCode = 3),
            lastCheckAt = null,
            refreshResult = Result.failure(IOException("sin red")),
        )

        val viewModel = UpdateViewModel(repository)

        assertTrue(viewModel.uiState.value.status is UpdateStatus.Required)
        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun `en una build debug el bloqueo se convierte en aviso`() {
        val repository = repository(
            installed = installed(versionCode = 1, isDebuggable = true),
            cached = updateInfo(versionCode = 3, minimumVersionCode = 3),
            lastCheckAt = System.currentTimeMillis(),
        )

        val viewModel = UpdateViewModel(repository)

        assertTrue(viewModel.uiState.value.status is UpdateStatus.Optional)
    }

    @Test
    fun `no comprueba si ya se comprobo hoy`() {
        val repository = repository(lastCheckAt = System.currentTimeMillis())

        UpdateViewModel(repository)

        assertEquals(0, repository.refreshCalls)
    }

    @Test
    fun `comprueba si la ultima comprobacion es antigua`() {
        val repository = repository(
            lastCheckAt = System.currentTimeMillis() - UpdateViewModel.CHECK_INTERVAL_MS - 1,
        )

        UpdateViewModel(repository)

        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun `la comprobacion automatica actualiza el estado`() {
        val repository = repository(
            cached = null,
            lastCheckAt = null,
            refreshResult = Result.success(updateInfo(versionCode = 2)),
        )

        val viewModel = UpdateViewModel(repository)
        val status = viewModel.uiState.value.status

        assertEquals(1, repository.refreshCalls)
        assertTrue(status is UpdateStatus.Optional)
        assertEquals(2L, (status as UpdateStatus.Optional).info.versionCode)
    }

    @Test
    fun `posponer guarda la version y oculta el aviso`() {
        val info = updateInfo(versionCode = 3)
        val repository = repository(
            cached = info,
            lastCheckAt = System.currentTimeMillis(),
        )
        val viewModel = UpdateViewModel(repository)

        viewModel.dismissUpdate(info.versionCode)

        assertEquals(info.versionCode, repository.dismissedVersionCode.value)
        assertEquals(UpdateStatus.UpToDate, viewModel.uiState.value.status)
    }

    private fun repository(
        installed: InstalledAppInfo = installed(),
        cached: UpdateInfo? = null,
        dismissed: Long? = null,
        lastCheckAt: Long? = null,
        refreshResult: Result<UpdateInfo> = Result.failure(IOException("sin red")),
    ) = FakeUpdateRepository(
        installed = installed,
        initialCached = cached,
        initialDismissed = dismissed,
        initialLastCheckAt = lastCheckAt,
        refreshResult = refreshResult,
    )

    private fun installed(
        versionCode: Long = 1,
        isDebuggable: Boolean = false,
    ) = InstalledAppInfo(
        versionCode = versionCode,
        versionName = "1.0.0",
        isDebuggable = isDebuggable,
    )

    private fun updateInfo(
        versionCode: Long,
        minimumVersionCode: Long = 0,
    ) = UpdateInfo(
        versionCode = versionCode,
        versionName = "1.2.0",
        minimumVersionCode = minimumVersionCode,
        apkUrl = "https://github.com/arodriguezvaz/PillRemind/releases/download/v1.2.0/PillRemind-1.2.0.apk",
        releaseUrl = "https://github.com/arodriguezvaz/PillRemind/releases/tag/v1.2.0",
    )

    /**
     * Repositorio falso que permite controlar la caché y la respuesta remota.
     */
    private class FakeUpdateRepository(
        private val installed: InstalledAppInfo,
        initialCached: UpdateInfo?,
        initialDismissed: Long?,
        initialLastCheckAt: Long?,
        private val refreshResult: Result<UpdateInfo>,
    ) : UpdateRepository {

        override val cachedInfo = MutableStateFlow(initialCached)
        override val dismissedVersionCode = MutableStateFlow(initialDismissed)
        override val lastCheckAt = MutableStateFlow(initialLastCheckAt)

        var refreshCalls = 0
            private set

        override suspend fun refresh(): Result<UpdateInfo> {
            refreshCalls++
            refreshResult.getOrNull()?.let { cachedInfo.value = it }
            return refreshResult
        }

        override suspend fun dismiss(versionCode: Long) {
            dismissedVersionCode.value = versionCode
        }

        override suspend fun clearDismissal() {
            dismissedVersionCode.value = null
        }

        override fun installedAppInfo(): InstalledAppInfo = installed
    }
}
