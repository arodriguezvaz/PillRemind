package com.andres.pillremind.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier personalizado que sirve para diferenciar este CoroutineScope
 * de otros posibles CoroutineScope que existan en la aplicación esto permite saber a hilt cual inyectar
 *
 * Este qualifier permite pedir específicamente el scope global de la aplicación:@ApplicationScope CoroutineScope
 *
 * en lugar de otros scopes como viewModelScope.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope


@Module
//Las dependencias creadas aquí viven durante toda la vida de la aplicación y solo se crean una vez (singleton)
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    /**
     * CoroutineScope global de la aplicación.
     *
     * Se usa para tareas en background que no dependen de la UI,
     * como sincronización con backend.
     *
     * SupervisorJob -> si una corrutina falla, las demás siguen ejecutándose
     * Dispatchers.IO -> optimizado para tareas de red y base de datos
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
