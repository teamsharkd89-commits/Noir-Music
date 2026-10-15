package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "noir_preferences")

enum class SortOption {
    TITLE,
    ARTIST,
    ALBUM,
    DURATION,
    DATE_ADDED
}

data class UserPreferences(
    val sortOption: SortOption = SortOption.DATE_ADDED,
    val sortAscending: Boolean = false,
    val isGridView: Boolean = false,
    val selectedTab: Int = 0,
    val lastSongId: Long = -1L,
    val lastPositionMs: Long = 0L,
    val shuffleMode: Boolean = false,
    val repeatMode: Int = 0, // 0: OFF, 1: ALL, 2: ONE
    val animationsEnabled: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val SORT_ASCENDING = booleanPreferencesKey("sort_ascending")
        val IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
        val SELECTED_TAB = intPreferencesKey("selected_tab")
        val LAST_SONG_ID = longPreferencesKey("last_song_id")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val ACTIVE_THEME_JSON = stringPreferencesKey("active_theme_json")
        val CUSTOM_THEMES_JSON = stringPreferencesKey("custom_themes_json")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val sortName = preferences[PreferencesKeys.SORT_OPTION] ?: SortOption.DATE_ADDED.name
        val sortOption = try {
            SortOption.valueOf(sortName)
        } catch (_: Exception) {
            SortOption.DATE_ADDED
        }
        val sortAscending = preferences[PreferencesKeys.SORT_ASCENDING] ?: false
        val isGridView = preferences[PreferencesKeys.IS_GRID_VIEW] ?: false
        val selectedTab = preferences[PreferencesKeys.SELECTED_TAB] ?: 0
        val lastSongId = preferences[PreferencesKeys.LAST_SONG_ID] ?: -1L
        val lastPositionMs = preferences[PreferencesKeys.LAST_POSITION_MS] ?: 0L
        val shuffleMode = preferences[PreferencesKeys.SHUFFLE_MODE] ?: false
        val repeatMode = preferences[PreferencesKeys.REPEAT_MODE] ?: 0
        val animationsEnabled = preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true

        UserPreferences(
            sortOption = sortOption,
            sortAscending = sortAscending,
            isGridView = isGridView,
            selectedTab = selectedTab,
            lastSongId = lastSongId,
            lastPositionMs = lastPositionMs,
            shuffleMode = shuffleMode,
            repeatMode = repeatMode,
            animationsEnabled = animationsEnabled
        )
    }

    suspend fun updateSortOption(sortOption: SortOption, ascending: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_OPTION] = sortOption.name
            preferences[PreferencesKeys.SORT_ASCENDING] = ascending
        }
    }

    suspend fun toggleGridView(isGrid: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_GRID_VIEW] = isGrid
        }
    }

    suspend fun setSelectedTab(tabIndex: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_TAB] = tabIndex
        }
    }

    suspend fun saveLastPlaybackState(songId: Long, positionMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SONG_ID] = songId
            preferences[PreferencesKeys.LAST_POSITION_MS] = positionMs
        }
    }

    suspend fun saveShuffleMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHUFFLE_MODE] = enabled
        }
    }

    suspend fun saveRepeatMode(repeatMode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REPEAT_MODE] = repeatMode
        }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = enabled
        }
    }

    val activeThemeFlow: Flow<AppThemeConfig> = context.dataStore.data.map { preferences ->
        val json = preferences[PreferencesKeys.ACTIVE_THEME_JSON]
        if (json != null) {
            AppThemeConfig.fromJson(json) ?: AppThemeConfig.NOIR_ORIGINAL
        } else {
            AppThemeConfig.NOIR_ORIGINAL
        }
    }

    val customThemesFlow: Flow<List<AppThemeConfig>> = context.dataStore.data.map { preferences ->
        val json = preferences[PreferencesKeys.CUSTOM_THEMES_JSON]
        if (json != null) {
            AppThemeConfig.listFromJson(json)
        } else {
            emptyList()
        }
    }

    suspend fun updateActiveTheme(theme: AppThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_THEME_JSON] = theme.toJson()
        }
    }

    suspend fun saveCustomTheme(theme: AppThemeConfig) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[PreferencesKeys.CUSTOM_THEMES_JSON]
            val currentList = if (currentJson != null) {
                AppThemeConfig.listFromJson(currentJson).toMutableList()
            } else {
                mutableListOf()
            }
            val existingIndex = currentList.indexOfFirst { it.id == theme.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = theme
            } else {
                currentList.add(theme)
            }
            preferences[PreferencesKeys.CUSTOM_THEMES_JSON] = AppThemeConfig.listToJson(currentList)
            preferences[PreferencesKeys.ACTIVE_THEME_JSON] = theme.toJson()
        }
    }

    suspend fun deleteCustomTheme(themeId: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[PreferencesKeys.CUSTOM_THEMES_JSON]
            if (currentJson != null) {
                val currentList = AppThemeConfig.listFromJson(currentJson).filter { it.id != themeId }
                preferences[PreferencesKeys.CUSTOM_THEMES_JSON] = AppThemeConfig.listToJson(currentList)
            }
            val activeJson = preferences[PreferencesKeys.ACTIVE_THEME_JSON]
            if (activeJson != null) {
                val active = AppThemeConfig.fromJson(activeJson)
                if (active?.id == themeId) {
                    preferences[PreferencesKeys.ACTIVE_THEME_JSON] = AppThemeConfig.NOIR_ORIGINAL.toJson()
                }
            }
        }
    }

    suspend fun duplicateCustomTheme(theme: AppThemeConfig, newName: String) {
        val duplicated = theme.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = newName,
            isPreset = false
        )
        saveCustomTheme(duplicated)
    }

    suspend fun resetThemeToDefault() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_THEME_JSON] = AppThemeConfig.NOIR_ORIGINAL.toJson()
        }
    }

    suspend fun resetPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SORT_OPTION)
            preferences.remove(PreferencesKeys.SORT_ASCENDING)
            preferences.remove(PreferencesKeys.IS_GRID_VIEW)
            preferences.remove(PreferencesKeys.SELECTED_TAB)
            preferences.remove(PreferencesKeys.SHUFFLE_MODE)
            preferences.remove(PreferencesKeys.REPEAT_MODE)
            preferences.remove(PreferencesKeys.ANIMATIONS_ENABLED)
            preferences.remove(PreferencesKeys.ACTIVE_THEME_JSON)
        }
    }
}
