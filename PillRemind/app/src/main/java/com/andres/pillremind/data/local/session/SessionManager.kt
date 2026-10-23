package com.andres.pillremind.data.local.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.andres.pillremind.data.local.session.SessionManager.Keys.DARK_MODE_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

// Extensión de Context para crear una única instancia de DataStore.
// Se usa para almacenar preferencias relacionadas con la sesión.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

/**
 * Clase encargada de gestionar la sesión del usuario.
 *
 * Permite guardar, obtener y eliminar el ID del profile activo
 * utilizando DataStore como sistema de persistencia.
 *
 * @property context Contexto necesario para acceder a DataStore.
 */
@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    /**
     * Objeto interno que define las claves usadas en DataStore.
     */
    private object Keys {
        // Clave para almacenar el ID del profile activo
        val ACTIVE_PROFILE_ID = intPreferencesKey("active_profile_id")

        // Clave para almacenar el ID de usuario activo
        val ACTIVE_USER_ID = intPreferencesKey("active_user_id")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val LAST_GENERATED_DATE = stringPreferencesKey("ultima_generacion")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { it[DARK_MODE_KEY] ?: false }

    /**
     * Devuelve el ID almacenado si existe o null si no hay ningún profile activo.
     * Se actualiza automáticamente cuando cambian los datos en DataStore.
     */
    val activeProfileId: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.ACTIVE_PROFILE_ID]
        }

    /**
     * Guarda el ID del profile activo en DataStore.
     * @param id ID del profile que se quiere establecer como activo.
     */
    suspend fun setActiveProfile(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ACTIVE_PROFILE_ID] = id
        }
    }

    /**
     * Elimina el profile activo de la sesión
     */
    suspend fun clearActiveProfile() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.ACTIVE_PROFILE_ID)
        }
    }

    /**
     * Devuelve el ID almacenado si existe o null si no hay ningún usuario activo.
     */
    val activeUserId: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.ACTIVE_USER_ID]
        }

    /**
     * Guarda el identificador del usuario activo.
     */
    suspend fun setActiveUser(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ACTIVE_USER_ID] = id
        }
    }

    /**
     * Elimina el usuario activo almacenado.
     */
    suspend fun clearActiveUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.ACTIVE_USER_ID)
        }
    }

    /**
     * Fecha de la última generación de tomas.
     */
    val lastGeneratedDate: Flow<LocalDate?> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.LAST_GENERATED_DATE]?.let { LocalDate.parse(it) }
        }

    /**
     * Guarda la preferencia de modo oscuro.
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    /**
     * Guarda la fecha de la última generación de tomas.
     */
    suspend fun saveLastGeneratedDate(fecha: LocalDate) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_GENERATED_DATE] = fecha.toString()
        }
    }

    /**
     * Borra la fecha de última generación para que el siguiente usuario o la
     * siguiente descarga reconstruyan la ventana desde cero.
     */
    suspend fun clearLastGeneratedDate() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.LAST_GENERATED_DATE)
        }
    }
}
