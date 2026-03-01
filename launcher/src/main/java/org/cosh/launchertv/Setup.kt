package org.cosh.launchertv

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.cosh.launchertv.fragments.PreferencesFragment

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

    fun isDefaultTransparency(): Boolean {
        return try {
            preferences.getBoolean(PreferencesFragment.PREFERENCE_DEFAULT_TRANSPARENCY, true)
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    // Update the transparency getter to handle the integer value
    fun getTransparency(): Float {
        return try {
            // Get the transparency as an integer (from 0 to 100)
            val transparencyInt = preferences.getInt(PreferencesFragment.PREFERENCE_TRANSPARENCY, 50)
            transparencyInt.toFloat() / 100f // Convert the integer value to float (0.0 to 1.0)
        } catch (e: Exception) {
            e.printStackTrace()
            0.5f // Default value
        }
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

    fun getGridX(): Int = getInt(PreferencesFragment.PREFERENCE_GRID_X, 3)
    fun getGridY(): Int = getInt(PreferencesFragment.PREFERENCE_GRID_Y, 2)
    fun getMarginX(): Int = getInt(PreferencesFragment.PREFERENCE_MARGIN_X, 5)
    fun getMarginY(): Int = getInt(PreferencesFragment.PREFERENCE_MARGIN_Y, 5)
}