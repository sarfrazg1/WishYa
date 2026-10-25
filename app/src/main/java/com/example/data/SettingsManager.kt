package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val defaultHour: Int = 9,
    val defaultMinute: Int = 0,
    val defaultOneDayBefore: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wishya_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val hour = prefs.getInt(KEY_HOUR, 9)
        val minute = prefs.getInt(KEY_MINUTE, 0)
        val oneDayBefore = prefs.getBoolean(KEY_ONE_DAY_BEFORE, true)
        val themeStr = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        return UserSettings(hour, minute, oneDayBefore, themeMode)
    }

    fun updateDefaultTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()
        _settings.value = _settings.value.copy(defaultHour = hour, defaultMinute = minute)
    }

    fun updateOneDayBefore(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_ONE_DAY_BEFORE, enabled)
            .apply()
        _settings.value = _settings.value.copy(defaultOneDayBefore = enabled)
    }

    fun updateThemeMode(mode: ThemeMode) {
        prefs.edit()
            .putString(KEY_THEME, mode.name)
            .apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    companion object {
        private const val KEY_HOUR = "key_default_hour"
        private const val KEY_MINUTE = "key_default_minute"
        private const val KEY_ONE_DAY_BEFORE = "key_one_day_before"
        private const val KEY_THEME = "key_theme_mode"

        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
