package com.andres.pillremind.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andres.pillremind.data.local.converters.Converters
import com.andres.pillremind.data.local.dao.ProfileDao
import com.andres.pillremind.data.local.dao.UsuarioDao
import com.andres.pillremind.data.local.entity.UsuarioEntity
import com.andres.pillremind.data.local.entity.ProfileEntity

@Database(
    entities = [UsuarioEntity::class,
        ProfileEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
/**
 * Clase abstracta que representa la base de datos de la aplicación
 * Expone los DAOs para acceder a las tablas locales.
 */
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun perfilDao(): ProfileDao

    companion object {
        //Crea una instancia única de la base de datos (Singleton)
        @Volatile
        private var instance: AppDatabase? = null

        //Devulve la instancia de la base de datos en caso de que no exista
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pillremind.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }

    }
}