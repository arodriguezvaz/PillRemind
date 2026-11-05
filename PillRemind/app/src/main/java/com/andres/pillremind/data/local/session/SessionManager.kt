package com.andres.pillremind.data.local.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.andres.pillremind.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

// Extensión de Context para crear una única instancia de DataStore.
// Se usa para almacenar preferencias relacionadas con la sesión.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

/**
 * Clase encargada de gestionar la sesión del usuario.
 *
 * Permite guardar, obtener y eliminar el ID del profile activo, la preferencia
 * de tema y el resto de ajustes de sesión utilizando DataStore como sistema de
 * persistencia.
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
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LEGACY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val LAST_GENERATED_DATE = stringPreferencesKey("ultima_generacion")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    /**
     * Preferencia de tema de la aplicación.
     *
     * Si existe la clave antigua `dark_mode`, se migra al vuelo: activada pasa
     * a [ThemeMode.DARK] y desactivada a [ThemeMode.SYSTEM], porque ese era el
     * valor por defecto anterior.
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.THEME_MODE]?.toThemeMode()
                ?: if (preferences[Keys.LEGACY_DARK_MODE] == true) {
                    ThemeMode.DARK
                } else {
                    ThemeMode.SYSTEM
                }
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
     * Fecha de la última generación de tomas.
     */
    val lastGeneratedDate: Flow<LocalDate?> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.LAST_GENERATED_DATE]?.let { LocalDate.parse(it) }
        }

    /**
     * Indica si el usuario ya ha completado la pantalla de bienvenida.
     *
     * Por defecto devuelve `false`, de modo que la bienvenida se muestra en el
     * primer arranque y una única vez.
     */
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] ?: false
        }

    /**
     * Guarda que el usuario ya ha completado la bienvenida.
     */
    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    /**
     * Guarda la preferencia de tema y elimina la clave antigua de modo oscuro.
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
            preferences.remove(Keys.LEGACY_DARK_MODE)
        }
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
     * Borra la fecha de la última generación de tomas.
     *
     * Se usa tras importar una copia para que la ventana de recordatorios se
     * regenere desde el día actual.
     */
    suspend fun clearLastGeneratedDate() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.LAST_GENERATED_DATE)
        }
    }

    /**
     * Convierte el valor persistido en [ThemeMode], ignorando valores inválidos.
     */
    private fun String.toThemeMode(): ThemeMode? =
        ThemeMode.entries.firstOrNull { it.name == this }
}
