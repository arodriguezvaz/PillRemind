package com.andres.pillremind.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andres.pillremind.data.local.converters.Converters
import com.andres.pillremind.data.local.dao.BackupDao
import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ProfileDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.dao.UserDao
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.local.entity.UserEntity

/**
 * Versión actual del esquema de Room.
 *
 * Es la única fuente de verdad: la anotación y los tests de migración la usan
 * para no quedar desincronizados al subir de versión.
 */
const val APP_DATABASE_VERSION = 6

/**
 * Clase que representa la base de datos de la aplicación
 */
@Database(
    entities = [
        UserEntity::class,
        ProfileEntity::class,
        MedicationEntity::class,
        ScheduleEntity::class,
        IntakeEntity::class
    ],
    version = APP_DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    //Dao para acceder a los datos de los usuarios
    abstract fun userDao(): UserDao

    //Dao para acceder a los datos de los perfiles
    abstract fun profileDao(): ProfileDao

    //Dao para acceder a los datos de los medicamentos
    abstract fun medicationDao(): MedicationDao

    //Dao para acceder a los datos de los horarios
    abstract fun scheduleDao(): ScheduleDao

    //Dao para acceder a los datos de las tomas
    abstract fun intakeDao(): IntakeDao

    //Dao para exportar e importar copias de seguridad
    abstract fun backupDao(): BackupDao
}