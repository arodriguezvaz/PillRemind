package com.andres.pillremind.di

import com.andres.pillremind.data.repository.AuthRepositoryImpl
import com.andres.pillremind.data.repository.IntakeRepositoryImpl
import com.andres.pillremind.data.repository.MedicationRepositoryImpl
import com.andres.pillremind.data.repository.ProfileRepositoryImpl
import com.andres.pillremind.data.repository.ScheduleRepositoryImpl
import com.andres.pillremind.data.repository.UserRepositoryImpl
import com.andres.pillremind.domain.repository.AuthRepository
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
import com.andres.pillremind.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

//Módulo de Hilt encargado de enlazar las interfaces de los repositorios con sus implementaciones.
// Esto permite desacoplar la capa de dominio de la capa de datos, aplicando principios de arquitectura limpia
// y facilitando la escalabilidad y las pruebas de la aplicación.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository

    @Binds
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds
    abstract fun bindIntakeRepository(impl: IntakeRepositoryImpl): IntakeRepository

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}