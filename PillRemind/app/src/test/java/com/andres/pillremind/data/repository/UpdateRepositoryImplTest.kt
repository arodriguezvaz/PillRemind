package com.andres.pillremind.data.repository

import android.content.Context
import com.andres.pillremind.data.local.update.UpdatePreferences
import com.andres.pillremind.data.remote.GitHubUpdateDataSource
import com.andres.pillremind.domain.model.UpdateInfo
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la caché y del tratamiento de errores del repositorio.
 */
class UpdateRepositoryImplTest {

    private val context = mockk<Context>(relaxed = true)
    private val dataSource = mockk<GitHubUpdateDataSource>()
    private val preferences = mockk<UpdatePreferences>(relaxed = true)

    private val info = UpdateInfo(
        versionCode = 42,
        versionName = "1.2.0",
        minimumVersionCode = 30,
        apkUrl = "https://github.com/arodriguezvaz/PillRemind/releases/download/v1.2.0/PillRemind-1.2.0.apk",
        releaseUrl = "https://github.com/arodriguezvaz/PillRemind/releases/tag/v1.2.0",
    )

    @Test
    fun `guarda la informacion cuando la consulta funciona`() = runBlocking {
        coEvery { dataSource.fetchLatest() } returns info
        coEvery { preferences.saveCheck(info, any()) } just Runs

        val result = repository().refresh()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { preferences.saveCheck(info, any()) }
        coVerify(exactly = 0) { preferences.saveLastCheckAt(any()) }
    }

    @Test
    fun `guarda la marca temporal cuando la consulta falla`() = runBlocking {
        coEvery { dataSource.fetchLatest() } throws IOException("sin red")
        coEvery { preferences.saveLastCheckAt(any()) } just Runs

        val result = repository().refresh()

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { preferences.saveLastCheckAt(any()) }
        coVerify(exactly = 0) { preferences.saveCheck(any(), any()) }
    }

    private fun repository() = UpdateRepositoryImpl(
        dataSource = dataSource,
        preferences = preferences,
        context = context,
    )
}
