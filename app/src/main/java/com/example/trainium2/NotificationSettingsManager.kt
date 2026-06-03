package com.example.trainium2

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationSettingsManager(private val context: Context) {
    companion object {
        private val NOTIFY_MINUTES_BEFORE = intPreferencesKey("notify_minutes_before")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        const val DEFAULT_MINUTES = 30
        val OPTIONS = listOf(10, 15, 30, 60, 120)
    }

    val minutesBefore: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[NOTIFY_MINUTES_BEFORE] ?: DEFAULT_MINUTES
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setMinutesBefore(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFY_MINUTES_BEFORE] = minutes
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
