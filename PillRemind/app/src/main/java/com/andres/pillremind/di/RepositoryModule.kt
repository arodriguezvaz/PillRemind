package com.andres.pillremind.di

import com.andres.pillremind.data.repository.MedicationRepositoryImpl
import com.andres.pillremind.data.repository.ProfileRepositoryImpl
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository
}