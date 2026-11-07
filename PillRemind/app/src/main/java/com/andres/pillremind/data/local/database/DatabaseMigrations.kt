package com.andres.pillremind.data.local.database

import androidx.room.migration.Migration

/**
 * Migraciones manuales de la base de datos.
 *
 * Room ejecuta automáticamente las auto-migraciones declaradas en
 * [AppDatabase]. Aquí se registran las migraciones que necesitan transformar
 * datos o recrear tablas.
 *
 * Cada migración debe ir acompañada de su test en `AppDatabaseMigrationTest`.
 */
object DatabaseMigrations {

    /**
     * Migraciones que se pasan a [androidx.room.RoomDatabase.Builder.addMigrations].
     *
     * Actualmente no hay ninguna: la versión 6 es la base inicial. La primera
     * migración real será 6 → 7 cuando cambie el esquema.
     */
    val ALL: Array<Migration> = emptyArray()
}
