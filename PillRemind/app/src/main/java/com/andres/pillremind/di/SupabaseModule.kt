package com.andres.pillremind.di

import com.andres.pillremind.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

/* Módulo de Hilt que proporciona las dependencias necesarias para trabajar con Supabase.
*
* Este módulo crea un único cliente de Supabase (Singleton) y expone sus módulos
* principales (Auth, Postgrest y Storage) para poder inyectarlos en toda la aplicación.
*/
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }

    /**
     * Proporciona el módulo de autenticación de Supabase.
     *
     * Permite gestionar:
     * - login
     * - logout
     * - usuario actual
     * - sesiones
     */
    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth = client.auth

    /**
     * Proporciona el módulo de base de datos (PostgREST).
     *
     * Permite realizar operaciones CRUD sobre las tablas de Supabase.
     */
    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    /**
     * Proporciona el módulo de almacenamiento de archivos.
     *
     * Se utiliza para subir, descargar y gestionar archivos (imágenes, PDFs, etc.).
     *
     * NO IMPLEMENTADO
     */
    @Provides
    @Singleton
    fun provideSupabaseStorage(client: SupabaseClient): Storage = client.storage
}