package com.maxweb.xunkal1.prefs

import android.content.Context
import android.content.SharedPreferences

class AppPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var theme: Int
        get() = prefs.getInt(KEY_THEME, 0)
        set(v) = prefs.edit().putInt(KEY_THEME, v).apply()

    var desktopMode: Boolean
        get() = prefs.getBoolean(KEY_DESKTOP, true)
        set(v) = prefs.edit().putBoolean(KEY_DESKTOP, v).apply()

    var jsEnabled: Boolean
        get() = prefs.getBoolean(KEY_JS, true)
        set(v) = prefs.edit().putBoolean(KEY_JS, v).apply()

    var domStorageEnabled: Boolean
        get() = prefs.getBoolean(KEY_DOM, true)
        set(v) = prefs.edit().putBoolean(KEY_DOM, v).apply()

    var dbEnabled: Boolean
        get() = prefs.getBoolean(KEY_DB, true)
        set(v) = prefs.edit().putBoolean(KEY_DB, v).apply()

    companion object {
        private const val PREFS_NAME = "max_web_prefs"
        private const val KEY_THEME = "theme"
        private const val KEY_DESKTOP = "desktop"
        private const val KEY_JS = "js"
        private const val KEY_DOM = "dom"
        private const val KEY_DB = "db"
    }
}