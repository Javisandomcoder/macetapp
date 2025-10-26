package com.example.pruebaandroid.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "macetohuerto_prefs",
        Context.MODE_PRIVATE
    )

    private val _notificationHour = MutableStateFlow(getNotificationHour())
    val notificationHour: StateFlow<Int> = _notificationHour

    private val _notificationMinute = MutableStateFlow(getNotificationMinute())
    val notificationMinute: StateFlow<Int> = _notificationMinute

    private val _notificationsEnabled = MutableStateFlow(getNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _isDarkMode = MutableStateFlow(getDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit().apply {
            putInt(KEY_NOTIFICATION_HOUR, hour)
            putInt(KEY_NOTIFICATION_MINUTE, minute)
            apply()
        }
        _notificationHour.value = hour
        _notificationMinute.value = minute
    }

    fun getNotificationHour(): Int {
        return prefs.getInt(KEY_NOTIFICATION_HOUR, DEFAULT_HOUR)
    }

    fun getNotificationMinute(): Int {
        return prefs.getInt(KEY_NOTIFICATION_MINUTE, DEFAULT_MINUTE)
    }

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

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_DARK_MODE, enabled)
            apply()
        }
        _isDarkMode.value = enabled
    }

    fun getDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    companion object {
        private const val KEY_NOTIFICATION_HOUR = "notification_hour"
        private const val KEY_NOTIFICATION_MINUTE = "notification_minute"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DARK_MODE = "dark_mode"

        private const val DEFAULT_HOUR = 9 // 9 AM
        private const val DEFAULT_MINUTE = 0

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
