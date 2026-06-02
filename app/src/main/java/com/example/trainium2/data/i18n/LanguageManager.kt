package com.example.trainium2.data.i18n

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.trainium2.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class LanguageManager(private val context: Context) {

    companion object {
        private val LANGUAGE_CODE = stringPreferencesKey("app_language_code")
    }

    val currentLanguage: Flow<AppLanguage?> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE_CODE]?.let { AppLanguage.fromCode(it) }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_CODE] = language.code
        }
    }

    fun detectSystemLanguage(): AppLanguage =
        AppLanguage.fromLocale(Locale.getDefault())
}