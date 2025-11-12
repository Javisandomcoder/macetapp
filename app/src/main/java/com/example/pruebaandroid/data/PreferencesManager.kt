package com.example.pruebaandroid.data

import android.content.Context
import android.content.SharedPreferences
import com.example.pruebaandroid.ui.Theme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "macetohuerto_prefs",
        Context.MODE_PRIVATE
    )





    private val _notificationsEnabled = MutableStateFlow(getNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _theme = MutableStateFlow(getTheme())
    val theme: StateFlow<Theme> = _theme

    private val _geminiApiKey = MutableStateFlow(getGeminiApiKey())
    val geminiApiKey: StateFlow<String?> = _geminiApiKey







    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            apply()
        }
        _notificationsEnabled.value = enabled
    }

    fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setTheme(theme: Theme) {
        prefs.edit().apply {
            putString(KEY_THEME, theme.name)
            apply()
        }
        _theme.value = theme
    }

    fun getTheme(): Theme {
        val themeName = prefs.getString(KEY_THEME, Theme.SYSTEM.name) ?: Theme.SYSTEM.name
        return Theme.valueOf(themeName)
    }



    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit().apply {
            putInt(KEY_NOTIFICATION_HOUR, hour)
            putInt(KEY_NOTIFICATION_MINUTE, minute)
            apply()
        }
    }

    fun getNotificationTime(): Pair<Int, Int> {
        val hour = prefs.getInt(KEY_NOTIFICATION_HOUR, 9) // Default 9 AM
        val minute = prefs.getInt(KEY_NOTIFICATION_MINUTE, 0) // Default 0 minutes
        return Pair(hour, minute)
    }

    fun setGeminiApiKey(apiKey: String) {
        prefs.edit().apply {
            putString(KEY_GEMINI_API_KEY, apiKey)
            apply()
        }
        _geminiApiKey.value = apiKey
    }

    fun getGeminiApiKey(): String? {
        return prefs.getString(KEY_GEMINI_API_KEY, null)
    }

    fun hasGeminiApiKey(): Boolean {
        return !getGeminiApiKey().isNullOrBlank()
    }

    companion object {

        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_THEME = "theme"

        private const val KEY_NOTIFICATION_HOUR = "notification_hour"
        private const val KEY_NOTIFICATION_MINUTE = "notification_minute"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"



        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
