package com.andres.pillremind.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andres.pillremind.data.local.converters.Converters
import com.andres.pillremind.data.local.dao.PerfilDao
import com.andres.pillremind.data.local.dao.UsuarioDao
import com.andres.pillremind.data.local.entity.UsuarioEntity

@Database(
    entities = [UsuarioEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
/**
 * Clase abstracta que representa la base de datos de la aplicación
 * @property usuarioDao DAO para acceder a los datos de los usuarios
 */
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun perfilDao(): PerfilDao

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
                    "pilremind.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }

    }
}