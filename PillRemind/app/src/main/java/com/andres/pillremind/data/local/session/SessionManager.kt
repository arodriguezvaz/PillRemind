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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class SessionManager(private val context: Context) {

    private object Keys {
        val ACTIVE_PROFILE_ID = intPreferencesKey("active_profile_id")
    }

    val activeProfileId: Flow<Int?> = context.dataStore.data
        .map { it[Keys.ACTIVE_PROFILE_ID] }

    suspend fun setActiveProfile(id: Int) {
        context.dataStore.edit { it[Keys.ACTIVE_PROFILE_ID] = id }
    }

    suspend fun clearActiveProfile() {
        context.dataStore.edit { it.remove(Keys.ACTIVE_PROFILE_ID) }
    }
}