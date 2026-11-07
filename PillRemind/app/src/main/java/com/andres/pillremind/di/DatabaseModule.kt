package com.andres.pillremind.di

import android.content.Context
import androidx.room.Room
import com.andres.pillremind.data.local.dao.BackupDao
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.database.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Este módulo de Hilt se encarga de proporcionar la instancia de la base de datos Room
//de la aplicación y los DAOs necesarios para el acceso a los datos.
//
//Se utiliza @Singleton para asegurar que solo exista una única instancia de la base de datos
//durante toda la vida de la aplicación, evitando problemas de consistencia y consumo innecesario
//de recursos.
//
//A través de este módulo se centraliza la creación de la base de datos y se facilita la inyección
//de dependencias en las distintas capas de la aplicación (repositories, viewmodels, etc.).

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pillremind.db"
        )
            .addMigrations(*DatabaseMigrations.ALL)
            // Las versiones 1-5 eran builds internas sin releases públicas.
            // A partir de la 6 no se borra la base de datos: si falta una
            // migración, Room falla en lugar de perder el historial.
            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
            .build()
    }

    @Provides
    fun provideProfileDao(db: AppDatabase) = db.profileDao()

    @Provides
    fun provideMedicationDao(db: AppDatabase) = db.medicationDao()

    @Provides
    fun provideUsuarioDao(db: AppDatabase) = db.userDao()

    @Provides
    fun provideScheduleDao(db: AppDatabase) = db.scheduleDao()

    @Provides
    fun provideIntakeDao(db: AppDatabase) = db.intakeDao()

    @Provides
    fun provideBackupDao(db: AppDatabase): BackupDao = db.backupDao()
}