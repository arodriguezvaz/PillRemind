package com.andres.pillremind.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la política que decide cuándo hay que actualizar.
 */
class UpdateInfoTest {

    @Test
    fun `una version nueva sin minimo es opcional`() {
        val status = updateInfo(versionCode = 10).statusFor(installedVersionCode = 9)

        assertTrue(status is UpdateStatus.Optional)
        assertEquals(10L, (status as UpdateStatus.Optional).info.versionCode)
    }

    @Test
    fun `una version por debajo del minimo es obligatoria`() {
        val status = updateInfo(versionCode = 10, minimumVersionCode = 8)
            .statusFor(installedVersionCode = 7)

        assertTrue(status is UpdateStatus.Required)
        assertEquals(10L, (status as UpdateStatus.Required).info.versionCode)
    }

    @Test
    fun `una version entre el minimo y la ultima sigue siendo valida`() {
        val status = updateInfo(versionCode = 10, minimumVersionCode = 8)
            .statusFor(installedVersionCode = 8)

        assertTrue(status is UpdateStatus.Optional)
    }

    @Test
    fun `la ultima version esta al dia`() {
        val status = updateInfo(versionCode = 10, minimumVersionCode = 8)
            .statusFor(installedVersionCode = 10)

        assertEquals(UpdateStatus.UpToDate, status)
    }

    @Test
    fun `una version instalada superior esta al dia`() {
        val status = updateInfo(versionCode = 10).statusFor(installedVersionCode = 11)

        assertEquals(UpdateStatus.UpToDate, status)
    }

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
}
