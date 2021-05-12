package com.doitstudio.sleepest_master

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class ProfileFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}