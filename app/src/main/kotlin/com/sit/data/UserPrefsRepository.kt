package com.sit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sit.domain.AppLanguage
import com.sit.domain.AppTheme
import com.sit.domain.AudioTrack
import com.sit.domain.WorkoutConfig
import com.sit.domain.WorkoutMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sit_user_prefs")

class UserPrefsRepository(private val context: Context) {

    private object Keys {
        val TOTAL_SEC = intPreferencesKey("total_sec")
        val SPRINTS = intPreferencesKey("sprints")
        val SPRINT_SEC = intPreferencesKey("sprint_sec")
        val REST_SEC = intPreferencesKey("rest_sec")
        val AUDIO = stringPreferencesKey("audio")
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val MODE = stringPreferencesKey("mode")
        val ADVANCED_SPRINTS = stringPreferencesKey("advanced_sprints")
    }

    val flow: Flow<UserPrefs> = context.dataStore.data.map { p ->
        val d = UserPrefs.DEFAULT
        UserPrefs(
            config = WorkoutConfig(
                totalSec = p[Keys.TOTAL_SEC] ?: d.config.totalSec,
                sprints = p[Keys.SPRINTS] ?: d.config.sprints,
                sprintSec = p[Keys.SPRINT_SEC] ?: d.config.sprintSec,
                restSec = p[Keys.REST_SEC] ?: d.config.restSec,
                audio = p[Keys.AUDIO]?.let(::parseAudio) ?: d.config.audio,
                mode = p[Keys.MODE]?.let(::parseMode) ?: d.config.mode,
                advancedSprintSecs = p[Keys.ADVANCED_SPRINTS]?.let(::parseAdvancedSprintSecs)
                    ?: d.config.advancedSprintSecs,
            ),
            theme = p[Keys.THEME]?.let(::parseTheme) ?: d.theme,
            language = p[Keys.LANGUAGE]?.let(::parseLanguage) ?: d.language,
        )
    }

    suspend fun save(prefs: UserPrefs) {
        context.dataStore.edit { p ->
            p[Keys.TOTAL_SEC] = prefs.config.totalSec
            p[Keys.SPRINTS] = prefs.config.sprints
            p[Keys.SPRINT_SEC] = prefs.config.sprintSec
            p[Keys.REST_SEC] = prefs.config.restSec
            p[Keys.AUDIO] = prefs.config.audio.name
            p[Keys.THEME] = prefs.theme.name
            p[Keys.LANGUAGE] = prefs.language.name
            p[Keys.MODE] = prefs.config.mode.name
            p[Keys.ADVANCED_SPRINTS] = prefs.config.advancedSprintSecs.joinToString(",")
        }
    }

    private fun parseAudio(s: String): AudioTrack =
        runCatching { AudioTrack.valueOf(s) }.getOrDefault(UserPrefs.DEFAULT.config.audio)

    private fun parseTheme(s: String): AppTheme =
        runCatching { AppTheme.valueOf(s) }.getOrDefault(UserPrefs.DEFAULT.theme)

    private fun parseLanguage(s: String): AppLanguage =
        runCatching { AppLanguage.valueOf(s) }.getOrDefault(UserPrefs.DEFAULT.language)

    private fun parseMode(s: String): WorkoutMode =
        runCatching { WorkoutMode.valueOf(s) }.getOrDefault(UserPrefs.DEFAULT.config.mode)

    private fun parseAdvancedSprintSecs(s: String): List<Int> =
        s.split(',')
            .mapNotNull { value -> value.toIntOrNull() }
            .ifEmpty { UserPrefs.DEFAULT.config.advancedSprintSecs }
}
