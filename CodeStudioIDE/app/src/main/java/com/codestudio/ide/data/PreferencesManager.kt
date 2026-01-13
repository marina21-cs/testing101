package com.codestudio.ide.data

import android.content.Context
import android.content.SharedPreferences
import com.codestudio.ide.model.EditorSettings
import com.google.gson.Gson

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    var editorSettings: EditorSettings
        get() {
            val json = prefs.getString(KEY_EDITOR_SETTINGS, null)
            return if (json != null) {
                gson.fromJson(json, EditorSettings::class.java)
            } else {
                EditorSettings()
            }
        }
        set(value) {
            prefs.edit().putString(KEY_EDITOR_SETTINGS, gson.toJson(value)).apply()
        }

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var lastOpenedProject: String?
        get() = prefs.getString(KEY_LAST_PROJECT, null)
        set(value) = prefs.edit().putString(KEY_LAST_PROJECT, value).apply()

    var lastOpenedFiles: List<String>
        get() {
            val json = prefs.getString(KEY_LAST_FILES, null)
            return if (json != null) {
                gson.fromJson(json, Array<String>::class.java).toList()
            } else {
                emptyList()
            }
        }
        set(value) {
            prefs.edit().putString(KEY_LAST_FILES, gson.toJson(value)).apply()
        }

    var showHiddenFiles: Boolean
        get() = prefs.getBoolean(KEY_SHOW_HIDDEN, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_HIDDEN, value).apply()

    var terminalFontSize: Int
        get() = prefs.getInt(KEY_TERMINAL_FONT_SIZE, 12)
        set(value) = prefs.edit().putInt(KEY_TERMINAL_FONT_SIZE, value).apply()

    var autoSaveEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SAVE, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SAVE, value).apply()

    var autoSaveInterval: Int
        get() = prefs.getInt(KEY_AUTO_SAVE_INTERVAL, 30)
        set(value) = prefs.edit().putInt(KEY_AUTO_SAVE_INTERVAL, value).apply()

    var webServerPort: Int
        get() = prefs.getInt(KEY_WEB_SERVER_PORT, 8080)
        set(value) = prefs.edit().putInt(KEY_WEB_SERVER_PORT, value).apply()

    var defaultEncoding: String
        get() = prefs.getString(KEY_DEFAULT_ENCODING, "UTF-8") ?: "UTF-8"
        set(value) = prefs.edit().putString(KEY_DEFAULT_ENCODING, value).apply()

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "codestudio_prefs"
        private const val KEY_EDITOR_SETTINGS = "editor_settings"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_LAST_PROJECT = "last_project"
        private const val KEY_LAST_FILES = "last_files"
        private const val KEY_SHOW_HIDDEN = "show_hidden"
        private const val KEY_TERMINAL_FONT_SIZE = "terminal_font_size"
        private const val KEY_AUTO_SAVE = "auto_save"
        private const val KEY_AUTO_SAVE_INTERVAL = "auto_save_interval"
        private const val KEY_WEB_SERVER_PORT = "web_server_port"
        private const val KEY_DEFAULT_ENCODING = "default_encoding"
    }
}
