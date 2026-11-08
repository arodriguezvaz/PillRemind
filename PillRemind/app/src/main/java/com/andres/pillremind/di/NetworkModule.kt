package com.andres.pillremind.di

import com.andres.pillremind.data.remote.HttpTextFetcher
import com.andres.pillremind.data.remote.UrlConnectionHttpTextFetcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Enlaces de la capa de red.
 *
 * De momento solo se usa para la comprobación de actualizaciones: el resto de
 * la aplicación sigue funcionando sin conexión.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    abstract fun bindHttpTextFetcher(
        impl: UrlConnectionHttpTextFetcher
    ): HttpTextFetcher
}
