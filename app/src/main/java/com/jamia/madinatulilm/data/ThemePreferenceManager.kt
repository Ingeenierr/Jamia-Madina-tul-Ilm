package com.jamia.madinatulilm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This creates a single instance of our DataStore for the whole app.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferenceManager(context: Context) {

    private val dataStore = context.dataStore

    // This is the key we will use to save our dark mode setting.
    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    }

    // This function saves the user's choice (true for dark mode, false for light mode).
    suspend fun setDarkMode(isDarkMode: Boolean) {
        dataStore.edit { settings ->
            settings[DARK_MODE_KEY] = isDarkMode
        }
    }

    // This gives us a Flow that will always emit the current theme preference.
    // Our UI will listen to this to know whether to be in dark or light mode.
    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false // Default to light mode (false)
    }
}
