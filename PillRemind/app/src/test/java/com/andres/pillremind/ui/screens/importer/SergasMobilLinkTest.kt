package com.andres.pillremind.ui.screens.importer

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Pruebas del enlace a Sergas Móbil: debe abrir la app o su ficha en la tienda,
 * nunca la página web de la app.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [33])
class SergasMobilLinkTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `sin la app instalada abre su ficha en la tienda`() {
        assertTrue(openSergasMobil(application))

        val started = shadowOf(application).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals("market://details?id=$SERGAS_MOBIL_PACKAGE", started.dataString)
    }

    @Test
    fun `con la app instalada la abre en lugar de la tienda`() {
        val launcher = ComponentName(SERGAS_MOBIL_PACKAGE, "es.sergas.appbox.MainActivity")
        shadowOf(application.packageManager).apply {
            addActivityIfNotPresent(launcher)
            addIntentFilterForActivity(
                launcher,
                IntentFilter(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                },
            )
        }

        assertTrue(openSergasMobil(application))

        val started = shadowOf(application).nextStartedActivity
        assertEquals(SERGAS_MOBIL_PACKAGE, started.component?.packageName)
    }
}

/** Paquete real de Sergas Móbil en Google Play. */
private const val SERGAS_MOBIL_PACKAGE = "es.sergas.appbox"
