package org.cosh.launchertv.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import androidx.preference.SeekBarPreference
import androidx.preference.PreferenceCategory
import org.cosh.launchertv.R
import org.cosh.launchertv.Setup
import java.util.Locale

class PreferencesFragment : PreferenceFragmentCompat() {

    companion object {
        const val PREFERENCE_DEFAULT_TRANSPARENCY = "preference_default_transparency"
        const val PREFERENCE_TRANSPARENCY = "preference_transparency"
        const val PREFERENCE_SCREEN_ON = "preference_screen_always_on"
        const val PREFERENCE_SHOW_DATE = "preference_show_date"
        const val PREFERENCE_SHOW_BATTERY = "preference_show_battery"
        const val PREFERENCE_GRID_X = "preference_grid_x"
        const val PREFERENCE_GRID_Y = "preference_grid_y"
        const val PREFERENCE_SHOW_NAME = "preference_show_name"
        const val PREFERENCE_MARGIN_X = "preference_margin_x"
        const val PREFERENCE_MARGIN_Y = "preference_margin_y"
        const val PREFERENCE_LOCKED = "preference_locked"

        private const val PREFERENCE_GITHUB = "preference_github"
        private const val PREFERENCE_ABOUT = "preference_about"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val setup = Setup(requireContext())

        bindSummary(PREFERENCE_GRID_X, R.string.summary_grid_x)
        bindSummary(PREFERENCE_GRID_Y, R.string.summary_grid_y)
        bindSummary(PREFERENCE_MARGIN_X, R.string.summary_margin_x)
        bindSummary(PREFERENCE_MARGIN_Y, R.string.summary_margin_y)

        // Disable the transparency SeekBar if default transparency is enabled
        findPreference<Preference>(PREFERENCE_TRANSPARENCY)?.isEnabled = !setup.isDefaultTransparency()

        // Toggle the transparency SeekBar based on the default transparency setting
        findPreference<Preference>(PREFERENCE_DEFAULT_TRANSPARENCY)?.setOnPreferenceChangeListener { _, newValue ->
            findPreference<Preference>(PREFERENCE_TRANSPARENCY)?.isEnabled = !(newValue as Boolean)
            true
        }


        setupGithubLink()
        setupAboutPreference()

        // Bind the transparency SeekBar
        bindSeekBarSummary(PREFERENCE_TRANSPARENCY)
    }

    private fun setupGithubLink() {
        findPreference<Preference>(PREFERENCE_GITHUB)
            ?.setOnPreferenceClickListener {
                openUrl(getString(R.string.github_url), "Github")
                true
            }
    }




    private fun setupAboutPreference() {
        val version = try {
            val packageInfo = requireContext()
                .packageManager
                .getPackageInfo(requireContext().packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "#Err"
        }

        findPreference<Preference>(PREFERENCE_ABOUT)?.apply {
            title = "${getString(R.string.app_name)} version $version"
            setOnPreferenceClickListener {
                openUrl(
                    "https://play.google.com/store/apps/details?id=org.cosinus.launchertv",
                    "Play Store"
                )
                true
            }
        }
    }



    private fun bindSummary(key: String, resId: Int) {
        val preference = findPreference<ListPreference>(key) ?: return

        updateSummary(preference, resId, preference.value)

        preference.setOnPreferenceChangeListener { _, newValue ->
            updateSummary(preference, resId, newValue as String)
            true
        }
    }


    private fun bindSeekBarSummary(key: String) {
        val transparencyPreference = findPreference<SeekBarPreference>(key) ?: return
        val setup = Setup(requireContext())

        // Set the initial value for transparency
        val transparency = setup.getTransparency() * 100 // Convert to integer (0 to 100)
        transparencyPreference.value = transparency.toInt()

        // Update the summary with the current transparency value
        updateSeekBarSummary(transparencyPreference)

        transparencyPreference.setOnPreferenceChangeListener { _, newValue ->
            // Update the summary with the new value
            updateSeekBarSummary(transparencyPreference)
            true
        }
    }

    private fun updateSeekBarSummary(transparencyPreference: SeekBarPreference) {
        val progress = transparencyPreference.value
        val transparencyValue = progress / 100f // Convert back to float (0.0 to 1.0)
        transparencyPreference.summary = String.format(
            Locale.getDefault(),
            getString(R.string.summary_transparency),
            (transparencyValue * 100).toInt() // Display percentage
        )
    }
    private fun updateSummary(
        preference: ListPreference,
        resId: Int,
        value: String
    ) {
        preference.summary = String.format(
            Locale.getDefault(),
            getString(resId),
            value
        )
    }

    private fun openUrl(url: String, label: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_opening_link)
                    .format(label, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}