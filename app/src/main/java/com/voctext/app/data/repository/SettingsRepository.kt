package com.voctext.app.data.repository

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getApiKey(): String? {
        return prefs.getString(KEY_GROQ_API_KEY, null)?.takeIf { it.isNotBlank() }
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GROQ_API_KEY, apiKey.trim()).apply()
    }

    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_GROQ_API_KEY).apply()
    }

    companion object {
        private const val PREFS_NAME = "voctext_settings"
        private const val KEY_GROQ_API_KEY = "groq_api_key"
    }
}
