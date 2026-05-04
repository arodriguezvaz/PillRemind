package com.andres.pillremind.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andres.pillremind.data.local.converters.Converters
import com.andres.pillremind.data.local.dao.MedicationDao
import com.andres.pillremind.data.local.dao.ProfileDao
import com.andres.pillremind.data.local.dao.ScheduleDao
import com.andres.pillremind.data.local.dao.UsuarioDao
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.UsuarioEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import com.andres.pillremind.data.local.entity.ScheduleEntity

@Database(
    entities = [
        UsuarioEntity::class,
        ProfileEntity::class,
        MedicationEntity::class,
        ScheduleEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun perfilDao(): ProfileDao
    abstract fun medicationDao(): MedicationDao
    abstract fun scheduleDao(): ScheduleDao
}