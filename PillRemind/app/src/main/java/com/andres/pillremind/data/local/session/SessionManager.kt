package com.andres.pillremind.data.local.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
class SessionManager(private val context: Context) {

    /**
     * Objeto interno que define las claves usadas en DataStore.
     */
    private object Keys {
        // Clave para almacenar el ID del profile activo
        val ACTIVE_PROFILE_ID = intPreferencesKey("active_profile_id")
    }

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
}