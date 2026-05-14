package com.andres.pillremind.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andres.pillremind.data.local.converters.Converters
import com.andres.pillremind.data.local.dao.IntakeDao
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ProfileDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.dao.UsuarioDao
import com.andres.pillremind.data.local.entity.IntakeEntity
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity
import com.andres.pillremind.data.local.entity.UsuarioEntity

/**
 * Clase que representa la base de datos de la aplicación
 */
@Database(
    entities = [
        UsuarioEntity::class,
        ProfileEntity::class,
        MedicationEntity::class,
        ScheduleEntity::class,
        IntakeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    //Dao para acceder a los datos de los usuarios
    abstract fun usuarioDao(): UsuarioDao

    //Dao para acceder a los datos de los perfiles
    abstract fun perfilDao(): ProfileDao

    //Dao para acceder a los datos de los medicamentos
    abstract fun medicationDao(): MedicationDao

    //Dao para acceder a los datos de los horarios
    abstract fun scheduleDao(): ScheduleDao

    //Dao para acceder a los datos de las tomas
    abstract fun intakeDao(): IntakeDao
}