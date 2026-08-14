package de.mamakow.dienstplanapotheke.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.mamakow.dienstplanapotheke.R
import de.mamakow.dienstplanapotheke.util.SystemUtils

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Set App Version summary
        val versionPref: Preference? = findPreference("pref_app_version")
        versionPref?.summary = SystemUtils.getAppVersion(requireContext())

        // Handle Support Email click
        val supportPref: Preference? = findPreference("pref_support_email")
        supportPref?.setOnPreferenceClickListener {
            SystemUtils.sendSupportEmail(requireContext())
            true
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "pref_ui_theme_mode") {
            val themeValue = sharedPreferences?.getString(key, "system")
            SystemUtils.applyTheme(themeValue)
        }
    }
}
