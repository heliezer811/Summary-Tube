package com.summarytube

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("summary_prefs", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString("api_key", "") ?: ""
        set(value) = prefs.edit().putString("api_key", value).apply()

    var customPrompt: String
        get() = prefs.getString("custom_prompt", "Extraía a transcrição do vídeo anexado...") ?: "Extraía a transcrição do vídeo anexado..."
        set(value) = prefs.edit().putString("custom_prompt", value).apply()

    var selectedModel: String
        get() = prefs.getString("selected_model", "gpt-4o") ?: "gpt-4o"
        set(value) = prefs.edit().putString("selected_model", value).apply()
}
