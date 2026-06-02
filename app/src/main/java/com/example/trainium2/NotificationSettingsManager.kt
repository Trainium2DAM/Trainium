package com.example.trainium2

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationSettingsManager(private val context: Context) {
    companion object {
        private val NOTIFY_MINUTES_BEFORE = intPreferencesKey("notify_minutes_before")
        const val DEFAULT_MINUTES = 30
        val OPTIONS = listOf(10, 15, 30, 60, 120)
    }

    val minutesBefore: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[NOTIFY_MINUTES_BEFORE] ?: DEFAULT_MINUTES
    }

    suspend fun setMinutesBefore(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFY_MINUTES_BEFORE] = minutes
        }
    }
}
