package com.example.spendwise

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences1, rootKey)
    }
}