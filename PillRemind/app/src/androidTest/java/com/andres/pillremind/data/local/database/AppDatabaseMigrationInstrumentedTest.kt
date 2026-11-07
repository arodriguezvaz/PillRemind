package com.andres.pillremind.data.local.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
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
 * creada por una versión publicada se puede abrir con el esquema actual sin
 * perder datos. Usa el SQLite real de Android y también abre la base de datos
 * con `AppDatabase` para validar el hash generado por Room.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationInstrumentedTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun unaBaseDeDatosV6MigraALaVersionActualSinPerderDatos() {
        helper.createDatabase(TEST_DB, BASELINE_VERSION).apply {
            seedAllTables()
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DB,
            APP_DATABASE_VERSION,
            true,
            *DatabaseMigrations.ALL,
        )
        try {
            assertEquals(APP_DATABASE_VERSION, migrated.version)
            migrated.assertSeededData()
        } finally {
            migrated.close()
        }

        val database = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            TEST_DB,
        )
            .addMigrations(*DatabaseMigrations.ALL)
            .allowMainThreadQueries()
            .build()

        try {
            val connection = database.openHelper.writableDatabase
            assertEquals(APP_DATABASE_VERSION, connection.version)
            connection.assertSeededData()
        } finally {
            database.close()
        }
    }

    private fun SupportSQLiteDatabase.seedAllTables() {
        execSQL(
            "INSERT INTO usuarios (id, createdAt, updatedAt) VALUES (?, ?, ?)",
            arrayOf<Any?>(1, "2026-10-01T09:00:00", "2026-10-01T09:00:00"),
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
            ),
        )
        execSQL(
            """
            INSERT INTO medications (
                id, profileId, name, dose, unit, isChronic, startDate,
                createdAt, updatedAt
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                1,
                1,
                "Ibuprofeno",
                400.0,
                "mg",
                0,
                "2026-10-01",
                "2026-10-01T09:00:00",
                "2026-10-01T09:00:00",
            ),
        )
        execSQL(
            """
            INSERT INTO schedules (id, medicationId, type, weekDay, time)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(1, 1, "WEEKLY", "MONDAY", "08:00"),
        )
        execSQL(
            """
            INSERT INTO intakes (
                id, medicationId, scheduleId, scheduledAt, takenAt,
                medicationNameSnapshot, doseSnapshot, unitSnapshot,
                createdAt, updatedAt
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                1,
                1,
                1,
                "2026-10-31T08:00:00",
                null,
                "Ibuprofeno",
                400.0,
                "mg",
                "2026-10-31T08:00:00",
                "2026-10-31T08:00:00",
            ),
        )
    }

    private fun SupportSQLiteDatabase.assertSeededData() {
        assertEquals(1, count("usuarios"))
        assertEquals(1, count("profiles"))
        assertEquals(1, count("medications"))
        assertEquals(1, count("schedules"))
        assertEquals(1, count("intakes"))

        query("SELECT name, birthDate, healthCardNumber FROM profiles WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Ana", cursor.getString(0))
            assertEquals("1990-05-12", cursor.getString(1))
            assertEquals("SIP-123", cursor.getString(2))
        }
        query("SELECT name, dose, unit FROM medications WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Ibuprofeno", cursor.getString(0))
            assertEquals(400.0, cursor.getDouble(1), 0.0)
            assertEquals("mg", cursor.getString(2))
        }
        query("SELECT type, weekDay, time FROM schedules WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("WEEKLY", cursor.getString(0))
            assertEquals("MONDAY", cursor.getString(1))
            assertEquals("08:00", cursor.getString(2))
        }
        query(
            "SELECT medicationNameSnapshot, doseSnapshot FROM intakes WHERE id = 1"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Ibuprofeno", cursor.getString(0))
            assertEquals(400.0, cursor.getDouble(1), 0.0)
        }
    }

    private fun SupportSQLiteDatabase.count(table: String): Int =
        query("SELECT COUNT(*) FROM $table").use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getInt(0)
        }

    private companion object {
        /** Última versión publicada en v1.0.0/v1.1.0. */
        const val BASELINE_VERSION = 6
        const val TEST_DB = "app-database-migration-instrumented-test.db"
    }
}
