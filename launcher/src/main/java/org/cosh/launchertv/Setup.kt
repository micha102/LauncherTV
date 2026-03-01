package org.cosh.launchertv

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.cosh.launchertv.fragments.PreferencesFragment
import kotlin.text.toInt

class Setup(private val context: Context) {

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private fun getInt(name: String, defaultValue: Int): Int {
        return try {
            preferences.getInt(name, defaultValue) // Use getInt for integer preferences
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    private fun getString(name: String, defaultValue: String): String {
            return preferences.getString(name, defaultValue).toString() // Use getString for string preferences
    }
    fun isDefaultTransparency(): Boolean {
        return try {
            preferences.getBoolean(PreferencesFragment.PREFERENCE_DEFAULT_TRANSPARENCY, true)
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    fun getTransparency(): Int {
        return preferences.getInt(PreferencesFragment.PREFERENCE_TRANSPARENCY, 50)
    }
    fun keepScreenOn(): Boolean {
        return try {
            preferences.getBoolean(PreferencesFragment.PREFERENCE_SCREEN_ON, false)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun iconsLocked(): Boolean {
        return try {
            preferences.getBoolean(PreferencesFragment.PREFERENCE_LOCKED, false)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun showDate(): Boolean {
        return try {
            preferences.getBoolean(PreferencesFragment.PREFERENCE_SHOW_DATE, true)
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    fun showBattery(): Boolean {
        return try {
            preferences.getBoolean(PreferencesFragment.PREFERENCE_SHOW_BATTERY, false)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun showNames(): Boolean {
        return try {
            preferences.getBoolean(PreferencesFragment.PREFERENCE_SHOW_NAME, true)
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    fun getGridX(): Int = getString(PreferencesFragment.PREFERENCE_GRID_X, "3").toInt()
    fun getGridY(): Int = getString(PreferencesFragment.PREFERENCE_GRID_Y, "2").toInt()
    fun getMarginX(): Int = getString(PreferencesFragment.PREFERENCE_MARGIN_X, "5").toInt()
    fun getMarginY(): Int = getString(PreferencesFragment.PREFERENCE_MARGIN_Y, "5").toInt()
}