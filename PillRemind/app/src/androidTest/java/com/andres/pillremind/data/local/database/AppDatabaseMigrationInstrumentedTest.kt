package com.andres.pillremind.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test instrumentado de la base de datos.
 *
 * Repite en un dispositivo o emulador la comprobación de que una base de datos
 * creada por una versión publicada se abre con el esquema actual sin perder
 * datos. Es el mismo escenario que el test local con Robolectric, pero usando
 * el SQLite real de Android.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationInstrumentedTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun unaBaseDeDatosV6SeAbreConElEsquemaActual() {
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                "INSERT INTO usuarios (id, createdAt, updatedAt) VALUES (?, ?, ?)",
                arrayOf<Any?>(1, "2026-10-01T09:00:00", "2026-10-01T09:00:00")
            )
            execSQL(
                """
                INSERT INTO profiles (
                    id, usuarioId, name, imageUrl, birthDate, sex, bloodType,
                    allergies, medicalNotes, healthCardNumber, createdAt, updatedAt
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any?>(
                    1,
                    1,
                    "Ana",
                    null,
                    "1990-05-12",
                    "FEMALE",
                    "O_POSITIVE",
                    "Penicilina",
                    "Asma",
                    "SIP-123",
                    "2026-10-01T09:00:00",
                    "2026-10-01T09:00:00",
                )
            )
            close()
        }

        val database = helper.runMigrationsAndValidate(
            TEST_DB,
            CURRENT_VERSION,
            true,
            *DatabaseMigrations.ALL,
        )

        database.query("SELECT name FROM profiles WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Ana", cursor.getString(0))
        }
        database.close()
    }

    private companion object {
        /** Debe coincidir con `@Database(version = ...)` de AppDatabase. */
        const val CURRENT_VERSION = 6
        const val TEST_DB = "app-database-migration-instrumented-test.db"
    }
}
