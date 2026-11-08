package com.andres.pillremind.di

import com.andres.pillremind.data.ocr.MlKitOcrEngine
import com.andres.pillremind.data.ocr.OcrEngine
import com.andres.pillremind.data.repository.MedicationLookupRepositoryImpl
import com.andres.pillremind.domain.repository.MedicationLookupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Enlaces del escaneo de CN y del OCR.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ScanModule {

    @Binds
    @Singleton
    abstract fun bindOcrEngine(impl: MlKitOcrEngine): OcrEngine

    @Binds
    @Singleton
    abstract fun bindMedicationLookupRepository(
        impl: MedicationLookupRepositoryImpl
    ): MedicationLookupRepository
}
