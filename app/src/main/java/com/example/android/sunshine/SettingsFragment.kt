/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.example.android.sunshine.data.SunshinePreferences
import com.example.android.sunshine.sync.SunshineSyncUtils

/**
 * The SettingsFragment serves as the display for all of the user's settings. In Sunshine, the
 * user will be able to change their preference for units of measurement from metric to imperial,
 * set their preferred weather location, and indicate whether or not they'd like to see
 * notifications.
 *
 * Please note: If you are using our dummy weather services, the location returned will always be
 * Mountain View, California.
 */
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private fun setPreferenceSummary(preference: Preference, value: Any?) {
        val stringValue = value!!.toString()

        if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            val prefIndex = preference.findIndexOfValue(stringValue)
            if (prefIndex >= 0) {
                preference.setSummary(preference.entries[prefIndex])
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.summary = stringValue
        }
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general)

        val sharedPreferences = preferenceScreen.sharedPreferences
        val prefScreen = preferenceScreen
        val count = prefScreen.preferenceCount
        for (i in 0 until count) {
            val p = prefScreen.getPreference(i)
            if (p !is CheckBoxPreference) {
                val value = sharedPreferences.getString(p.key, "")
                setPreferenceSummary(p, value)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // unregister the preference change listener
        preferenceScreen.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onStart() {
        super.onStart()
        // register the preference change listener
        preferenceScreen.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val activity = activity

        if (key == getString(R.string.pref_location_key)) {
            // we've changed the location
            // Wipe out any potential PlacePicker latlng values so that we can use this text entry.
            SunshinePreferences.resetLocationCoordinates(activity)
            SunshineSyncUtils.startImmediateSync(activity!!)
        } else if (key == getString(R.string.pref_units_key)) {
            // units have changed. update lists of weather entries accordingly
            //activity!!.recyclerView.adapter.notifyDataSetChanged()
        }
        val preference = findPreference(key)
        if (null != preference) {
            if (preference !is CheckBoxPreference) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""))
            }
        }
    }
}
