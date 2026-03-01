package org.cosh.launchertv

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        const val PREFERENCES_NAME = "applications"
    }

    // Save a package name to the preferences with a specific key
    fun savePackageName(key: String, packageName: String?) {
        if (packageName.isNullOrEmpty()) {
            editor.remove(key)
        } else {
            editor.putString(key, packageName)
        }
        editor.apply()
    }

    // Get a package name from preferences with a specific key
    fun getPackageName(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    // Clear all preferences (optional, based on your needs)
    fun clearPreferences() {
        editor.clear().apply()
    }
}