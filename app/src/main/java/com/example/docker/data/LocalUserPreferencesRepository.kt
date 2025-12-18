package com.example.docker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class LocalUserPreferencesRepository(private val context: Context) {

    private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")

    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // Default to false (Light theme) or maybe we can check system somehow, 
            // but for now let's default to false as per request "switch manually"
            // Usually we might want to default to System, but DataStore stores a specific value.
            // If the user hasn't set it, we could return null or handle it in UI.
            // For simplicity, let's say default is false (Light) or we leave it to the UI to decide if missing.
            // But map returns a non-nullable Boolean here.
            preferences[IS_DARK_THEME] ?: false
        }

    // A better approach might be to store an Enum: LIGHT, DARK, SYSTEM.
    // The requirement says "Manually switch Light/Dark".
    // So simple boolean is fine.
    
    suspend fun saveThemePreference(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }
}
